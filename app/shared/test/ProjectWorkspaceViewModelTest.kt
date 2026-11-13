package co.ke.xently.expense.tracker

import androidx.lifecycle.ViewModelStore
import co.ke.xently.expense.tracker.data.ApiException
import co.ke.xently.expense.tracker.data.CachedProjectMember
import co.ke.xently.expense.tracker.data.CachedProjectSnapshot
import co.ke.xently.expense.tracker.data.CreateExpensePayload
import co.ke.xently.expense.tracker.data.CreateTranchePayload
import co.ke.xently.expense.tracker.data.InMemoryWorkspaceCache
import co.ke.xently.expense.tracker.data.ProjectApi
import co.ke.xently.expense.tracker.data.ProjectMemberResponse
import co.ke.xently.expense.tracker.data.RevenueSplitPayload
import co.ke.xently.expense.tracker.data.WorkspaceCache
import co.ke.xently.expense.tracker.model.Expense
import co.ke.xently.expense.tracker.model.ExpenseCategory
import co.ke.xently.expense.tracker.model.MyProjectsResponse
import co.ke.xently.expense.tracker.model.Project
import co.ke.xently.expense.tracker.model.ProjectInvitation
import co.ke.xently.expense.tracker.model.ProjectInvitationStatus
import co.ke.xently.expense.tracker.model.ProjectMemberRole
import co.ke.xently.expense.tracker.model.ProjectMembership
import co.ke.xently.expense.tracker.model.ProjectUser
import co.ke.xently.expense.tracker.model.SeasonRevenueCalculation
import co.ke.xently.expense.tracker.model.TranchePayment
import co.ke.xently.expense.tracker.viewmodel.NavigationTab
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceEvent
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceViewModel
import co.ke.xently.expense.tracker.viewmodel.WorkspaceForm
import co.ke.xently.expense.tracker.viewmodel.WorkspaceIssue
import co.ke.xently.expense.tracker.viewmodel.WorkspacePhase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectWorkspaceViewModelTest {
    @BeforeTest
    fun setMainDispatcher() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun formEventsOpenAndDismissTheSharedExpenseAndPaymentForms() {
        val api = FakeProjectApi().apply { projects += project("form-project") }
        val viewModel = ProjectWorkspaceViewModel(api)
        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))

        viewModel.onEvent(ProjectWorkspaceEvent.OpenExpenseForm)
        assertEquals(WorkspaceForm.EXPENSE, viewModel.state.value.activeForm)

        viewModel.onEvent(ProjectWorkspaceEvent.OpenTrancheForm)
        assertEquals(WorkspaceForm.TRANCHE, viewModel.state.value.activeForm)

        viewModel.onEvent(ProjectWorkspaceEvent.DismissForm)
        assertNull(viewModel.state.value.activeForm)

        viewModel.onEvent(ProjectWorkspaceEvent.OpenExpenseForm)
        viewModel.onEvent(ProjectWorkspaceEvent.BackToProjects)
        assertNull(viewModel.state.value.activeForm)
    }

    @Test
    fun refreshHasSeparateProgressAndIgnoresDuplicateRefreshes() {
        val api = FakeProjectApi().apply { projects += project("refresh-project") }
        val viewModel = ProjectWorkspaceViewModel(api)
        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))
        assertFalse(viewModel.state.value.isRefreshing)

        val refreshGate = CompletableDeferred<Unit>()
        api.getProjectsGate = refreshGate
        viewModel.onEvent(ProjectWorkspaceEvent.Refresh)

        assertTrue(viewModel.state.value.isRefreshing)
        assertEquals(2, api.getProjectsCalls)
        viewModel.onEvent(ProjectWorkspaceEvent.Refresh)
        assertEquals(2, api.getProjectsCalls)

        refreshGate.complete(Unit)

        assertFalse(viewModel.state.value.isRefreshing)
    }

    @Test
    fun requestErrorsCanBeDismissedAndReportedAgain() {
        val api = FakeProjectApi().apply {
            projects += project("error-project")
            failMemberLoad = true
        }
        val viewModel = ProjectWorkspaceViewModel(api)
        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))
        assertEquals(WorkspaceIssue.RequestFailed(503), viewModel.state.value.error)

        viewModel.onEvent(ProjectWorkspaceEvent.DismissMessage)
        assertNull(viewModel.state.value.error)

        viewModel.onEvent(ProjectWorkspaceEvent.Refresh)

        assertEquals(WorkspaceIssue.RequestFailed(503), viewModel.state.value.error)
        assertFalse(viewModel.state.value.isRefreshing)
    }

    @Test
    fun clearingViewModelCancelsSuspendedRequestWithoutShowingAnError() {
        val api = FakeProjectApi().apply { getProfileGate = CompletableDeferred() }
        val viewModel = ProjectWorkspaceViewModel(api)
        val viewModelStore = ViewModelStore()
        viewModelStore.put("workspace", viewModel)

        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))
        assertTrue(viewModel.state.value.isLoading)

        viewModelStore.clear()

        assertTrue(api.profileRequestCancelled)
        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun cancellationDuringCacheReadIsNotReportedAsARequestFailure() {
        val api = FakeProjectApi().apply { projects += project("cache-read") }
        val cache = object : WorkspaceCache {
            override suspend fun read(projectId: String): CachedProjectSnapshot? {
                throw CancellationException("cache read cancelled")
            }

            override suspend fun write(snapshot: CachedProjectSnapshot) = Unit

            override suspend fun clear(projectId: String) = Unit
        }
        val viewModel = ProjectWorkspaceViewModel(api, cache)

        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))

        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun cancellationDuringCacheWriteIsNotReportedAsARequestFailure() {
        val api = FakeProjectApi().apply { projects += project("cache-write") }
        val cache = object : WorkspaceCache {
            override suspend fun read(projectId: String): CachedProjectSnapshot? = null

            override suspend fun write(snapshot: CachedProjectSnapshot) {
                throw CancellationException("cache write cancelled")
            }

            override suspend fun clear(projectId: String) = Unit
        }
        val viewModel = ProjectWorkspaceViewModel(api, cache)
        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))

        viewModel.onEvent(
            ProjectWorkspaceEvent.CreateExpense(
                title = "Seed delivery",
                category = ExpenseCategory.SEEDLINGS,
                amount = 100.0,
                date = LocalDate(2026, 5, 1),
                payingPartnerId = USER.id,
                notes = ""
            )
        )

        assertEquals("expense-1", viewModel.state.value.expenses.single().id)
        assertNull(viewModel.state.value.error)
        assertFalse(viewModel.state.value.isLoading)
    }

    @Test
    fun singleProjectIsSelectedAutomatically() {
        val api = FakeProjectApi().apply { projects += project("only") }
        val viewModel = ProjectWorkspaceViewModel(api)

        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))

        assertEquals(WorkspacePhase.PROJECT, viewModel.state.value.phase)
        assertEquals("only", viewModel.state.value.selectedProjectId)
        assertEquals(listOf("only"), api.loadedProjectIds)
    }

    @Test
    fun noProjectsShowsProjectCreationAndInvitationState() {
        val viewModel = ProjectWorkspaceViewModel(FakeProjectApi())

        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))

        assertEquals(WorkspacePhase.PROJECTS, viewModel.state.value.phase)
        assertNull(viewModel.state.value.selectedProjectId)
        assertEquals(emptyList(), viewModel.state.value.projects)
    }

    @Test
    fun multipleProjectsRequireExplicitSelectionAndLoadOnlyChosenProject() {
        val api = FakeProjectApi().apply {
            projects += project("one")
            projects += project("two")
            expensesByProject["one"] = listOf(expense("one-expense"))
            expensesByProject["two"] = listOf(expense("two-expense"))
        }
        val viewModel = ProjectWorkspaceViewModel(api)

        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))
        assertEquals(WorkspacePhase.PROJECTS, viewModel.state.value.phase)
        assertNull(viewModel.state.value.selectedProjectId)

        viewModel.onEvent(ProjectWorkspaceEvent.SelectProject("two"))

        assertEquals("two", viewModel.state.value.selectedProjectId)
        assertEquals(listOf("two-expense"), viewModel.state.value.expenses.map { it.id })
        assertEquals(listOf("two"), api.loadedProjectIds)
    }

    @Test
    fun returningFromProjectSwitcherRestoresTheSameProjectAndTab() {
        val api = FakeProjectApi().apply {
            projects += project("one")
            projects += project("two")
        }
        val viewModel = ProjectWorkspaceViewModel(api)
        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))
        viewModel.onEvent(ProjectWorkspaceEvent.SelectProject("one"))
        viewModel.onEvent(ProjectWorkspaceEvent.SelectTab(NavigationTab.PARTNERS))
        viewModel.onEvent(ProjectWorkspaceEvent.BackToProjects)

        assertEquals(WorkspacePhase.PROJECTS, viewModel.state.value.phase)
        assertEquals("one", viewModel.state.value.selectedProjectId)

        viewModel.onEvent(ProjectWorkspaceEvent.ReturnToProject)

        assertEquals(WorkspacePhase.PROJECT, viewModel.state.value.phase)
        assertEquals("one", viewModel.state.value.selectedProjectId)
        assertEquals(NavigationTab.PARTNERS, viewModel.state.value.selectedTab)
    }

    @Test
    fun returningWithoutAnActiveProjectKeepsTheProjectSelectionScreen() {
        val viewModel = ProjectWorkspaceViewModel(FakeProjectApi())
        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))

        viewModel.onEvent(ProjectWorkspaceEvent.ReturnToProject)

        assertEquals(WorkspacePhase.PROJECTS, viewModel.state.value.phase)
        assertNull(viewModel.state.value.selectedProjectId)
    }

    @Test
    fun creatingProjectFromSwitcherOpensTheNewProject() {
        val api = FakeProjectApi().apply { projects += project("existing") }
        val viewModel = ProjectWorkspaceViewModel(api)
        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))
        viewModel.onEvent(ProjectWorkspaceEvent.BackToProjects)

        viewModel.onEvent(ProjectWorkspaceEvent.CreateProject("New project", "", listOf("Tools")))

        assertEquals(WorkspacePhase.PROJECT, viewModel.state.value.phase)
        assertEquals("created", viewModel.state.value.selectedProjectId)
        assertEquals(listOf("Tools"), viewModel.state.value.selectedProject?.expenseCategories)
    }

    @Test
    fun projectCategoriesAreSentAndUsedForNewExpenses() {
        val api = FakeProjectApi()
        val viewModel = ProjectWorkspaceViewModel(api)
        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))
        viewModel.onEvent(
            ProjectWorkspaceEvent.CreateProject(
                name = "Orchard",
                description = "",
                expenseCategories = listOf("Soil testing", "Seedlings")
            )
        )

        assertEquals(listOf("Soil testing", "Seedlings"), api.lastProjectCategories)
        assertEquals(
            listOf("Soil testing", "Seedlings"),
            viewModel.state.value.selectedProject?.expenseCategories
        )

        viewModel.onEvent(
            ProjectWorkspaceEvent.CreateExpense(
                title = "Soil sample",
                category = ExpenseCategory.MISCELLANEOUS,
                amount = 100.0,
                date = LocalDate(2026, 5, 1),
                payingPartnerId = USER.id,
                notes = "",
                categoryName = "Soil testing"
            )
        )

        assertEquals("Soil testing", api.lastExpensePayload?.categoryName)
        assertEquals("Soil testing", viewModel.state.value.expenses.single().categoryName)
    }

    @Test
    fun switchingProjectsNeverShowsPreviousProjectRecordsOnLoadFailure() {
        val api = FakeProjectApi().apply {
            projects += project("one")
            projects += project("two")
            expensesByProject["one"] = listOf(expense("one-expense"))
        }
        val viewModel = ProjectWorkspaceViewModel(api)
        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))
        viewModel.onEvent(ProjectWorkspaceEvent.SelectProject("one"))
        assertEquals(listOf("one-expense"), viewModel.state.value.expenses.map { it.id })

        api.failMemberProjects += "two"
        viewModel.onEvent(ProjectWorkspaceEvent.SelectProject("two"))

        assertEquals("two", viewModel.state.value.selectedProjectId)
        assertEquals(emptyList(), viewModel.state.value.expenses)
        assertEquals(emptyList(), viewModel.state.value.memberships)
    }

    @Test
    fun acceptingInvitationLoadsNewSingleProject() {
        val api = FakeProjectApi().apply {
            invitations += invitation()
        }
        val viewModel = ProjectWorkspaceViewModel(api)
        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))

        viewModel.onEvent(ProjectWorkspaceEvent.RespondToInvitation("invite-1", accept = true))

        assertEquals(WorkspacePhase.PROJECT, viewModel.state.value.phase)
        assertEquals("invited-project", viewModel.state.value.selectedProjectId)
        assertEquals(
            ProjectInvitationStatus.ACCEPTED,
            viewModel.state.value.invitations.single().status
        )
    }

    @Test
    fun profileNotFoundOpensRegistrationBeforeLoadingProjects() {
        val api = FakeProjectApi().apply { profileNotFound = true }
        val viewModel = ProjectWorkspaceViewModel(api)

        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))
        assertEquals(WorkspacePhase.REGISTRATION, viewModel.state.value.phase)

        viewModel.onEvent(ProjectWorkspaceEvent.Register("new_partner", "New Partner"))

        assertEquals("new_partner", viewModel.state.value.profile?.username)
        assertEquals(WorkspacePhase.PROJECTS, viewModel.state.value.phase)
    }

    @Test
    fun cachedProjectSnapshotRemainsVisibleWhenRefreshFails() {
        val project = project("cached")
        val cachedExpense = expense("cached-expense")
        val cache = InMemoryWorkspaceCache(
            listOf(
                CachedProjectSnapshot(
                    project = project,
                    members = listOf(
                        CachedProjectMember(
                            ProjectMembership(
                                project.id,
                                USER.id,
                                ProjectMemberRole.OWNER,
                                0
                            ), USER
                        )
                    ),
                    expenses = listOf(cachedExpense),
                    tranches = emptyList()
                )
            )
        )
        val api = FakeProjectApi().apply {
            projects += project
            failMemberLoad = true
        }
        val viewModel = ProjectWorkspaceViewModel(api, cache)

        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))

        assertEquals(listOf(cachedExpense.id), viewModel.state.value.expenses.map { it.id })
        assertEquals(WorkspaceIssue.RequestFailed(503), viewModel.state.value.error)
    }

    @Test
    fun financeWritesUseTheSelectedProjectAndAuthenticatedPartner() {
        val api = FakeProjectApi().apply {
            projects += project("one")
            projects += project("two")
        }
        val viewModel = ProjectWorkspaceViewModel(api)
        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))
        viewModel.onEvent(ProjectWorkspaceEvent.SelectProject("two"))

        viewModel.onEvent(
            ProjectWorkspaceEvent.CreateExpense(
                title = "Seed delivery",
                category = ExpenseCategory.SEEDLINGS,
                amount = 1200.0,
                date = LocalDate(2026, 5, 1),
                payingPartnerId = USER.id,
                notes = ""
            )
        )
        viewModel.onEvent(
            ProjectWorkspaceEvent.CreateTranche(
                amount = 500.0,
                date = LocalDate(2026, 5, 2),
                referenceNumber = "receipt-1",
                notes = ""
            )
        )

        assertEquals("two", api.lastExpenseProjectId)
        assertEquals("two", api.lastTrancheProjectId)
        assertEquals(USER.id, api.lastTranchePayload?.partnerId)
        assertEquals("expense-1", viewModel.state.value.expenses.single().id)
        assertEquals("tranche-1", viewModel.state.value.tranches.single().id)

        viewModel.onEvent(ProjectWorkspaceEvent.UpdateRevenue("10000", "2000"))
        viewModel.onEvent(ProjectWorkspaceEvent.CalculateRevenueSplit)

        assertEquals("two", api.lastRevenueProjectId)
        assertEquals(10_000.0, api.lastRevenuePayload?.grossFactoryPayout)
        assertEquals(8_000.0, viewModel.state.value.revenueCalculation?.netFactoryPayout)
    }

    @Test
    fun invalidRevenueValuesAreRejectedBeforeCallingTheServer() {
        val api = FakeProjectApi().apply { projects += project("project") }
        val viewModel = ProjectWorkspaceViewModel(api)
        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))
        viewModel.onEvent(ProjectWorkspaceEvent.UpdateRevenue("-1", "10"))

        viewModel.onEvent(ProjectWorkspaceEvent.CalculateRevenueSplit)

        assertNull(api.lastRevenueProjectId)
        assertEquals(WorkspaceIssue.PayoutValuesRequired, viewModel.state.value.error)
    }

    @Test
    fun failedExpenseWriteDoesNotOptimisticallyAddRecord() {
        val api = FakeProjectApi().apply {
            projects += project("project")
            failExpenseCreation = true
        }
        val viewModel = ProjectWorkspaceViewModel(api)
        viewModel.onEvent(ProjectWorkspaceEvent.Connect(SERVICE_URL, TOKEN))
        viewModel.onEvent(
            ProjectWorkspaceEvent.CreateExpense(
                title = "Unauthorized write",
                category = ExpenseCategory.WEEDING,
                amount = 10.0,
                date = LocalDate(2026, 5, 1),
                payingPartnerId = USER.id,
                notes = ""
            )
        )

        assertEquals(emptyList(), viewModel.state.value.expenses)
        assertNotNull(viewModel.state.value.error)
    }

    @Test
    fun tabAndThemeEventsUpdateWorkspaceState() {
        val viewModel = ProjectWorkspaceViewModel(FakeProjectApi())

        viewModel.onEvent(ProjectWorkspaceEvent.SelectTab(NavigationTab.EXPENSES))
        viewModel.onEvent(ProjectWorkspaceEvent.SetTheme(co.ke.xently.expense.tracker.viewmodel.ThemePreference.DARK))

        assertEquals(NavigationTab.EXPENSES, viewModel.state.value.selectedTab)
        assertEquals(
            co.ke.xently.expense.tracker.viewmodel.ThemePreference.DARK,
            viewModel.state.value.themePreference
        )
    }

    private class FakeProjectApi : ProjectApi {
        val projects = mutableListOf<Project>()
        val invitations = mutableListOf<ProjectInvitation>()
        val expensesByProject = mutableMapOf<String, List<Expense>>()
        val tranchesByProject = mutableMapOf<String, List<TranchePayment>>()
        val loadedProjectIds = mutableListOf<String>()
        val failMemberProjects = mutableSetOf<String>()
        var failExpenseCreation = false
        var failMemberLoad = false
        var profileNotFound = false
        var lastExpenseProjectId: String? = null
        var lastExpensePayload: CreateExpensePayload? = null
        var lastProjectCategories: List<String> = emptyList()
        var lastTrancheProjectId: String? = null
        var lastTranchePayload: CreateTranchePayload? = null
        var lastRevenueProjectId: String? = null
        var lastRevenuePayload: RevenueSplitPayload? = null
        var getProjectsGate: CompletableDeferred<Unit>? = null
        var getProjectsCalls: Int = 0
        var getProfileGate: CompletableDeferred<Unit>? = null
        var profileRequestCancelled: Boolean = false

        override suspend fun getProfile(baseUrl: String, token: String): ProjectUser {
            if (profileNotFound) throw ApiException(404, "Profile not registered")
            try {
                getProfileGate?.await()
            } catch (cancelled: CancellationException) {
                profileRequestCancelled = true
                throw cancelled
            }
            return USER
        }

        override suspend fun register(
            baseUrl: String,
            token: String,
            username: String,
            displayName: String
        ) =
            USER.copy(username = username, displayName = displayName)

        override suspend fun getProjects(baseUrl: String, token: String): MyProjectsResponse {
            getProjectsCalls++
            getProjectsGate?.await()
            return MyProjectsResponse(
                projects = projects.toList(),
                defaultProjectId = projects.singleOrNull()?.id
            )
        }

        override suspend fun getInvitations(baseUrl: String, token: String) = invitations.toList()

        override suspend fun createProject(
            baseUrl: String,
            token: String,
            name: String,
            description: String,
            expenseCategories: List<String>
        ): Project {
            lastProjectCategories = expenseCategories
            return project("created", name, description).copy(expenseCategories = expenseCategories)
                .also(projects::add)
        }

        override suspend fun invite(
            baseUrl: String,
            token: String,
            projectId: String,
            username: String
        ): ProjectInvitation = invitation().also(invitations::add)

        override suspend fun respondToInvitation(
            baseUrl: String,
            token: String,
            invitationId: String,
            accept: Boolean
        ): ProjectInvitation {
            val old = invitations.first { it.id == invitationId }
            val updated =
                old.copy(status = if (accept) ProjectInvitationStatus.ACCEPTED else ProjectInvitationStatus.DECLINED)
            invitations[invitations.indexOf(old)] = updated
            if (accept) projects += project(old.projectId, old.projectName)
            return updated
        }

        override suspend fun getMembers(
            baseUrl: String,
            token: String,
            projectId: String
        ): List<ProjectMemberResponse> {
            loadedProjectIds += projectId
            if (failMemberLoad || projectId in failMemberProjects) throw ApiException(
                503,
                "Unavailable"
            )
            return listOf(
                ProjectMemberResponse(
                    USER,
                    ProjectMembership(projectId, USER.id, ProjectMemberRole.OWNER, 0)
                )
            )
        }

        override suspend fun getExpenses(baseUrl: String, token: String, projectId: String) =
            expensesByProject[projectId].orEmpty()

        override suspend fun createExpense(
            baseUrl: String,
            token: String,
            projectId: String,
            payload: CreateExpensePayload
        ): Expense {
            if (failExpenseCreation) throw ApiException(403, "Forbidden")
            lastExpenseProjectId = projectId
            lastExpensePayload = payload
            return Expense(
                id = "expense-1",
                title = payload.title,
                category = ExpenseCategory.fromString(payload.category),
                totalAmount = payload.totalAmount,
                transactionDate = LocalDate.parse(payload.transactionDate),
                payingPartnerId = payload.payingPartnerId,
                notes = payload.notes,
                categoryName = payload.categoryName
            ).also { expensesByProject[projectId] = expensesByProject[projectId].orEmpty() + it }
        }

        override suspend fun deleteExpense(
            baseUrl: String,
            token: String,
            projectId: String,
            expenseId: String
        ) {
            expensesByProject[projectId] =
                expensesByProject[projectId].orEmpty().filterNot { it.id == expenseId }
        }

        override suspend fun getTranches(baseUrl: String, token: String, projectId: String) =
            tranchesByProject[projectId].orEmpty()

        override suspend fun createTranche(
            baseUrl: String,
            token: String,
            projectId: String,
            payload: CreateTranchePayload
        ): TranchePayment {
            lastTrancheProjectId = projectId
            lastTranchePayload = payload
            return TranchePayment(
                "tranche-1",
                payload.partnerId,
                payload.amount,
                LocalDate.parse(payload.paymentDate)
            )
                .also {
                    tranchesByProject[projectId] =
                        listOf(it) + tranchesByProject[projectId].orEmpty()
                }
        }

        override suspend fun calculateRevenueSplit(
            baseUrl: String,
            token: String,
            projectId: String,
            payload: RevenueSplitPayload
        ): SeasonRevenueCalculation {
            lastRevenueProjectId = projectId
            lastRevenuePayload = payload
            return SeasonRevenueCalculation(
                grossFactoryPayout = payload.grossFactoryPayout,
                harvestOperationsExpenses = payload.harvestOperationsExpenses,
                netFactoryPayout = payload.grossFactoryPayout - payload.harvestOperationsExpenses,
                totalProjectInvestment = 0.0,
                partnerSettlements = emptyList()
            )
        }
    }

    companion object {
        private const val SERVICE_URL = "https://ledger.test"
        private const val TOKEN = "session-token"
        private val USER =
            ProjectUser("user-1", "partner_one", "Partner One", "partner@example.test")

        private fun project(id: String, name: String = id, description: String = "") =
            Project(id, name, description, USER.id, 0)

        private fun expense(id: String) = Expense(
            id = id,
            title = id,
            category = ExpenseCategory.MISCELLANEOUS,
            totalAmount = 100.0,
            transactionDate = LocalDate(2026, 4, 1),
            payingPartnerId = USER.id
        )

        private fun invitation() = ProjectInvitation(
            id = "invite-1",
            projectId = "invited-project",
            projectName = "Invited project",
            inviterUserId = "owner-1",
            inviterUsername = "owner_name",
            inviteeUserId = USER.id,
            inviteeUsername = USER.username,
            status = ProjectInvitationStatus.PENDING,
            createdAtEpochMs = 0
        )
    }
}