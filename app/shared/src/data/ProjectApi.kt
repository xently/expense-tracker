package co.ke.xently.expense.tracker.data

import co.ke.xently.expense.tracker.model.CreateProjectRequest
import co.ke.xently.expense.tracker.model.Expense
import co.ke.xently.expense.tracker.model.InviteProjectPartnerRequest
import co.ke.xently.expense.tracker.model.MyProjectsResponse
import co.ke.xently.expense.tracker.model.Project
import co.ke.xently.expense.tracker.model.ProjectInvitation
import co.ke.xently.expense.tracker.model.ProjectUser
import co.ke.xently.expense.tracker.model.RegistrationRequest
import co.ke.xently.expense.tracker.model.RespondToInvitationRequest
import co.ke.xently.expense.tracker.model.SeasonRevenueCalculation
import co.ke.xently.expense.tracker.model.TranchePayment
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val DEMO_ACCESS_TOKEN = "local-demo-token"

expect fun defaultDemoServiceUrl(): String
expect fun defaultHttpClientEngine(): HttpClientEngineFactory<*>

@Serializable
data class ProjectMemberResponse(
    val user: ProjectUser,
    val membership: co.ke.xently.expense.tracker.model.ProjectMembership
)

@Serializable
data class CreateExpensePayload(
    val title: String,
    val category: String,
    val categoryName: String? = null,
    val totalAmount: Double,
    val currency: String = "KES",
    val transactionDate: String,
    val payingPartnerId: String,
    val notes: String = ""
)

@Serializable
data class CreateTranchePayload(
    val partnerId: String,
    val amount: Double,
    val paymentDate: String,
    val notes: String = "",
    val referenceNumber: String = ""
)

@Serializable
data class RevenueSplitPayload(
    val grossFactoryPayout: Double,
    val harvestOperationsExpenses: Double
)

interface ProjectApi {
    suspend fun getProfile(baseUrl: String, token: String): ProjectUser
    suspend fun register(
        baseUrl: String,
        token: String,
        username: String,
        displayName: String
    ): ProjectUser

    suspend fun getProjects(baseUrl: String, token: String): MyProjectsResponse
    suspend fun getInvitations(baseUrl: String, token: String): List<ProjectInvitation>
    suspend fun createProject(
        baseUrl: String,
        token: String,
        name: String,
        description: String,
        expenseCategories: List<String> = emptyList()
    ): Project

    suspend fun invite(
        baseUrl: String,
        token: String,
        projectId: String,
        username: String
    ): ProjectInvitation

    suspend fun respondToInvitation(
        baseUrl: String,
        token: String,
        invitationId: String,
        accept: Boolean
    ): ProjectInvitation

    suspend fun getMembers(
        baseUrl: String,
        token: String,
        projectId: String
    ): List<ProjectMemberResponse>

    suspend fun getExpenses(baseUrl: String, token: String, projectId: String): List<Expense>
    suspend fun createExpense(
        baseUrl: String,
        token: String,
        projectId: String,
        payload: CreateExpensePayload
    ): Expense

    suspend fun deleteExpense(baseUrl: String, token: String, projectId: String, expenseId: String)
    suspend fun getTranches(baseUrl: String, token: String, projectId: String): List<TranchePayment>
    suspend fun createTranche(
        baseUrl: String,
        token: String,
        projectId: String,
        payload: CreateTranchePayload
    ): TranchePayment

    suspend fun calculateRevenueSplit(
        baseUrl: String,
        token: String,
        projectId: String,
        payload: RevenueSplitPayload
    ): SeasonRevenueCalculation
}

class ApiException(val statusCode: Int, message: String) : Exception(message)

