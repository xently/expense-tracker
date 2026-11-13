package co.ke.xently.expense.tracker.server.repository

import co.ke.xently.expense.tracker.calculator.ExpenseCalculator
import co.ke.xently.expense.tracker.model.Expense
import co.ke.xently.expense.tracker.model.Project
import co.ke.xently.expense.tracker.model.ProjectInvitation
import co.ke.xently.expense.tracker.model.ProjectInvitationStatus
import co.ke.xently.expense.tracker.model.ProjectMemberRole
import co.ke.xently.expense.tracker.model.ProjectMembership
import co.ke.xently.expense.tracker.model.ProjectUser
import co.ke.xently.expense.tracker.model.TranchePayment
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.UUID

enum class RegistrationStatus {
    CREATED,
    ALREADY_REGISTERED,
    USERNAME_TAKEN
}

data class RegistrationResult(val status: RegistrationStatus, val user: ProjectUser?)

enum class InviteStatus {
    CREATED,
    NOT_OWNER,
    PROJECT_NOT_FOUND,
    USER_NOT_FOUND,
    ALREADY_MEMBER,
    DUPLICATE_PENDING,
    SELF_INVITE
}

data class InviteResult(val status: InviteStatus, val invitation: ProjectInvitation? = null)

enum class InvitationResponseStatus {
    UPDATED,
    NOT_FOUND,
    NOT_INVITEE,
    ALREADY_RESPONDED
}

data class InvitationResponseResult(
    val status: InvitationResponseStatus,
    val invitation: ProjectInvitation? = null
)

interface ProjectRepository {
    suspend fun registerUser(user: ProjectUser): RegistrationResult
    suspend fun getUserById(userId: String): ProjectUser?
    suspend fun getUserByUsername(username: String): ProjectUser?
    suspend fun createProject(
        ownerUserId: String,
        name: String,
        description: String,
        expenseCategories: List<String> = emptyList()
    ): Project?
    suspend fun getProjectsForUser(userId: String): List<Project>
    suspend fun getProjectForMember(projectId: String, userId: String): Project?
    suspend fun getMemberships(
        projectId: String,
        userId: String
    ): List<Pair<ProjectMembership, ProjectUser>>?

    suspend fun invitePartner(
        projectId: String,
        inviterUserId: String,
        username: String
    ): InviteResult

    suspend fun getInvitationsForUser(userId: String): List<ProjectInvitation>
    suspend fun respondToInvitation(
        invitationId: String,
        userId: String,
        accept: Boolean
    ): InvitationResponseResult

    suspend fun getExpenses(projectId: String, userId: String): List<Expense>?
    suspend fun createExpense(projectId: String, userId: String, expense: Expense): Expense?
    suspend fun deleteExpense(projectId: String, userId: String, expenseId: String): Boolean?
    suspend fun getTranches(projectId: String, userId: String): List<TranchePayment>?
    suspend fun createTranche(
        projectId: String,
        userId: String,
        payment: TranchePayment
    ): TranchePayment?
}

/** In-memory project store for development and tests; all authorization and mutation checks share one lock. */
class InMemoryProjectRepository : ProjectRepository {
    private val mutex = Mutex()
    private val users = mutableMapOf<String, ProjectUser>()
    private val userIdsByUsername = mutableMapOf<String, String>()
    private val projects = mutableMapOf<String, Project>()
    private val memberships = mutableMapOf<String, MutableMap<String, ProjectMembership>>()
    private val invitations = mutableMapOf<String, ProjectInvitation>()
    private val expenses = mutableMapOf<String, MutableMap<String, Expense>>()
    private val tranches = mutableMapOf<String, MutableMap<String, TranchePayment>>()

    override suspend fun registerUser(user: ProjectUser): RegistrationResult = mutex.withLock {
        users[user.id]?.let {
            return@withLock RegistrationResult(
                RegistrationStatus.ALREADY_REGISTERED,
                it
            )
        }
        val normalizedUsername = normalizeUsername(user.username)
        if (userIdsByUsername.containsKey(normalizedUsername)) {
            return@withLock RegistrationResult(RegistrationStatus.USERNAME_TAKEN, null)
        }
        val normalizedUser = user.copy(username = normalizedUsername)
        users[normalizedUser.id] = normalizedUser
        userIdsByUsername[normalizedUsername] = normalizedUser.id
        RegistrationResult(RegistrationStatus.CREATED, normalizedUser)
    }

