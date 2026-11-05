package co.ke.xently.expense.tracker.server.routes

import co.ke.xently.expense.tracker.calculator.LedgerCalculator
import co.ke.xently.expense.tracker.calculator.RevenueSplitCalculator
import co.ke.xently.expense.tracker.model.CreateProjectRequest
import co.ke.xently.expense.tracker.model.Expense
import co.ke.xently.expense.tracker.model.ExpenseCategory
import co.ke.xently.expense.tracker.model.InviteProjectPartnerRequest
import co.ke.xently.expense.tracker.model.MyProjectsResponse
import co.ke.xently.expense.tracker.model.Partner
import co.ke.xently.expense.tracker.model.ProjectMembership
import co.ke.xently.expense.tracker.model.ProjectUser
import co.ke.xently.expense.tracker.model.RegistrationRequest
import co.ke.xently.expense.tracker.model.RespondToInvitationRequest
import co.ke.xently.expense.tracker.model.TranchePayment
import co.ke.xently.expense.tracker.server.auth.AuthTokenVerifier
import co.ke.xently.expense.tracker.server.auth.AuthenticatedUser
import co.ke.xently.expense.tracker.server.repository.InvitationResponseStatus
import co.ke.xently.expense.tracker.server.repository.InviteStatus
import co.ke.xently.expense.tracker.server.repository.ProjectRepository
import co.ke.xently.expense.tracker.server.repository.RegistrationStatus
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import java.util.UUID
import kotlin.time.Clock

@Serializable
data class ProjectMemberResponse(
    val user: ProjectUser,
    val membership: ProjectMembership
)

@Serializable
data class ProjectRevenueSplitRequest(
    val grossFactoryPayout: Double,
    val harvestOperationsExpenses: Double
)

