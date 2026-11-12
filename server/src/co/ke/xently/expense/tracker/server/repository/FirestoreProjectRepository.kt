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
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.security.MessageDigest
import java.util.UUID

/** Durable project-scoped repository backed by Firebase Cloud Firestore. */
class FirestoreProjectRepository(
    private val firestoreProvider: () -> Firestore = { FirestoreClient.getFirestore() }
) : ProjectRepository {
    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun registerUser(user: ProjectUser): RegistrationResult = withContext(Dispatchers.IO) {
        val db = firestoreProvider()
        val normalized = user.copy(username = normalizeUsername(user.username))
        val userRef = db.collection(USERS).document(normalized.id)
        val usernameRef = db.collection(USERNAMES).document(indexId(normalized.username))
        db.runTransaction { transaction ->
            val existingUser = transaction.get(userRef).get()
            if (existingUser.exists()) {
                val registered = existingUser.decode<ProjectUser>()
                    ?: error("Stored user ${normalized.id} is invalid")
                return@runTransaction RegistrationResult(RegistrationStatus.ALREADY_REGISTERED, registered)
            }
            if (transaction.get(usernameRef).get().exists()) {
                return@runTransaction RegistrationResult(RegistrationStatus.USERNAME_TAKEN, null)
            }
            transaction.set(userRef, payload(normalized))
            transaction.set(usernameRef, mapOf("username" to normalized.username, "userId" to normalized.id))
            RegistrationResult(RegistrationStatus.CREATED, normalized)
        }.get()
    }

    override suspend fun getUserById(userId: String): ProjectUser? = withContext(Dispatchers.IO) {
        firestoreProvider().collection(USERS).document(userId).get().get().decode()
    }

    override suspend fun getUserByUsername(username: String): ProjectUser? = withContext(Dispatchers.IO) {
        val db = firestoreProvider()
        val usernameDoc = db.collection(USERNAMES).document(indexId(normalizeUsername(username))).get().get()
        usernameDoc.getString("userId")?.let { db.collection(USERS).document(it).get().get().decode() }
    }

    override suspend fun createProject(
        ownerUserId: String,
        name: String,
        description: String,
        expenseCategories: List<String>
    ): Project? =
        withContext(Dispatchers.IO) {
            val db = firestoreProvider()
            val normalizedCategories = normalizeExpenseCategories(expenseCategories)
                ?: return@withContext null
            val project = Project(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                description = description.trim(),
                ownerUserId = ownerUserId,
                createdAtEpochMs = System.currentTimeMillis(),
                expenseCategories = normalizedCategories
            )
            val userRef = db.collection(USERS).document(ownerUserId)
            val projectRef = db.collection(PROJECTS).document(project.id)
            val membership = ProjectMembership(
                projectId = project.id,
                userId = ownerUserId,
                role = ProjectMemberRole.OWNER,
                joinedAtEpochMs = project.createdAtEpochMs
            )
            val membershipRef = projectRef.collection(MEMBERSHIPS).document(ownerUserId)
            db.runTransaction { transaction ->
                val userSnapshot = transaction.get(userRef).get()
                if (!userSnapshot.exists()) return@runTransaction null
                userSnapshot.decode<ProjectUser>() ?: error("Stored user $ownerUserId is invalid")
                transaction.set(projectRef, payload(project))
                transaction.set(membershipRef, membershipData(membership))
                transaction.update(userRef, "projectIds", (userSnapshot.get("projectIds").stringList() + project.id).distinct())
                project
            }.get()
        }

    override suspend fun getProjectsForUser(userId: String): List<Project> = withContext(Dispatchers.IO) {
        val db = firestoreProvider()
        val user = db.collection(USERS).document(userId).get().get()
        if (!user.exists()) return@withContext emptyList()
        user.get("projectIds").stringList().mapNotNull { id ->
            db.collection(PROJECTS).document(id).get().get().decode<Project>()
        }.sortedBy { it.name.lowercase() }
    }

    override suspend fun getProjectForMember(projectId: String, userId: String): Project? =
        withContext(Dispatchers.IO) {
            val projectRef = firestoreProvider().collection(PROJECTS).document(projectId)
            if (!projectRef.collection(MEMBERSHIPS).document(userId).get().get().exists()) return@withContext null
            projectRef.get().get().decode()
        }

    override suspend fun getMemberships(
        projectId: String,
        userId: String
    ): List<Pair<ProjectMembership, ProjectUser>>? = withContext(Dispatchers.IO) {
        val db = firestoreProvider()
        val projectRef = db.collection(PROJECTS).document(projectId)
        if (!projectRef.collection(MEMBERSHIPS).document(userId).get().get().exists()) {
            return@withContext null
        }
        projectRef.collection(MEMBERSHIPS).get().get().documents.mapNotNull { membershipDoc ->
            val membership = membershipDoc.decodeMembership() ?: return@mapNotNull null
            val member = db.collection(USERS).document(membership.userId).get().get().decode<ProjectUser>()
                ?: return@mapNotNull null
            membership to member
        }
    }

    override suspend fun invitePartner(
        projectId: String,
        inviterUserId: String,
        username: String
    ): InviteResult = withContext(Dispatchers.IO) {
        val db = firestoreProvider()
        val projectRef = db.collection(PROJECTS).document(projectId)
        val inviterRef = db.collection(USERS).document(inviterUserId)
        val usernameRef = db.collection(USERNAMES).document(indexId(normalizeUsername(username)))
        val invitationRef = db.collection(INVITATIONS).document()
        db.runTransaction { transaction ->
            val projectDoc = transaction.get(projectRef).get()
            if (!projectDoc.exists()) return@runTransaction InviteResult(InviteStatus.PROJECT_NOT_FOUND)
            val project = projectDoc.decode<Project>() ?: error("Stored project $projectId is invalid")
            if (project.ownerUserId != inviterUserId) return@runTransaction InviteResult(InviteStatus.NOT_OWNER)
            val inviterDoc = transaction.get(inviterRef).get()
            val usernameDoc = transaction.get(usernameRef).get()
            if (!inviterDoc.exists() || !usernameDoc.exists()) {
                return@runTransaction InviteResult(InviteStatus.USER_NOT_FOUND)
            }
            val inviteeId = usernameDoc.getString("userId") ?: error("Username index is invalid")
            if (inviteeId == inviterUserId) return@runTransaction InviteResult(InviteStatus.SELF_INVITE)
            val inviteeRef = db.collection(USERS).document(inviteeId)
            val memberRef = projectRef.collection(MEMBERSHIPS).document(inviteeId)
            val pendingRef = db.collection(PENDING_INVITATIONS).document(indexId("$projectId:$inviteeId"))
            val inviteeDoc = transaction.get(inviteeRef).get()
            if (!inviteeDoc.exists()) return@runTransaction InviteResult(InviteStatus.USER_NOT_FOUND)
            if (transaction.get(memberRef).get().exists()) {
                return@runTransaction InviteResult(InviteStatus.ALREADY_MEMBER)
            }
            if (transaction.get(pendingRef).get().exists()) {
                return@runTransaction InviteResult(InviteStatus.DUPLICATE_PENDING)
            }
            val inviter = inviterDoc.decode<ProjectUser>() ?: error("Stored user $inviterUserId is invalid")
            val invitee = inviteeDoc.decode<ProjectUser>() ?: error("Stored user $inviteeId is invalid")
            val invitation = ProjectInvitation(
                id = invitationRef.id,
                projectId = project.id,
                projectName = project.name,
                inviterUserId = inviterUserId,
                inviterUsername = inviter.username,
                inviteeUserId = invitee.id,
                inviteeUsername = invitee.username,
                status = ProjectInvitationStatus.PENDING,
                createdAtEpochMs = System.currentTimeMillis()
            )
            transaction.set(invitationRef, invitationData(invitation))
            transaction.set(pendingRef, mapOf("invitationId" to invitation.id))
            InviteResult(InviteStatus.CREATED, invitation)
        }.get()
    }

    override suspend fun getInvitationsForUser(userId: String): List<ProjectInvitation> =
        withContext(Dispatchers.IO) {
            firestoreProvider().collection(INVITATIONS).whereEqualTo("inviteeUserId", userId).get().get()
                .documents.mapNotNull { it.decodeInvitation() }
                .sortedByDescending { it.createdAtEpochMs }
        }

    override suspend fun respondToInvitation(
        invitationId: String,
        userId: String,
        accept: Boolean
    ): InvitationResponseResult = withContext(Dispatchers.IO) {
        val db = firestoreProvider()
        val invitationRef = db.collection(INVITATIONS).document(invitationId)
        db.runTransaction { transaction ->
            val invitationDoc = transaction.get(invitationRef).get()
            if (!invitationDoc.exists()) return@runTransaction InvitationResponseResult(InvitationResponseStatus.NOT_FOUND)
            val invitation = invitationDoc.decodeInvitation() ?: error("Stored invitation $invitationId is invalid")
            if (invitation.inviteeUserId != userId) {
                return@runTransaction InvitationResponseResult(InvitationResponseStatus.NOT_INVITEE)
            }
            if (invitation.status != ProjectInvitationStatus.PENDING) {
                return@runTransaction InvitationResponseResult(InvitationResponseStatus.ALREADY_RESPONDED, invitation)
            }
            val projectRef = db.collection(PROJECTS).document(invitation.projectId)
            val userRef = db.collection(USERS).document(userId)
            val memberRef = projectRef.collection(MEMBERSHIPS).document(userId)
            val pendingRef = db.collection(PENDING_INVITATIONS)
                .document(indexId("${invitation.projectId}:$userId"))
            val projectDoc = transaction.get(projectRef).get()
            val userDoc = transaction.get(userRef).get()
            val memberDoc = transaction.get(memberRef).get()
            val updated = invitation.copy(
                status = if (accept) ProjectInvitationStatus.ACCEPTED else ProjectInvitationStatus.DECLINED
            )
            transaction.set(invitationRef, invitationData(updated))
            if (accept && projectDoc.exists() && userDoc.exists() && !memberDoc.exists()) {
                val membership = ProjectMembership(
                    projectId = invitation.projectId,
                    userId = userId,
                    role = ProjectMemberRole.PARTNER,
                    joinedAtEpochMs = System.currentTimeMillis()
                )
                transaction.set(memberRef, membershipData(membership))
                transaction.update(
                    userRef,
                    "projectIds",
                    (userDoc.get("projectIds").stringList() + invitation.projectId).distinct()
                )
            }
            transaction.delete(pendingRef)
            InvitationResponseResult(InvitationResponseStatus.UPDATED, updated)
        }.get()
    }

    override suspend fun getExpenses(projectId: String, userId: String): List<Expense>? =
        withContext(Dispatchers.IO) {
            val projectRef = firestoreProvider().collection(PROJECTS).document(projectId)
            if (!projectRef.collection(MEMBERSHIPS).document(userId).get().get().exists()) {
                return@withContext null
            }
            projectRef.collection(EXPENSES).get().get().documents.mapNotNull { it.decode<Expense>() }
                .sortedByDescending { it.transactionDate }
        }

    override suspend fun createExpense(projectId: String, userId: String, expense: Expense): Expense? =
        withContext(Dispatchers.IO) {
            val db = firestoreProvider()
            val projectRef = db.collection(PROJECTS).document(projectId)
            val callerRef = projectRef.collection(MEMBERSHIPS).document(userId)
            val payerRef = projectRef.collection(MEMBERSHIPS).document(expense.payingPartnerId)
            val expenseRef = projectRef.collection(EXPENSES).document(expense.id)
            db.runTransaction { transaction ->
                val projectDoc = transaction.get(projectRef).get()
                val project = projectDoc.decode<Project>() ?: return@runTransaction null
                val caller = transaction.get(callerRef).get()
                val payer = transaction.get(payerRef).get()
                if (!caller.exists() || !payer.exists()) return@runTransaction null
                val categoryName = if (project.expenseCategories.isEmpty()) {
                    if (expense.categoryName != null) return@runTransaction null
                    null
                } else {
                    val requestedCategory =
                        expense.categoryName?.trim() ?: return@runTransaction null
                    project.expenseCategories.firstOrNull {
                        it.equals(requestedCategory, ignoreCase = true)
                    } ?: return@runTransaction null
                }
                val memberDocs = transaction.get(projectRef.collection(MEMBERSHIPS)).get().documents
                val partners = memberDocs.mapNotNull { it.decodeMembership() }
                    .mapNotNull { membership ->
                        transaction.get(db.collection(USERS).document(membership.userId)).get()
                            .decode<ProjectUser>()
                    }
                    .map { member ->
                        co.ke.xently.expense.tracker.model.Partner(member.id, member.displayName, member.email)
                    }
                val authorizedExpense = expense.copy(
                    categoryName = categoryName,
                    splits = ExpenseCalculator.calculateSplits(expense.totalAmount, expense.transactionDate, partners)
                )
                transaction.set(expenseRef, payload(authorizedExpense))
                authorizedExpense
            }.get()
        }

    override suspend fun deleteExpense(projectId: String, userId: String, expenseId: String): Boolean? =
        withContext(Dispatchers.IO) {
            val db = firestoreProvider()
            val projectRef = db.collection(PROJECTS).document(projectId)
            val memberRef = projectRef.collection(MEMBERSHIPS).document(userId)
            val expenseRef = projectRef.collection(EXPENSES).document(expenseId)
            db.runTransaction { transaction ->
                if (!transaction.get(memberRef).get().exists()) return@runTransaction null
                val expense = transaction.get(expenseRef).get()
                if (!expense.exists()) return@runTransaction false
                transaction.delete(expenseRef)
                true
            }.get()
        }

    override suspend fun getTranches(projectId: String, userId: String): List<TranchePayment>? =
        withContext(Dispatchers.IO) {
            val projectRef = firestoreProvider().collection(PROJECTS).document(projectId)
            if (!projectRef.collection(MEMBERSHIPS).document(userId).get().get().exists()) {
                return@withContext null
            }
            projectRef.collection(TRANCHES).get().get().documents.mapNotNull { it.decode<TranchePayment>() }
                .sortedByDescending { it.paymentDate }
        }

    override suspend fun createTranche(
        projectId: String,
        userId: String,
        payment: TranchePayment
    ): TranchePayment? = withContext(Dispatchers.IO) {
        if (payment.partnerId != userId) return@withContext null
        val db = firestoreProvider()
        val projectRef = db.collection(PROJECTS).document(projectId)
        val memberRef = projectRef.collection(MEMBERSHIPS).document(userId)
        val trancheRef = projectRef.collection(TRANCHES).document(payment.id)
        db.runTransaction { transaction ->
            if (!transaction.get(memberRef).get().exists()) return@runTransaction null
            transaction.set(trancheRef, payload(payment))
            payment
        }.get()
    }

    private inline fun <reified T> DocumentSnapshot.decode(): T? =
        getString(PAYLOAD)?.let { json.decodeFromString<T>(it) }

    private fun DocumentSnapshot.decodeMembership(): ProjectMembership? = runCatching {
        ProjectMembership(
            projectId = getString("projectId") ?: return null,
            userId = getString("userId") ?: return null,
            role = ProjectMemberRole.valueOf(getString("role") ?: return null),
            joinedAtEpochMs = getLong("joinedAtEpochMs") ?: return null
        )
    }.getOrNull()

    private fun DocumentSnapshot.decodeInvitation(): ProjectInvitation? =
        getString(PAYLOAD)?.let { json.decodeFromString<ProjectInvitation>(it) }

    private inline fun <reified T> payload(value: T): Map<String, Any> =
        mapOf(PAYLOAD to json.encodeToString(value))

    private fun membershipData(membership: ProjectMembership): Map<String, Any> = mapOf(
        "projectId" to membership.projectId,
        "userId" to membership.userId,
        "role" to membership.role.name,
        "joinedAtEpochMs" to membership.joinedAtEpochMs
    )

    private fun invitationData(invitation: ProjectInvitation): Map<String, Any> = mapOf(
        "payload" to json.encodeToString(invitation),
        "projectId" to invitation.projectId,
        "inviteeUserId" to invitation.inviteeUserId,
        "status" to invitation.status.name,
        "createdAtEpochMs" to invitation.createdAtEpochMs
    )

    private fun Any?.stringList(): List<String> = (this as? List<*>)?.filterIsInstance<String>().orEmpty()

    private fun normalizeUsername(username: String): String = username.trim().lowercase()

    private fun indexId(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray())
        .joinToString("") { byte -> "%02x".format(byte) }

    private companion object {
        const val USERS = "projectUsers"
        const val USERNAMES = "projectUsernames"
        const val PROJECTS = "projects"
        const val MEMBERSHIPS = "memberships"
        const val INVITATIONS = "projectInvitations"
        const val PENDING_INVITATIONS = "pendingProjectInvitations"
        const val EXPENSES = "expenses"
        const val TRANCHES = "tranches"
        const val PAYLOAD = "payload"
    }
}