    override suspend fun getUserById(userId: String): ProjectUser? =
        mutex.withLock { users[userId] }

    override suspend fun getUserByUsername(username: String): ProjectUser? = mutex.withLock {
        userIdsByUsername[normalizeUsername(username)]?.let(users::get)
    }

    override suspend fun createProject(
        ownerUserId: String,
        name: String,
        description: String,
        expenseCategories: List<String>
    ): Project? =
        mutex.withLock {
            if (ownerUserId !in users) return@withLock null
            val normalizedCategories =
                normalizeExpenseCategories(expenseCategories) ?: return@withLock null
            val project = Project(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                description = description.trim(),
                ownerUserId = ownerUserId,
                createdAtEpochMs = System.currentTimeMillis(),
                expenseCategories = normalizedCategories
            )
            projects[project.id] = project
            memberships.getOrPut(project.id, ::mutableMapOf)[ownerUserId] = ProjectMembership(
                projectId = project.id,
                userId = ownerUserId,
                role = ProjectMemberRole.OWNER,
                joinedAtEpochMs = project.createdAtEpochMs
            )
            project
        }

    override suspend fun getProjectsForUser(userId: String): List<Project> = mutex.withLock {
        memberships.values.mapNotNull { projectMembers ->
            if (userId in projectMembers) projects[projectMembers.getValue(userId).projectId] else null
        }.sortedBy { it.name.lowercase() }
    }

    override suspend fun getProjectForMember(projectId: String, userId: String): Project? =
        mutex.withLock {
            if (userId in memberships[projectId].orEmpty()) projects[projectId] else null
        }

    override suspend fun getMemberships(
        projectId: String,
        userId: String
    ): List<Pair<ProjectMembership, ProjectUser>>? = mutex.withLock {
        if (userId !in memberships[projectId].orEmpty()) return@withLock null
        memberships[projectId].orEmpty().values.mapNotNull { membership ->
            users[membership.userId]?.let { membership to it }
        }
    }

    override suspend fun invitePartner(
        projectId: String,
        inviterUserId: String,
        username: String
    ): InviteResult = mutex.withLock {
        val project =
            projects[projectId] ?: return@withLock InviteResult(InviteStatus.PROJECT_NOT_FOUND)
        if (project.ownerUserId != inviterUserId) return@withLock InviteResult(InviteStatus.NOT_OWNER)
        val inviteeId = userIdsByUsername[normalizeUsername(username)]
            ?: return@withLock InviteResult(InviteStatus.USER_NOT_FOUND)
        if (inviteeId == inviterUserId) return@withLock InviteResult(InviteStatus.SELF_INVITE)
        if (inviteeId in memberships[projectId].orEmpty()) return@withLock InviteResult(InviteStatus.ALREADY_MEMBER)
        if (invitations.values.any {
                it.projectId == projectId && it.inviteeUserId == inviteeId &&
                        it.status == ProjectInvitationStatus.PENDING
            }
        ) return@withLock InviteResult(InviteStatus.DUPLICATE_PENDING)

        val inviter = users.getValue(inviterUserId)
        val invitee = users.getValue(inviteeId)
        val invitation = ProjectInvitation(
            id = UUID.randomUUID().toString(),
            projectId = projectId,
            projectName = project.name,
            inviterUserId = inviterUserId,
            inviterUsername = inviter.username,
            inviteeUserId = inviteeId,
            inviteeUsername = invitee.username,
            status = ProjectInvitationStatus.PENDING,
            createdAtEpochMs = System.currentTimeMillis()
        )
        invitations[invitation.id] = invitation
        InviteResult(InviteStatus.CREATED, invitation)
    }

    override suspend fun getInvitationsForUser(userId: String): List<ProjectInvitation> =
        mutex.withLock {
            invitations.values.filter { it.inviteeUserId == userId }
                .sortedByDescending { it.createdAtEpochMs }
        }

