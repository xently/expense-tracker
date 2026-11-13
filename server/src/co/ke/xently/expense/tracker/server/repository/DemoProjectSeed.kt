package co.ke.xently.expense.tracker.server.repository

import co.ke.xently.expense.tracker.model.Expense
import co.ke.xently.expense.tracker.model.ExpenseCategory
import co.ke.xently.expense.tracker.model.Project
import co.ke.xently.expense.tracker.model.ProjectInvitationStatus
import co.ke.xently.expense.tracker.model.ProjectUser
import co.ke.xently.expense.tracker.model.TranchePayment
import co.ke.xently.expense.tracker.server.auth.AuthenticatedUser
import kotlinx.datetime.LocalDate

const val DEMO_ACCESS_TOKEN = "local-demo-token"
const val DEMO_USER_ID = "demo-vin"
val DEMO_USER = AuthenticatedUser(DEMO_USER_ID, "vin@example.test", "Vin")

suspend fun seedDemoWorkspace(repository: ProjectRepository): Project {
    val vin = ProjectUser(DEMO_USER_ID, "vin", "Vin", "vin@example.test")
    val harrison = ProjectUser("demo-harrison", "harrison", "Harrison", "harrison@example.test")
    val herman = ProjectUser("demo-herman", "herman", "Herman", "herman@example.test")
    listOf(vin, harrison, herman).forEach { repository.registerUser(it) }

    val project = requireNotNull(
        repository.createProject(vin.id, "Sugarcane Planting Season", "Sample data for UI exploration")
    )
    listOf(harrison, herman).forEach { partner ->
        val invite = repository.invitePartner(project.id, vin.id, partner.username)
        val invitation = requireNotNull(invite.invitation)
        check(invite.status == InviteStatus.CREATED)
        check(
            repository.respondToInvitation(invitation.id, partner.id, accept = true).invitation?.status ==
                ProjectInvitationStatus.ACCEPTED
        )
    }

    val otherProject = requireNotNull(
        repository.createProject(harrison.id, "Community Orchard", "Another sample project")
    )
    val pendingInvitation = repository.invitePartner(otherProject.id, harrison.id, vin.username)
    check(pendingInvitation.status == InviteStatus.CREATED)

    val expenses = listOf(
        Expense(
            id = "demo-land-preparation",
            title = "Land preparation",
            category = ExpenseCategory.LAND_PREP,
            totalAmount = 30000.0,
            transactionDate = LocalDate(2026, 2, 15),
            payingPartnerId = harrison.id,
            notes = "Initial demo expense"
        ),
        Expense(
            id = "demo-seedlings",
            title = "Seedlings and delivery",
            category = ExpenseCategory.SEEDLINGS,
            totalAmount = 18000.0,
            transactionDate = LocalDate(2026, 3, 5),
            payingPartnerId = vin.id
        )
    )
    expenses.forEach { expense ->
        check(repository.createExpense(project.id, vin.id, expense) != null)
    }
    check(
        repository.createTranche(
            project.id,
            herman.id,
            TranchePayment(
                id = "demo-tranche-herman",
                partnerId = herman.id,
                amount = 10000.0,
                paymentDate = LocalDate(2026, 3, 12),
                notes = "Sample installment",
                referenceNumber = "DEMO-001"
            )
        ) != null
    )
    return project
}