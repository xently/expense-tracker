package co.ke.xently.expense.tracker.server

import co.ke.xently.expense.tracker.server.notification.BackgroundScheduler
import co.ke.xently.expense.tracker.server.notification.FcmPushNotificationService
import co.ke.xently.expense.tracker.server.notification.OverdueNotificationManager
import co.ke.xently.expense.tracker.server.repository.FirestoreRepositoryProvider
import co.ke.xently.expense.tracker.server.repository.InMemoryRepositoryProvider
import co.ke.xently.expense.tracker.server.repository.RepositoryProvider
import co.ke.xently.expense.tracker.server.routes.apiRoutes
import co.ke.xently.expense.tracker.server.sse.EventBroadcaster
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sse.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::mainModule)
        .start(wait = true)
}

fun Application.mainModule() {
    module(InMemoryRepositoryProvider())
}

@Serializable
data class ServerRootResponse(
    val service: String,
    val version: String
)

fun Application.module(
    repositoryProvider: RepositoryProvider = InMemoryRepositoryProvider()
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
    backgroundScheduler.start()

    monitor.subscribe(ApplicationStopped) {
        backgroundScheduler.stop()
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
            overdueNotificationManager = overdueNotificationManager
        )
    }
}
