package co.ke.xently.expense.tracker.ui

import androidx.compose.ui.tooling.preview.Preview
import co.ke.xently.expense.tracker.model.Expense
import co.ke.xently.expense.tracker.model.ExpenseCategory
import co.ke.xently.expense.tracker.model.ExpenseSplit
import co.ke.xently.expense.tracker.model.Project
import co.ke.xently.expense.tracker.model.ProjectInvitation
import co.ke.xently.expense.tracker.model.ProjectInvitationStatus
import co.ke.xently.expense.tracker.model.ProjectMemberRole
import co.ke.xently.expense.tracker.model.ProjectMembership
import co.ke.xently.expense.tracker.model.ProjectUser
import co.ke.xently.expense.tracker.model.TranchePayment
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceState
import co.ke.xently.expense.tracker.viewmodel.WorkspacePhase
import kotlinx.datetime.LocalDate

@Preview(name = "Default font", fontScale = 1f)
@Preview(name = "Large font", fontScale = 1.5f)
annotation class PreviewFontScale

internal val previewWorkspaceState = ProjectWorkspaceState(
    phase = WorkspacePhase.PROJECT,
    profile = ProjectUser("user-harrison", "harrison", "Harrison", "harrison@example.com"),
    projects = listOf(
        Project("project-demo", "Planting Season", "2026 crop", "user-harrison", 0L),
        Project("project-orchard", "Community Orchard", "Another sample", "user-herman", 0L)
    ),
    selectedProjectId = "project-demo",
    memberships = listOf(
        ProjectMembership("project-demo", "user-harrison", ProjectMemberRole.OWNER, 0L) to
                ProjectUser("user-harrison", "harrison", "Harrison", "harrison@example.com"),
        ProjectMembership("project-demo", "user-herman", ProjectMemberRole.PARTNER, 0L) to
                ProjectUser("user-herman", "herman", "Herman", "herman@example.com"),
        ProjectMembership("project-demo", "user-vin", ProjectMemberRole.PARTNER, 0L) to
                ProjectUser("user-vin", "vin", "Vin", "vin@example.com")
    ),
    invitations = listOf(
        ProjectInvitation(
            id = "invitation-demo",
            projectId = "project-orchard",
            projectName = "Community Orchard",
            inviterUserId = "user-herman",
            inviterUsername = "herman",
            inviteeUserId = "user-harrison",
            inviteeUsername = "harrison",
            status = ProjectInvitationStatus.PENDING,
            createdAtEpochMs = 0L
        )
    ),
    expenses = listOf(
        Expense(
            id = "expense-land-prep",
            title = "Land preparation",
            category = ExpenseCategory.LAND_PREP,
            totalAmount = 30000.0,
            transactionDate = LocalDate(2026, 1, 15),
            payingPartnerId = "user-harrison",
            splits = listOf(
                ExpenseSplit("user-harrison", 10000.0, LocalDate(2026, 2, 10)),
                ExpenseSplit("user-herman", 10000.0, LocalDate(2026, 2, 10)),
                ExpenseSplit("user-vin", 10000.0, LocalDate(2026, 2, 10))
            )
        ),
        Expense(
            id = "expense-seedlings",
            title = "Seedlings",
            category = ExpenseCategory.SEEDLINGS,
            totalAmount = 18000.0,
            transactionDate = LocalDate(2026, 2, 3),
            payingPartnerId = "user-vin"
        )
    ),
    tranches = listOf(
        TranchePayment(
            id = "tranche-demo",
            partnerId = "user-herman",
            amount = 10000.0,
            paymentDate = LocalDate(2026, 2, 1),
            referenceNumber = "TRX-001"
        )
    ),
    grossPayout = "450000",
    harvestCosts = "25000"
)

internal val previewEmptyWorkspaceState = ProjectWorkspaceState(
    phase = WorkspacePhase.PROJECT,
    profile = previewWorkspaceState.profile,
    projects = previewWorkspaceState.projects,
    selectedProjectId = previewWorkspaceState.selectedProjectId,
    memberships = previewWorkspaceState.memberships
)