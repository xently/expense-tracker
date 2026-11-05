package co.ke.xently.expense.tracker.server.routes

import co.ke.xently.expense.tracker.calculator.ExpenseCalculator
import co.ke.xently.expense.tracker.calculator.LedgerCalculator
import co.ke.xently.expense.tracker.calculator.RevenueSplitCalculator
import co.ke.xently.expense.tracker.model.AppNotification
import co.ke.xently.expense.tracker.model.Expense
import co.ke.xently.expense.tracker.model.ExpenseCategory
import co.ke.xently.expense.tracker.model.ServerEvent
import co.ke.xently.expense.tracker.model.TranchePayment
import co.ke.xently.expense.tracker.server.notification.OverdueNotificationManager
import co.ke.xently.expense.tracker.server.repository.RepositoryProvider
import co.ke.xently.expense.tracker.server.sse.EventBroadcaster
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.sse.sse
import io.ktor.sse.ServerSentEvent
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlin.time.Clock

@Serializable
data class CreateExpenseRequest(
    val title: String,
    val category: String,
    val totalAmount: Double,
    val currency: String = "KES",
    val transactionDate: String, // ISO date string e.g. "2026-05-15"
    val payingPartnerId: String,
    val notes: String = ""
)

@Serializable
data class CreateTrancheRequest(
    val partnerId: String,
    val amount: Double,
    val paymentDate: String, // ISO date string e.g. "2026-05-20"
    val notes: String = "",
    val referenceNumber: String = ""
)

@Serializable
data class RevenueSplitRequest(
    val grossFactoryPayout: Double,
    val harvestOperationsExpenses: Double
)

@Serializable
data class HealthResponse(
    val status: String,
    val service: String
)

@Serializable
data class DeleteResponse(
    val deleted: Boolean
)

@Serializable
data class TriggerCheckResponse(
    val dispatchedCount: Int,
    val notifications: List<AppNotification>
)