class KtorProjectApi(
    private val client: HttpClient = HttpClient(defaultHttpClientEngine()) {
        expectSuccess = false
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = println(message)
            }
            level = LogLevel.HEADERS
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                explicitNulls = false
            })
        }
    }
) : ProjectApi {
    override suspend fun getProfile(baseUrl: String, token: String): ProjectUser =
        client.get(url(baseUrl, "/me/profile")) { authorize(token) }.decode()

    override suspend fun register(
        baseUrl: String,
        token: String,
        username: String,
        displayName: String
    ): ProjectUser = client.post(url(baseUrl, "/me/profile")) {
        authorize(token)
        jsonBody(RegistrationRequest(username, displayName))
    }.decode()

    override suspend fun getProjects(baseUrl: String, token: String): MyProjectsResponse =
        client.get(url(baseUrl, "/me/projects")) { authorize(token) }.decode()

    override suspend fun getInvitations(baseUrl: String, token: String): List<ProjectInvitation> =
        client.get(url(baseUrl, "/me/invitations")) { authorize(token) }.decode()

    override suspend fun createProject(
        baseUrl: String,
        token: String,
        name: String,
        description: String,
        expenseCategories: List<String>
    ): Project = client.post(url(baseUrl, "/projects")) {
        authorize(token)
        jsonBody(CreateProjectRequest(name, description, expenseCategories))
    }.decode()

    override suspend fun invite(
        baseUrl: String,
        token: String,
        projectId: String,
        username: String
    ): ProjectInvitation = client.post(url(baseUrl, "/projects/$projectId/invitations")) {
        authorize(token)
        jsonBody(InviteProjectPartnerRequest(username))
    }.decode()

    override suspend fun respondToInvitation(
        baseUrl: String,
        token: String,
        invitationId: String,
        accept: Boolean
    ): ProjectInvitation = client.post(url(baseUrl, "/invitations/$invitationId/respond")) {
        authorize(token)
        jsonBody(RespondToInvitationRequest(accept))
    }.decode()

    override suspend fun getMembers(
        baseUrl: String,
        token: String,
        projectId: String
    ): List<ProjectMemberResponse> =
        client.get(url(baseUrl, "/projects/$projectId/members")) { authorize(token) }.decode()

    override suspend fun getExpenses(
        baseUrl: String,
        token: String,
        projectId: String
    ): List<Expense> =
        client.get(url(baseUrl, "/projects/$projectId/expenses")) { authorize(token) }.decode()

    override suspend fun createExpense(
        baseUrl: String,
        token: String,
        projectId: String,
        payload: CreateExpensePayload
    ): Expense = client.post(url(baseUrl, "/projects/$projectId/expenses")) {
        authorize(token)
        jsonBody(payload)
    }.decode()

    override suspend fun deleteExpense(
        baseUrl: String,
        token: String,
        projectId: String,
        expenseId: String
    ) {
        client.delete(url(baseUrl, "/projects/$projectId/expenses/$expenseId")) {
            authorize(token)
        }.decode<Unit>()
    }

    override suspend fun getTranches(
        baseUrl: String,
        token: String,
        projectId: String
    ): List<TranchePayment> =
        client.get(url(baseUrl, "/projects/$projectId/tranches")) { authorize(token) }.decode()

    override suspend fun createTranche(
        baseUrl: String,
        token: String,
        projectId: String,
        payload: CreateTranchePayload
    ): TranchePayment = client.post(url(baseUrl, "/projects/$projectId/tranches")) {
        authorize(token)
        jsonBody(payload)
    }.decode()

    override suspend fun calculateRevenueSplit(
        baseUrl: String,
        token: String,
        projectId: String,
        payload: RevenueSplitPayload
    ): SeasonRevenueCalculation = client.post(url(baseUrl, "/projects/$projectId/revenue-split")) {
        authorize(token)
        jsonBody(payload)
    }.decode()

    private fun io.ktor.client.request.HttpRequestBuilder.authorize(token: String) {
        header(HttpHeaders.Authorization, "Bearer $token")
    }

    private fun io.ktor.client.request.HttpRequestBuilder.jsonBody(body: Any) {
        contentType(ContentType.Application.Json)
        setBody(body)
    }

    private suspend inline fun <reified T> HttpResponse.decode(): T {
        if (status == HttpStatusCode.NotFound) throw ApiException(status.value, "Not found")
        if (status.value !in 200..299) throw ApiException(
            status.value,
            "Request failed (${status.value})"
        )
        return if (status == HttpStatusCode.NoContent || T::class == Unit::class) Unit as T else body()
    }

    private fun url(baseUrl: String, path: String): String =
        "${baseUrl.trim().trimEnd('/')}/api/v1$path"
}