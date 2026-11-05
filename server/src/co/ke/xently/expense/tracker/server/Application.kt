package co.ke.xently.expense.tracker.server

import co.ke.xently.expense.tracker.server.auth.AuthTokenVerifier
import co.ke.xently.expense.tracker.server.auth.DevelopmentAuthTokenVerifier
import co.ke.xently.expense.tracker.server.auth.FirebaseAuthTokenVerifier
import co.ke.xently.expense.tracker.server.notification.BackgroundScheduler
import co.ke.xently.expense.tracker.server.notification.FcmPushNotificationService
import co.ke.xently.expense.tracker.server.notification.OverdueNotificationManager
import co.ke.xently.expense.tracker.server.repository.InMemoryProjectRepository
import co.ke.xently.expense.tracker.server.repository.InMemoryRepositoryProvider
import co.ke.xently.expense.tracker.server.repository.FirestoreProjectRepository
import co.ke.xently.expense.tracker.server.repository.DEMO_ACCESS_TOKEN
import co.ke.xently.expense.tracker.server.repository.DEMO_USER
import co.ke.xently.expense.tracker.server.repository.ProjectRepository
import co.ke.xently.expense.tracker.server.repository.RepositoryProvider
import co.ke.xently.expense.tracker.server.repository.seedDemoWorkspace
import co.ke.xently.expense.tracker.server.routes.apiRoutes
import co.ke.xently.expense.tracker.server.routes.projectRoutes
import co.ke.xently.expense.tracker.server.sse.EventBroadcaster
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import io.ktor.server.netty.EngineMain
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.application.install
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.sse.SSE
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.runBlocking
import java.io.File

private const val LEGACY_DEVELOPMENT_CONFIG = "src/main/resources/application-dev.yaml"
private const val DEVELOPMENT_CONFIG_RESOURCE = "application-dev.yaml"

fun main(args: Array<String>): Unit = EngineMain.main(normalizeEngineArgs(args))

internal fun normalizeEngineArgs(
    args: Array<String>,
    fileExists: (String) -> Boolean = { File(it).isFile },
    resourceExists: (String) -> Boolean = {
        Application::class.java.classLoader.getResource(it) != null
    }
): Array<String> = args.map { argument ->
    if (
        argument == "-config=$LEGACY_DEVELOPMENT_CONFIG" &&
        !fileExists(LEGACY_DEVELOPMENT_CONFIG) &&
        !resourceExists(LEGACY_DEVELOPMENT_CONFIG) &&
        resourceExists(DEVELOPMENT_CONFIG_RESOURCE)
    ) {
        "-config=$DEVELOPMENT_CONFIG_RESOURCE"
    } else {
        argument
    }
}.toTypedArray()

fun Application.mainModule() {
    val config = environment.config
    val demoMode = config.booleanOrDefault("app.demoMode", false)
    val enableLegacyFinanceRoutes = config.booleanOrDefault("app.enableLegacyFinanceRoutes", false)
    val repositoryType = config.stringOrDefault("app.projectRepository", "inmemory").lowercase()
    if (!demoMode) initializeFirebaseAdmin(config.stringOrDefault("app.firebaseProjectId", ""))
    val projectRepository = when (repositoryType) {
        "inmemory" -> InMemoryProjectRepository()
        "firestore" -> {
            check(!demoMode) { "Demo mode is only supported with the in-memory project repository" }
            FirestoreProjectRepository()
        }
        else -> error("app.projectRepository must be either 'inmemory' or 'firestore'")
    }
    val authTokenVerifier = if (!demoMode) FirebaseAuthTokenVerifier() else {
        runBlocking { seedDemoWorkspace(projectRepository) }
        DevelopmentAuthTokenVerifier(
            mapOf(DEMO_ACCESS_TOKEN to DEMO_USER)
        )
    }
    module(
        repositoryProvider = InMemoryRepositoryProvider(),
        projectRepository = projectRepository,
        authTokenVerifier = authTokenVerifier,
        enableLegacyFinanceRoutes = enableLegacyFinanceRoutes
    )
}

private fun initializeFirebaseAdmin(projectId: String) {
    if (FirebaseApp.getApps().none { it.name == FirebaseApp.DEFAULT_APP_NAME }) {
        val options = FirebaseOptions.builder()
            .setCredentials(GoogleCredentials.getApplicationDefault())
            .apply {
                projectId.takeIf(String::isNotBlank)?.let(::setProjectId)
            }
            .build()
        FirebaseApp.initializeApp(options)
    }
}

private fun ApplicationConfig.stringOrDefault(path: String, default: String): String =
    propertyOrNull(path)?.getString() ?: default

private fun ApplicationConfig.booleanOrDefault(path: String, default: Boolean): Boolean =
    propertyOrNull(path)?.getString()?.toBooleanStrictOrNull() ?: default

@Serializable
data class ServerRootResponse(
    val service: String,
    val version: String
)

fun Application.module(
    repositoryProvider: RepositoryProvider = InMemoryRepositoryProvider(),
    projectRepository: ProjectRepository = InMemoryProjectRepository(),
    authTokenVerifier: AuthTokenVerifier = FirebaseAuthTokenVerifier(),
    enableLegacyFinanceRoutes: Boolean = false
) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }

    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowMethod(HttpMethod.Post)
        allowMethod(HttpMethod.Get)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        anyHost()
    }

    install(SSE)

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(
                HttpStatusCode.InternalServerError,
                mapOf("error" to (cause.message ?: "Internal server error"))
            )
        }
    }

    val eventBroadcaster = EventBroadcaster()
    val pushNotificationService = FcmPushNotificationService()
    val overdueNotificationManager = OverdueNotificationManager(
        repositoryProvider = repositoryProvider,
        pushNotificationService = pushNotificationService,
        eventBroadcaster = eventBroadcaster
    )

    val backgroundScheduler = BackgroundScheduler(overdueNotificationManager)
    if (enableLegacyFinanceRoutes) {
        backgroundScheduler.start()
        monitor.subscribe(ApplicationStopped) {
            backgroundScheduler.stop()
        }
    }

    routing {
        get("/") {
            call.respond(
                ServerRootResponse(
                    service = "Sugarcane Season Expense Tracker Server",
                    version = "1.0.0"
                )
            )
        }

        apiRoutes(
            repositoryProvider = repositoryProvider,
            eventBroadcaster = eventBroadcaster,
            overdueNotificationManager = overdueNotificationManager,
            enableLegacyFinanceRoutes = enableLegacyFinanceRoutes
        )
        projectRoutes(projectRepository, authTokenVerifier)
    }
}
