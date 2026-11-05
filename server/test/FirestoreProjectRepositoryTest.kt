package co.ke.xently.expense.tracker.server.repository

import co.ke.xently.expense.tracker.model.Expense
import co.ke.xently.expense.tracker.model.ExpenseCategory
import co.ke.xently.expense.tracker.model.ProjectUser
import co.ke.xently.expense.tracker.model.TranchePayment
import com.google.auth.oauth2.AccessToken
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.FirestoreOptions
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.junit.jupiter.api.Assumptions.assumeTrue

class FirestoreProjectRepositoryTest {
    @Test
    fun persistsInvitationsAndProjectScopedFinance() = runBlocking {
        val emulatorHost = System.getenv("FIRESTORE_EMULATOR_HOST")
        assumeTrue(!emulatorHost.isNullOrBlank(), "FIRESTORE_EMULATOR_HOST must point to a Firestore emulator")
        val firestore = FirestoreOptions.getDefaultInstance().toBuilder()
            .setProjectId("expense-tracker-tests")
            .setHost(checkNotNull(emulatorHost))
            .setCredentials(
                GoogleCredentials.create(
                    AccessToken("firestore-emulator-token", Date(System.currentTimeMillis() + 60_000))
                )
            )
            .build()
            .service

        try {
            val repository = FirestoreProjectRepository { firestore }
            val owner = user("owner")
            val invitee = user("invitee")
            val stranger = user("stranger")
            assertEquals(RegistrationStatus.CREATED, repository.registerUser(owner).status)
            assertEquals(RegistrationStatus.CREATED, repository.registerUser(invitee).status)
            assertEquals(RegistrationStatus.CREATED, repository.registerUser(stranger).status)
            assertEquals(
                RegistrationStatus.USERNAME_TAKEN,
                repository.registerUser(user("duplicate-owner", owner.username.uppercase())).status
            )

            val project = assertNotNull(repository.createProject(owner.id, "Test project", "emulator"))
            val invitation = assertNotNull(
                repository.invitePartner(project.id, owner.id, invitee.username).invitation
            )
            assertEquals(
                InviteStatus.DUPLICATE_PENDING,
                repository.invitePartner(project.id, owner.id, invitee.username).status
            )
            assertNotNull(FirestoreProjectRepository { firestore }.getUserById(owner.id))
            assertEquals(
                InvitationResponseStatus.UPDATED,
                repository.respondToInvitation(invitation.id, invitee.id, accept = true).status
            )
            assertEquals(listOf(project.id), repository.getProjectsForUser(invitee.id).map { it.id })

            val expense = assertNotNull(
                repository.createExpense(
                    project.id,
                    owner.id,
                    Expense(
                        id = "expense-${owner.id}",
                        title = "Seedlings",
                        category = ExpenseCategory.SEEDLINGS,
                        totalAmount = 90_000.0,
                        transactionDate = LocalDate(2026, 1, 15),
                        payingPartnerId = owner.id
                    )
                )
            )
            assertEquals(2, expense.splits.size)
            assertEquals(90_000.0, expense.splits.sumOf { it.shareAmount })
            assertNotNull(
                repository.createTranche(
                    project.id,
                    invitee.id,
                    TranchePayment(
                        id = "tranche-${invitee.id}",
                        partnerId = invitee.id,
                        amount = 20_000.0,
                        paymentDate = LocalDate(2026, 2, 1)
                    )
                )
            )

            val freshRepository = FirestoreProjectRepository { firestore }
            assertEquals(listOf(expense.id), freshRepository.getExpenses(project.id, invitee.id)?.map { it.id })
            assertEquals(1, freshRepository.getTranches(project.id, invitee.id)?.size)
            assertNull(freshRepository.getExpenses(project.id, stranger.id))
        } finally {
            firestore.close()
        }
    }

    private fun user(id: String, username: String = "$id-${System.nanoTime()}"): ProjectUser = ProjectUser(
        id = "$id-${System.nanoTime()}",
        username = username,
        displayName = id,
        email = "$id@example.com"
    )
}