package co.ke.xently.expense.tracker.server

import co.ke.xently.expense.tracker.model.DashboardSummary
import co.ke.xently.expense.tracker.model.MyProjectsResponse
import co.ke.xently.expense.tracker.model.PartnerId
import co.ke.xently.expense.tracker.model.Project
import co.ke.xently.expense.tracker.model.ProjectInvitation
import co.ke.xently.expense.tracker.server.auth.AuthTokenVerifier
import co.ke.xently.expense.tracker.server.auth.AuthenticatedUser
import co.ke.xently.expense.tracker.server.auth.DevelopmentAuthTokenVerifier
import co.ke.xently.expense.tracker.server.auth.FirebaseAuthTokenVerifier
import co.ke.xently.expense.tracker.server.repository.DEMO_ACCESS_TOKEN
import co.ke.xently.expense.tracker.server.repository.DEMO_USER
import co.ke.xently.expense.tracker.server.repository.InMemoryProjectRepository
import co.ke.xently.expense.tracker.server.repository.InMemoryRepositoryProvider
import co.ke.xently.expense.tracker.server.repository.seedDemoWorkspace
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun missingLegacyDevelopmentConfigUsesAmperClasspathResource() {
        val args = arrayOf("-config=src/main/resources/application-dev.yaml", "-port=9090")

        val normalized = normalizeEngineArgs(
            args = args,
            fileExists = { false },
            resourceExists = { it == "application-dev.yaml" }
        )

        assertEquals(listOf("-config=application-dev.yaml", "-port=9090"), normalized.toList())
    }

    @Test
    fun existingLegacyDevelopmentConfigTakesPrecedenceOverClasspathFallback() {
        val args = arrayOf("-config=src/main/resources/application-dev.yaml")

        val normalized = normalizeEngineArgs(
            args = args,
            fileExists = { it == "src/main/resources/application-dev.yaml" },
            resourceExists = { it == "application-dev.yaml" }
        )

        assertEquals(args.toList(), normalized.toList())
    }

    @Test
    fun existingLegacyClasspathResourceTakesPrecedenceOverAmperFallback() {
        val args = arrayOf("-config=src/main/resources/application-dev.yaml")

        val normalized = normalizeEngineArgs(
            args = args,
            fileExists = { false },
            resourceExists = { it == "src/main/resources/application-dev.yaml" || it == "application-dev.yaml" }
        )

        assertEquals(args.toList(), normalized.toList())
    }

    @Test
    fun otherConfigArgumentsAreNotRewrittenWhenNoMatchingResourceExists() {
        val args = arrayOf("-config=unavailable.yaml", "-port=9090")

        val normalized = normalizeEngineArgs(
            args = args,
            fileExists = { false },
            resourceExists = { false }
        )

        assertEquals(args.toList(), normalized.toList())
    }

    @Test
    fun serverConfigurationsUseApiPort8081() {
        listOf("application.yaml", "application-dev.yaml").forEach { configName ->
            val config = requireNotNull(javaClass.classLoader.getResourceAsStream(configName)) {
                "Missing server configuration: $configName"
            }.bufferedReader().use { it.readText() }

            val configuredPort = Regex("(?m)^\\s+port:\\s*(\\d+)\\s*$")
                .find(config)
                ?.groupValues
                ?.get(1)
            assertEquals("8081", configuredPort, "$configName should use the API port")
        }
    }

    private val testVerifier = object : AuthTokenVerifier {
        private val users = mapOf(
            "owner-token" to AuthenticatedUser("uid-owner", "owner@example.test", "Owner"),
            "partner-token" to AuthenticatedUser("uid-partner", "partner@example.test", "Partner"),
            "outsider-token" to AuthenticatedUser(
                "uid-outsider",
                "outsider@example.test",
                "Outsider"
            ),
            "new-user-token" to AuthenticatedUser("uid-new-user", "new@example.test", "New User")
        )

        override suspend fun verify(token: String): AuthenticatedUser? = users[token]
    }

    @Test
    fun firebaseVerifierFailsClosedWhenAdminAuthIsUnavailable() = runBlocking {
        val verifier = FirebaseAuthTokenVerifier { null }
        assertEquals(null, verifier.verify("untrusted-token"))
    }

    @Test
    fun demoWorkspaceIsSeededAndOnlyTheConfiguredDemoTokenCanAccessIt() = testApplication {
        val projectRepository = InMemoryProjectRepository()
        val project = seedDemoWorkspace(projectRepository)
        application {
            module(
                projectRepository = projectRepository,
                authTokenVerifier = DevelopmentAuthTokenVerifier(mapOf(DEMO_ACCESS_TOKEN to DEMO_USER))
            )
        }

        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/me/projects").status)
        assertEquals(
            HttpStatusCode.Unauthorized,
            client.get("/api/v1/me/projects") {
                header(HttpHeaders.Authorization, "Bearer invalid-demo-token")
            }.status
        )

        val headers = HttpHeaders.Authorization to "Bearer $DEMO_ACCESS_TOKEN"
        val profile = client.get("/api/v1/me/profile") { header(headers.first, headers.second) }
        assertEquals(HttpStatusCode.OK, profile.status)
        assertTrue(profile.bodyAsText().contains("vin"))

        val projectsResponse = client.get("/api/v1/me/projects") { header(headers.first, headers.second) }
        assertEquals(HttpStatusCode.OK, projectsResponse.status)
        val projects = json.decodeFromString<MyProjectsResponse>(projectsResponse.bodyAsText())
        assertEquals(listOf(project.id), projects.projects.map { it.id })
        assertEquals(project.id, projects.defaultProjectId)

        val invitations = client.get("/api/v1/me/invitations") { header(headers.first, headers.second) }
        assertEquals(HttpStatusCode.OK, invitations.status)
        assertTrue(invitations.bodyAsText().contains("Community Orchard"))

        val expenses = client.get("/api/v1/projects/${project.id}/expenses") {
            header(headers.first, headers.second)
        }
        assertEquals(HttpStatusCode.OK, expenses.status)
        assertTrue(expenses.bodyAsText().contains("Land preparation"))
        assertTrue(expenses.bodyAsText().contains("Seedlings and delivery"))

        val tranches = client.get("/api/v1/projects/${project.id}/tranches") {
            header(headers.first, headers.second)
        }
        assertEquals(HttpStatusCode.OK, tranches.status)
        assertTrue(tranches.bodyAsText().contains("DEMO-001"))
    }

    @Test
    fun testRootAndHealth() = testApplication {
        val repo = InMemoryRepositoryProvider()
        application {
            module(repo)
        }

        val rootResponse = client.get("/")
        assertEquals(HttpStatusCode.OK, rootResponse.status)
        assertTrue(rootResponse.bodyAsText().contains("Sugarcane Season Expense Tracker"))

        val healthResponse = client.get("/api/v1/health")
        assertEquals(HttpStatusCode.OK, healthResponse.status)
        assertTrue(healthResponse.bodyAsText().contains("healthy"))

        val openApiResponse = client.get("/api/v1/openapi.json")
        assertEquals(HttpStatusCode.OK, openApiResponse.status)
        val openApiBody = openApiResponse.bodyAsText()
        json.parseToJsonElement(openApiBody)
        assertTrue(openApiBody.contains("openapi"))
        assertTrue(openApiBody.contains("Project Expense Tracker API"))
        assertTrue(openApiBody.contains("/api/v1/projects/{projectId}/expenses"))
        assertTrue(openApiBody.contains("BearerAuth"))
        assertTrue(openApiBody.contains("\"url\": \"http://localhost:8081\""))

        val swaggerUiResponse = client.get("/api/v1/swagger")
        assertEquals(HttpStatusCode.OK, swaggerUiResponse.status)
        assertTrue(swaggerUiResponse.bodyAsText().contains("SwaggerUIBundle"))
    }

    @Test
    fun testDashboardAndLedgers() = testApplication {
        val repo = InMemoryRepositoryProvider()
        application {
            module(repo, enableLegacyFinanceRoutes = true)
        }

        val response = client.get("/api/v1/dashboard?asOfDate=2026-11-02")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        val dashboard = json.decodeFromString<DashboardSummary>(body)

        assertTrue(dashboard.totalExpenses > 0)
        assertEquals(3, dashboard.partnerSummaries.size)
        assertTrue(dashboard.partnerSummaries.any { it.partnerId == PartnerId.HARRISON.name })
    }

    @Test
    fun testCreateExpenseAndTranche() = testApplication {
        val repo = InMemoryRepositoryProvider()
        application {
            module(repo, enableLegacyFinanceRoutes = true)
        }

        // Post expense
        val expenseJson = """
            {
                "title": "Sugarcane Loading & Transport",
                "category": "TRANSPORT",
                "totalAmount": 36000.0,
                "currency": "KES",
                "transactionDate": "2026-08-10",
                "payingPartnerId": "HARRISON",
                "notes": "Lorry transport to factory"
            }
        """.trimIndent()

        val expRes = client.post("/api/v1/expenses") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(expenseJson)
        }
        assertEquals(HttpStatusCode.Created, expRes.status)
        assertTrue(expRes.bodyAsText().contains("TRANSPORT"))

        // Post tranche
        val trancheJson = """
            {
                "partnerId": "VIN",
                "amount": 12000.0,
                "paymentDate": "2026-08-15",
                "notes": "Transport share contribution",
                "referenceNumber": "MPESA-TRX999"
            }
        """.trimIndent()

        val trancheRes = client.post("/api/v1/tranches") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(trancheJson)
        }
        assertEquals(HttpStatusCode.Created, trancheRes.status)
        assertTrue(trancheRes.bodyAsText().contains("MPESA-TRX999"))
    }

    @Test
    fun testRevenueSplitCalculationRoute() = testApplication {
        val repo = InMemoryRepositoryProvider()
        application {
            module(repo, enableLegacyFinanceRoutes = true)
        }

        val requestJson = """
            {
                "grossFactoryPayout": 450000.0,
                "harvestOperationsExpenses": 60000.0
            }
        """.trimIndent()

        val response = client.post("/api/v1/revenue-split") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(requestJson)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("netFactoryPayout"))
        assertTrue(body.contains("390000"))
    }

    @Test
    fun testTriggerOverdueNotificationCheck() = testApplication {
        val repo = InMemoryRepositoryProvider()
        application {
            module(repo, enableLegacyFinanceRoutes = true)
        }

        val response = client.post("/api/v1/notifications/trigger-check?asOfDate=2026-11-02")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("dispatchedCount"))
    }

    @Test
    fun projectMembershipAndFinanceAreScopedToAuthenticatedUsers() = testApplication {
        val projectRepository = InMemoryProjectRepository()
        application {
            module(
                projectRepository = projectRepository,
                authTokenVerifier = testVerifier
            )
        }

        assertEquals(HttpStatusCode.Unauthorized, client.get("/api/v1/me/projects").status)

        suspend fun register(token: String, username: String) = client.post("/api/v1/me/profile") {
            header(HttpHeaders.Authorization, "Bearer $token")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"username":"$username","displayName":"${username.replaceFirstChar { it.uppercase() }}"}""")
        }

        assertEquals(HttpStatusCode.Created, register("owner-token", "owner").status)
        assertEquals(HttpStatusCode.Created, register("partner-token", "partner").status)
        assertEquals(HttpStatusCode.Created, register("outsider-token", "outsider").status)
        assertEquals(HttpStatusCode.Conflict, register("new-user-token", "partner").status)

        val projectResponse = client.post("/api/v1/projects") {
            header(HttpHeaders.Authorization, "Bearer owner-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"name":"Community Garden","description":"Shared land"}""")
        }
        assertEquals(HttpStatusCode.Created, projectResponse.status)
        val project = json.decodeFromString<Project>(projectResponse.bodyAsText())

        val ownerProjects = client.get("/api/v1/me/projects") {
            header(HttpHeaders.Authorization, "Bearer owner-token")
        }
        assertEquals(HttpStatusCode.OK, ownerProjects.status)
        assertTrue(ownerProjects.bodyAsText().contains(project.id))

        assertEquals(
            HttpStatusCode.NotFound,
            client.get("/api/v1/projects/${project.id}") {
                header(HttpHeaders.Authorization, "Bearer outsider-token")
            }.status
        )
        assertEquals(
            HttpStatusCode.Unauthorized,
            client.get("/api/v1/me/projects") {
                header(HttpHeaders.Authorization, "Bearer invalid-token")
            }.status
        )

        val inviteBody = """{"username":"partner"}"""
        val nonOwnerInvite = client.post("/api/v1/projects/${project.id}/invitations") {
            header(HttpHeaders.Authorization, "Bearer partner-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(inviteBody)
        }
        assertEquals(HttpStatusCode.Forbidden, nonOwnerInvite.status)

        val invitationResponse = client.post("/api/v1/projects/${project.id}/invitations") {
            header(HttpHeaders.Authorization, "Bearer owner-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(inviteBody)
        }
        assertEquals(HttpStatusCode.Created, invitationResponse.status)
        val invitation = json.decodeFromString<ProjectInvitation>(invitationResponse.bodyAsText())
        val duplicateInvite = client.post("/api/v1/projects/${project.id}/invitations") {
            header(HttpHeaders.Authorization, "Bearer owner-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(inviteBody)
        }
        assertEquals(HttpStatusCode.Conflict, duplicateInvite.status)

        val wrongUserResponse = client.post("/api/v1/invitations/${invitation.id}/respond") {
            header(HttpHeaders.Authorization, "Bearer outsider-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"accept":true}""")
        }
        assertEquals(HttpStatusCode.Forbidden, wrongUserResponse.status)

        val acceptResponse = client.post("/api/v1/invitations/${invitation.id}/respond") {
            header(HttpHeaders.Authorization, "Bearer partner-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"accept":true}""")
        }
        assertEquals(HttpStatusCode.OK, acceptResponse.status)
        val partnerProjects = json.decodeFromString<MyProjectsResponse>(
            client.get("/api/v1/me/projects") {
                header(HttpHeaders.Authorization, "Bearer partner-token")
            }.bodyAsText()
        )
        assertEquals(project.id, partnerProjects.defaultProjectId)

        val expenseResponse = client.post("/api/v1/projects/${project.id}/expenses") {
            header(HttpHeaders.Authorization, "Bearer owner-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(
                """{"title":"Seeds","category":"SEEDLINGS","totalAmount":100.0,"transactionDate":"2026-04-03","payingPartnerId":"uid-owner"}"""
            )
        }
        assertEquals(HttpStatusCode.Created, expenseResponse.status)
        assertTrue(expenseResponse.bodyAsText().contains("uid-partner"))

        val secondProjectResponse = client.post("/api/v1/projects") {
            header(HttpHeaders.Authorization, "Bearer outsider-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody("""{"name":"Art Collective"}""")
        }
        assertEquals(HttpStatusCode.Created, secondProjectResponse.status)
        val secondProject = json.decodeFromString<Project>(secondProjectResponse.bodyAsText())
        val secondInvitation = client.post("/api/v1/projects/${secondProject.id}/invitations") {
            header(HttpHeaders.Authorization, "Bearer outsider-token")
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(inviteBody)
        }
        assertEquals(HttpStatusCode.Created, secondInvitation.status)
        val secondInvitationId =
            json.decodeFromString<ProjectInvitation>(secondInvitation.bodyAsText()).id
        val acceptSecondInvitation =
            client.post("/api/v1/invitations/$secondInvitationId/respond") {
                header(HttpHeaders.Authorization, "Bearer partner-token")
                header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                setBody("""{"accept":true}""")
            }
        assertEquals(HttpStatusCode.OK, acceptSecondInvitation.status)

        val partnerProjectsWithMultipleMemberships = json.decodeFromString<MyProjectsResponse>(
            client.get("/api/v1/me/projects") {
                header(HttpHeaders.Authorization, "Bearer partner-token")
            }.bodyAsText()
        )
        assertEquals(2, partnerProjectsWithMultipleMemberships.projects.size)
        assertEquals(null, partnerProjectsWithMultipleMemberships.defaultProjectId)

        val secondProjectExpenses = client.get("/api/v1/projects/${secondProject.id}/expenses") {
            header(HttpHeaders.Authorization, "Bearer partner-token")
        }
        assertEquals(HttpStatusCode.OK, secondProjectExpenses.status)
        assertEquals("[]", secondProjectExpenses.bodyAsText())

        assertEquals(
            HttpStatusCode.NotFound,
            client.get("/api/v1/projects/${secondProject.id}") {
                header(HttpHeaders.Authorization, "Bearer owner-token")
            }.status
        )

        val outsiderExpenses = client.get("/api/v1/projects/${project.id}/expenses") {
            header(HttpHeaders.Authorization, "Bearer outsider-token")
        }
        assertEquals(HttpStatusCode.NotFound, outsiderExpenses.status)

        val legacyGlobalDashboard = client.get("/api/v1/dashboard") {
            header(HttpHeaders.Authorization, "Bearer owner-token")
        }
        assertEquals(HttpStatusCode.NotFound, legacyGlobalDashboard.status)
    }
}