fun Route.apiRoutes(
    repositoryProvider: RepositoryProvider,
    eventBroadcaster: EventBroadcaster,
    overdueNotificationManager: OverdueNotificationManager,
    enableLegacyFinanceRoutes: Boolean = false
) {
    route("/api/v1") {
        get("/health") {
            call.respond(HealthResponse(status = "healthy", service = "sugarcane-expense-tracker"))
        }

        // OpenAPI / Swagger Documentation Endpoints
        get("/openapi.json") {
            call.respondText(OPENAPI_SPEC_JSON, ContentType.Application.Json)
        }

        get("/swagger") {
            // language=HTML
            val html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                  <meta charset="UTF-8">
                  <title>Sugarcane Expense Tracker - Swagger UI</title>
                  <link rel="stylesheet" href="https://unpkg.com/swagger-ui-dist@5/swagger-ui.css" />
                  <style>
                    body { margin: 0; background: #fafafa; }
                    .topbar { display: none; }
                  </style>
                </head>
                <body>
                  <div id="swagger-ui"></div>
                  <script src="https://unpkg.com/swagger-ui-dist@5/swagger-ui-bundle.js"></script>
                  <script>
                    window.onload = function() {
                      SwaggerUIBundle({
                        url: "/api/v1/openapi.json",
                        dom_id: '#swagger-ui',
                        deepLinking: true,
                        presets: [
                          SwaggerUIBundle.presets.apis,
                          SwaggerUIBundle.SwaggerUIStandalonePreset
                        ],
                        layout: "BaseLayout"
                      });
                    };
                  </script>
                </body>
                </html>
            """.trimIndent()
            call.respondText(html, ContentType.Text.Html)
        }

        if (enableLegacyFinanceRoutes) {
            // Dashboard Summary Endpoint
            get("/dashboard") {
                val asOfDateParam = call.request.queryParameters["asOfDate"]
                val asOfDate = if (!asOfDateParam.isNullOrBlank()) {
                    runCatching { LocalDate.parse(asOfDateParam) }
                        .getOrDefault(LocalDate(year = 2026, month = 11, day = 2))
                } else {
                    LocalDate(year = 2026, month = 11, day = 2)
                }

                val expenses = repositoryProvider.expenseRepository.getAll()
                val tranches = repositoryProvider.trancheRepository.getAll()
                val notifications = repositoryProvider.notificationRepository.getAll()
                val partners = repositoryProvider.partnerRepository.getAll()

                val summary = LedgerCalculator.computeDashboardSummary(
                    expenses = expenses,
                    tranches = tranches,
                    notifications = notifications,
                    partners = partners,
                    asOfDate = asOfDate
                )
                call.respond(summary)
            }

            // Expenses Routes
            route("/expenses") {
                get {
                    val expenses = repositoryProvider.expenseRepository.getAll()
                    call.respond(expenses)
                }

                get("/{id}") {
                    val id = call.parameters["id"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        "Missing id"
                    )
                    val expense = repositoryProvider.expenseRepository.getById(id)
                    if (expense != null) {
                        call.respond(expense)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Expense not found")
                    }
                }

                post {
                    val req = call.receive<CreateExpenseRequest>()
                    val txDate = runCatching { LocalDate.parse(req.transactionDate) }.getOrElse {
                        currentLocalDateTime().date
                    }
                    val category = ExpenseCategory.fromString(req.category)
                    val partners = repositoryProvider.partnerRepository.getAll()
                    val splits =
                        ExpenseCalculator.calculateSplits(req.totalAmount, txDate, partners)

                    val expense = Expense(
                        id = "exp-${UUID.randomUUID().toString().take(8)}",
                        title = req.title,
                        category = category,
                        totalAmount = req.totalAmount,
                        currency = req.currency,
                        transactionDate = txDate,
                        payingPartnerId = req.payingPartnerId,
                        notes = req.notes,
                        splits = splits,
                        createdAtEpochMs = System.currentTimeMillis()
                    )

                    val saved = repositoryProvider.expenseRepository.create(expense)
                    eventBroadcaster.broadcast(ServerEvent.ExpenseCreated(saved))
                    call.respond(HttpStatusCode.Created, saved)
                }

                delete("/{id}") {
                    val id = call.parameters["id"] ?: return@delete call.respond(
                        HttpStatusCode.BadRequest,
                        "Missing id"
                    )
                    val deleted = repositoryProvider.expenseRepository.delete(id)
                    if (deleted) {
                        eventBroadcaster.broadcast(ServerEvent.ExpenseDeleted(id))
                        call.respond(HttpStatusCode.OK, DeleteResponse(deleted = true))
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Expense not found")
                    }
                }
            }

            // Tranches Routes
            route("/tranches") {
                get {
                    val partnerId = call.request.queryParameters["partnerId"]
                    val tranches = if (!partnerId.isNullOrBlank()) {
                        repositoryProvider.trancheRepository.getByPartnerId(partnerId)
                    } else {
                        repositoryProvider.trancheRepository.getAll()
                    }
                    call.respond(tranches)
                }

                post {
                    val req = call.receive<CreateTrancheRequest>()
                    val payDate = runCatching { LocalDate.parse(req.paymentDate) }.getOrElse {
                        currentLocalDateTime().date
                    }

                    val tranche = TranchePayment(
                        id = "tr-${UUID.randomUUID().toString().take(8)}",
                        partnerId = req.partnerId,
                        amount = req.amount,
                        paymentDate = payDate,
                        notes = req.notes,
                        referenceNumber = req.referenceNumber,
                        createdAtEpochMs = System.currentTimeMillis()
                    )

                    val saved = repositoryProvider.trancheRepository.create(tranche)
                    eventBroadcaster.broadcast(ServerEvent.TranchePaymentRecorded(saved))
                    call.respond(HttpStatusCode.Created, saved)
                }
            }

            // Partners Routes
            route("/partners") {
                get {
                    val partners = repositoryProvider.partnerRepository.getAll()
                    call.respond(partners)
                }

                get("/{id}/ledger") {
                    val id = call.parameters["id"] ?: return@get call.respond(
                        HttpStatusCode.BadRequest,
                        "Missing partner id"
                    )
                    val asOfDateParam = call.request.queryParameters["asOfDate"]
                    val asOfDate = if (!asOfDateParam.isNullOrBlank()) {
                        runCatching { LocalDate.parse(asOfDateParam) }.getOrDefault(
                            LocalDate(
                                2026,
                                11,
                                2
                            )
                        )
                    } else {
                        LocalDate(2026, 11, 2)
                    }

                    val expenses = repositoryProvider.expenseRepository.getAll()
                    val tranches = repositoryProvider.trancheRepository.getAll()
                    val partners = repositoryProvider.partnerRepository.getAll()

                    val summaries = LedgerCalculator.computePartnerSummaries(
                        expenses = expenses,
                        tranches = tranches,
                        partners = partners,
                        asOfDate = asOfDate
                    )

                    val partnerSummary =
                        summaries.firstOrNull { it.partnerId.equals(id, ignoreCase = true) }
                    if (partnerSummary != null) {
                        call.respond(partnerSummary)
                    } else {
                        call.respond(HttpStatusCode.NotFound, "Partner summary not found")
                    }
                }
            }

            // Revenue Split Calculator Endpoint
            route("/revenue-split") {
                post {
                    val req = call.receive<RevenueSplitRequest>()
                    val expenses = repositoryProvider.expenseRepository.getAll()
                    val tranches = repositoryProvider.trancheRepository.getAll()
                    val partners = repositoryProvider.partnerRepository.getAll()

                    val summaries = LedgerCalculator.computePartnerSummaries(
                        expenses = expenses,
                        tranches = tranches,
                        partners = partners,
                        asOfDate = LocalDate(year = 2026, month = 11, day = 2)
                    )

                    val calculation = RevenueSplitCalculator.calculate(
                        grossFactoryPayout = req.grossFactoryPayout,
                        harvestOperationsExpenses = req.harvestOperationsExpenses,
                        partnerLedgerSummaries = summaries
                    )
                    call.respond(calculation)
                }
            }

            // Notifications & Alerts Routes
            route("/notifications") {
                get {
                    val partnerId = call.request.queryParameters["partnerId"]
                    val notifications = if (!partnerId.isNullOrBlank()) {
                        repositoryProvider.notificationRepository.getByPartnerId(partnerId)
                    } else {
                        repositoryProvider.notificationRepository.getAll()
                    }
                    call.respond(notifications)
                }

                post("/trigger-check") {
                    val asOfDateParam = call.request.queryParameters["asOfDate"]
                    val asOfDate = if (!asOfDateParam.isNullOrBlank()) {
                        runCatching { LocalDate.parse(asOfDateParam) }
                            .getOrDefault(LocalDate(year = 2026, month = 11, day = 2))
                    } else {
                        LocalDate(year = 2026, month = 11, day = 2)
                    }
                    val dispatched =
                        overdueNotificationManager.checkAndDispatchOverdueNotifications(asOfDate)
                    call.respond(
                        TriggerCheckResponse(
                            dispatchedCount = dispatched.size,
                            notifications = dispatched
                        )
                    )
                }
            }

            // Real-Time Server-Sent Events (SSE) Stream
            sse("/events/stream") {
                val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
                try {
                    // Send initial connection event
                    send(ServerSentEvent(data = """{"status":"connected"}""", event = "Connected"))
                    eventBroadcaster.events.collect { event ->
                        val eventName = event::class.simpleName ?: "ServerEvent"
                        val eventData = json.encodeToString(ServerEvent.serializer(), event)
                        send(ServerSentEvent(data = eventData, event = eventName))
                    }
                } catch (e: Exception) {
                    // Client disconnected
                }
            }
        }
    }
}

private fun currentLocalDateTime(): LocalDateTime =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