    override suspend fun respondToInvitation(
        invitationId: String,
        userId: String,
        accept: Boolean
    ): InvitationResponseResult = mutex.withLock {
        val invitation = invitations[invitationId]
            ?: return@withLock InvitationResponseResult(InvitationResponseStatus.NOT_FOUND)
        if (invitation.inviteeUserId != userId) {
            return@withLock InvitationResponseResult(InvitationResponseStatus.NOT_INVITEE)
        }
        if (invitation.status != ProjectInvitationStatus.PENDING) {
            return@withLock InvitationResponseResult(
                InvitationResponseStatus.ALREADY_RESPONDED,
                invitation
            )
        }
        val updated = invitation.copy(
            status = if (accept) ProjectInvitationStatus.ACCEPTED else ProjectInvitationStatus.DECLINED
        )
        invitations[invitationId] = updated
        if (accept && invitation.projectId in projects) {
            memberships.getOrPut(invitation.projectId, ::mutableMapOf)[userId] = ProjectMembership(
                projectId = invitation.projectId,
                userId = userId,
                role = ProjectMemberRole.PARTNER,
                joinedAtEpochMs = System.currentTimeMillis()
            )
        }
        InvitationResponseResult(InvitationResponseStatus.UPDATED, updated)
    }

    override suspend fun getExpenses(projectId: String, userId: String): List<Expense>? =
        mutex.withLock {
            if (userId !in memberships[projectId].orEmpty()) return@withLock null
            expenses[projectId].orEmpty().values.sortedByDescending { it.transactionDate }
        }

    override suspend fun createExpense(
        projectId: String,
        userId: String,
        expense: Expense
    ): Expense? =
        mutex.withLock {
            val projectMembers = memberships[projectId] ?: return@withLock null
            val project = projects[projectId] ?: return@withLock null
            if (userId !in projectMembers || expense.payingPartnerId !in projectMembers) return@withLock null
            val categoryName = if (project.expenseCategories.isEmpty()) {
                if (expense.categoryName != null) return@withLock null
                null
            } else {
                val requestedCategory = expense.categoryName?.trim() ?: return@withLock null
                project.expenseCategories.firstOrNull {
                    it.equals(
                        requestedCategory,
                        ignoreCase = true
                    )
                }
                    ?: return@withLock null
            }
            val partners = projectMembers.keys.mapNotNull(users::get).map { user ->
                co.ke.xently.expense.tracker.model.Partner(
                    id = user.id,
                    name = user.displayName,
                    email = user.email
                )
            }
            val projectExpense = expense.copy(
                categoryName = categoryName,
                splits = ExpenseCalculator.calculateSplits(
                    expense.totalAmount,
                    expense.transactionDate,
                    partners
                )
            )
            expenses.getOrPut(projectId, ::mutableMapOf)[projectExpense.id] = projectExpense
            projectExpense
        }

    override suspend fun deleteExpense(
        projectId: String,
        userId: String,
        expenseId: String
    ): Boolean? = mutex.withLock {
        if (userId !in memberships[projectId].orEmpty()) return@withLock null
        expenses[projectId]?.remove(expenseId) != null
    }

    override suspend fun getTranches(projectId: String, userId: String): List<TranchePayment>? =
        mutex.withLock {
            if (userId !in memberships[projectId].orEmpty()) return@withLock null
            tranches[projectId].orEmpty().values.sortedByDescending { it.paymentDate }
        }

    override suspend fun createTranche(
        projectId: String,
        userId: String,
        payment: TranchePayment
    ): TranchePayment? = mutex.withLock {
        val projectMembers = memberships[projectId] ?: return@withLock null
        if (userId !in projectMembers || payment.partnerId != userId) return@withLock null
        tranches.getOrPut(projectId, ::mutableMapOf)[payment.id] = payment
        payment
    }

    private fun normalizeUsername(username: String): String = username.trim().lowercase()
}

fun normalizeExpenseCategories(categories: List<String>): List<String>? {
    val normalized = categories.map(String::trim)
    if (normalized.any(String::isEmpty) || normalized.distinctBy { it.lowercase() }.size != normalized.size) {
        return null
    }
    return normalized
}