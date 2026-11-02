package co.ke.xently.expense.tracker.server

import co.ke.xently.expense.tracker.model.DashboardSummary
import co.ke.xently.expense.tracker.model.PartnerId
import co.ke.xently.expense.tracker.server.repository.InMemoryRepositoryProvider
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

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
        assertTrue(openApiResponse.bodyAsText().contains("openapi"))
        assertTrue(openApiResponse.bodyAsText().contains("Sugarcane Season Expense Tracker API"))

        val swaggerUiResponse = client.get("/api/v1/swagger")
        assertEquals(HttpStatusCode.OK, swaggerUiResponse.status)
        assertTrue(swaggerUiResponse.bodyAsText().contains("SwaggerUIBundle"))
    }

    @Test
    fun testDashboardAndLedgers() = testApplication {
        val repo = InMemoryRepositoryProvider()
        application {
            module(repo)
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
            module(repo)
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
            module(repo)
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
            module(repo)
        }

        val response = client.post("/api/v1/notifications/trigger-check?asOfDate=2026-11-02")
        assertEquals(HttpStatusCode.OK, response.status)
        val body = response.bodyAsText()
        assertTrue(body.contains("dispatchedCount"))
    }
}