fun Route.projectRoutes(
    projectRepository: ProjectRepository,
    authTokenVerifier: AuthTokenVerifier
) {
    route("/api/v1") {
        post("/me/profile") {
            val authenticatedUser = call.authenticatedUser(authTokenVerifier)
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Unauthorized")
                )
            val request = call.receive<RegistrationRequest>()
            val username = request.username.trim().lowercase()
            val displayName = request.displayName.trim()
            if (!username.matches(Regex("[a-z0-9_][a-z0-9_-]{2,31}")) || displayName.isBlank()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Username must be 3-32 characters; display name is required")
                )
            }
            val result = projectRepository.registerUser(
                ProjectUser(
                    id = authenticatedUser.id,
                    username = username,
                    displayName = displayName,
                    email = authenticatedUser.email
                )
            )
            when (result.status) {
                RegistrationStatus.CREATED, RegistrationStatus.ALREADY_REGISTERED ->
                    call.respond(
                        if (result.status == RegistrationStatus.CREATED) HttpStatusCode.Created else HttpStatusCode.OK,
                        result.user!!
                    )

                RegistrationStatus.USERNAME_TAKEN ->
                    call.respond(
                        HttpStatusCode.Conflict,
                        mapOf("error" to "Username is already registered")
                    )
            }
        }

        get("/me/profile") {
            val user = call.authenticatedUser(authTokenVerifier)
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Unauthorized")
                )
            val profile = projectRepository.getUserById(user.id)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Profile not registered")
                )
            call.respond(profile)
        }

        get("/me/projects") {
            val user = call.authenticatedUser(authTokenVerifier)
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Unauthorized")
                )
            val projects = projectRepository.getProjectsForUser(user.id)
            call.respond(MyProjectsResponse(projects, projects.singleOrNull()?.id))
        }

        get("/me/invitations") {
            val user = call.authenticatedUser(authTokenVerifier)
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Unauthorized")
                )
            call.respond(projectRepository.getInvitationsForUser(user.id))
        }

        post("/projects") {
            val user = call.authenticatedUser(authTokenVerifier)
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Unauthorized")
                )
            if (projectRepository.getUserById(user.id) == null) {
                return@post call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("error" to "Register a username before creating a project")
                )
            }
            val request = call.receive<CreateProjectRequest>()
            if (request.name.isBlank()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Project name is required")
                )
            }
            val project =
                projectRepository.createProject(user.id, request.name, request.description)
                    ?: return@post call.respond(
                        HttpStatusCode.Conflict,
                        mapOf("error" to "Project owner profile not found")
                    )
            call.respond(HttpStatusCode.Created, project)
        }

        get("/projects/{projectId}") {
            val user = call.authenticatedUser(authTokenVerifier)
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Unauthorized")
                )
            val projectId = call.parameters["projectId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Project id is required")
                )
            val project = projectRepository.getProjectForMember(projectId, user.id)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Project not found")
                )
            call.respond(project)
        }

        get("/projects/{projectId}/members") {
            val user = call.authenticatedUser(authTokenVerifier)
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Unauthorized")
                )
            val projectId = call.parameters["projectId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Project id is required")
                )
            val members = projectRepository.getMemberships(projectId, user.id)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Project not found")
                )
            call.respond(members.map { (membership, profile) ->
                ProjectMemberResponse(
                    profile,
                    membership
                )
            })
        }

        post("/projects/{projectId}/invitations") {
            val user = call.authenticatedUser(authTokenVerifier)
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Unauthorized")
                )
            val projectId = call.parameters["projectId"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Project id is required")
                )
            val request = call.receive<InviteProjectPartnerRequest>()
            if (request.username.isBlank()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Username is required")
                )
            }
            val result = projectRepository.invitePartner(projectId, user.id, request.username)
            when (result.status) {
                InviteStatus.CREATED -> call.respond(HttpStatusCode.Created, result.invitation!!)
                InviteStatus.NOT_OWNER -> call.respond(
                    HttpStatusCode.Forbidden,
                    mapOf("error" to "Only the project owner can invite partners")
                )

                InviteStatus.PROJECT_NOT_FOUND -> call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Project not found")
                )

                InviteStatus.USER_NOT_FOUND -> call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Registered username not found")
                )

                InviteStatus.ALREADY_MEMBER -> call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("error" to "User is already a project partner")
                )

                InviteStatus.DUPLICATE_PENDING -> call.respond(
                    HttpStatusCode.Conflict,
                    mapOf("error" to "Invitation is already pending")
                )

                InviteStatus.SELF_INVITE -> call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Project owner is already a member")
                )
            }
        }

        post("/invitations/{invitationId}/respond") {
            val user = call.authenticatedUser(authTokenVerifier)
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Unauthorized")
                )
            val invitationId = call.parameters["invitationId"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Invitation id is required")
                )
            val request = call.receive<RespondToInvitationRequest>()
            val result =
                projectRepository.respondToInvitation(invitationId, user.id, request.accept)
            when (result.status) {
                InvitationResponseStatus.UPDATED -> call.respond(
                    HttpStatusCode.OK,
                    result.invitation!!
                )

                InvitationResponseStatus.NOT_FOUND -> call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Invitation not found")
                )

                InvitationResponseStatus.NOT_INVITEE -> call.respond(
                    HttpStatusCode.Forbidden,
                    mapOf("error" to "Invitation belongs to another user")
                )

                InvitationResponseStatus.ALREADY_RESPONDED -> call.respond(
                    HttpStatusCode.Conflict,
                    result.invitation!!
                )
            }
        }

        get("/projects/{projectId}/expenses") {
            val user = call.authenticatedUser(authTokenVerifier)
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Unauthorized")
                )
            val projectId = call.parameters["projectId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Project id is required")
                )
            val expenses = projectRepository.getExpenses(projectId, user.id)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Project not found")
                )
            call.respond(expenses)
        }

        post("/projects/{projectId}/expenses") {
            val user = call.authenticatedUser(authTokenVerifier)
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Unauthorized")
                )
            val projectId = call.parameters["projectId"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Project id is required")
                )
            val request = call.receive<CreateExpenseRequest>()
            if (request.title.isBlank() || request.totalAmount <= 0.0 || !request.totalAmount.isFinite()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Title and a positive finite amount are required")
                )
            }
            val transactionDate =
                runCatching { LocalDate.parse(request.transactionDate) }.getOrNull()
                    ?: return@post call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to "Transaction date must be YYYY-MM-DD")
                    )
            val projectMembers = projectRepository.getMemberships(projectId, user.id)
                ?: return@post call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Project not found")
                )
            if (request.payingPartnerId !in projectMembers.map { it.second.id }) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Paying partner must be a project member")
                )
            }
            val expense = Expense(
                id = "exp-${UUID.randomUUID()}",
                title = request.title.trim(),
                category = ExpenseCategory.fromString(request.category),
                totalAmount = request.totalAmount,
                currency = request.currency,
                transactionDate = transactionDate,
                payingPartnerId = request.payingPartnerId,
                notes = request.notes,
                createdAtEpochMs = System.currentTimeMillis()
            )
            val saved = projectRepository.createExpense(projectId, user.id, expense)
                ?: return@post call.respond(
                    HttpStatusCode.Forbidden,
                    mapOf("error" to "Project access changed; retry the request")
                )
            call.respond(HttpStatusCode.Created, saved)
        }

        delete("/projects/{projectId}/expenses/{expenseId}") {
            val user = call.authenticatedUser(authTokenVerifier)
                ?: return@delete call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Unauthorized")
                )
            val projectId = call.parameters["projectId"]
                ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Project id is required")
                )
            val expenseId = call.parameters["expenseId"]
                ?: return@delete call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Expense id is required")
                )
            val deleted = projectRepository.deleteExpense(projectId, user.id, expenseId)
                ?: return@delete call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Project not found")
                )
            if (deleted) call.respond(HttpStatusCode.OK, mapOf("deleted" to true))
            else call.respond(HttpStatusCode.NotFound, mapOf("error" to "Expense not found"))
        }

        get("/projects/{projectId}/tranches") {
            val user = call.authenticatedUser(authTokenVerifier)
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Unauthorized")
                )
            val projectId = call.parameters["projectId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Project id is required")
                )
            val tranches = projectRepository.getTranches(projectId, user.id)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Project not found")
                )
            call.respond(tranches)
        }

        post("/projects/{projectId}/tranches") {
            val user = call.authenticatedUser(authTokenVerifier)
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Unauthorized")
                )
            val projectId = call.parameters["projectId"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Project id is required")
                )
            val request = call.receive<CreateTrancheRequest>()
            if (request.amount <= 0.0 || !request.amount.isFinite()) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "A positive finite amount is required")
                )
            }
            if (request.partnerId != user.id) {
                return@post call.respond(
                    HttpStatusCode.Forbidden,
                    mapOf("error" to "Partners can only record their own contributions")
                )
            }
            val paymentDate = runCatching { LocalDate.parse(request.paymentDate) }.getOrNull()
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Payment date must be YYYY-MM-DD")
                )
            val payment = TranchePayment(
                id = "tr-${UUID.randomUUID()}",
                partnerId = user.id,
                amount = request.amount,
                paymentDate = paymentDate,
                notes = request.notes,
                referenceNumber = request.referenceNumber,
                createdAtEpochMs = System.currentTimeMillis()
            )
            val saved = projectRepository.createTranche(projectId, user.id, payment)
                ?: return@post call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Project not found")
                )
            call.respond(HttpStatusCode.Created, saved)
        }

        get("/projects/{projectId}/dashboard") {
            val user = call.authenticatedUser(authTokenVerifier)
                ?: return@get call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Unauthorized")
                )
            val projectId = call.parameters["projectId"]
                ?: return@get call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Project id is required")
                )
            val members = projectRepository.getMemberships(projectId, user.id)
                ?: return@get call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Project not found")
                )
            val expenses = projectRepository.getExpenses(projectId, user.id).orEmpty()
            val tranches = projectRepository.getTranches(projectId, user.id).orEmpty()
            val partners = members.map { (_, profile) ->
                Partner(id = profile.id, name = profile.displayName, email = profile.email)
            }
            val date =
                call.request.queryParameters["asOfDate"]?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                    ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            call.respond(
                LedgerCalculator.computeDashboardSummary(
                    expenses = expenses,
                    tranches = tranches,
                    notifications = emptyList(),
                    partners = partners,
                    asOfDate = date
                )
            )
        }

        post("/projects/{projectId}/revenue-split") {
            val user = call.authenticatedUser(authTokenVerifier)
                ?: return@post call.respond(
                    HttpStatusCode.Unauthorized,
                    mapOf("error" to "Unauthorized")
                )
            val projectId = call.parameters["projectId"]
                ?: return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Project id is required")
                )
            val members = projectRepository.getMemberships(projectId, user.id)
                ?: return@post call.respond(
                    HttpStatusCode.NotFound,
                    mapOf("error" to "Project not found")
                )
            val request = call.receive<ProjectRevenueSplitRequest>()
            if (request.grossFactoryPayout < 0.0 || request.harvestOperationsExpenses < 0.0 ||
                !request.grossFactoryPayout.isFinite() || !request.harvestOperationsExpenses.isFinite()
            ) {
                return@post call.respond(
                    HttpStatusCode.BadRequest,
                    mapOf("error" to "Payout values must be finite and non-negative")
                )
            }
            val expenses = projectRepository.getExpenses(projectId, user.id).orEmpty()
            val tranches = projectRepository.getTranches(projectId, user.id).orEmpty()
            val partners = members.map { (_, profile) ->
                Partner(id = profile.id, name = profile.displayName, email = profile.email)
            }
            val date = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
            val ledger =
                LedgerCalculator.computePartnerSummaries(expenses, tranches, partners, date)
            call.respond(
                RevenueSplitCalculator.calculate(
                    request.grossFactoryPayout,
                    request.harvestOperationsExpenses,
                    ledger
                )
            )
        }
    }
}

private suspend fun ApplicationCall.authenticatedUser(verifier: AuthTokenVerifier): AuthenticatedUser? {
    val authorization = request.headers[HttpHeaders.Authorization] ?: return null
    val parts = authorization.split(' ', limit = 2)
    if (parts.size != 2 || !parts[0].equals(
            "Bearer",
            ignoreCase = true
        ) || parts[1].isBlank()
    ) return null
    return verifier.verify(parts[1])
}