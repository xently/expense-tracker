package co.ke.xently.expense.tracker.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.ke.xently.expense.tracker.calculator.LedgerCalculator
import co.ke.xently.expense.tracker.calculator.RevenueSplitCalculator
import co.ke.xently.expense.tracker.data.ApiException
import co.ke.xently.expense.tracker.data.CachedProjectMember
import co.ke.xently.expense.tracker.data.CachedProjectSnapshot
import co.ke.xently.expense.tracker.data.CreateExpensePayload
import co.ke.xently.expense.tracker.data.CreateTranchePayload
import co.ke.xently.expense.tracker.data.InMemoryWorkspaceCache
import co.ke.xently.expense.tracker.data.ProjectApi
import co.ke.xently.expense.tracker.data.RevenueSplitPayload
import co.ke.xently.expense.tracker.data.SavedThemePreference
import co.ke.xently.expense.tracker.data.ThemePreferenceStore
import co.ke.xently.expense.tracker.data.WorkspaceCache
import co.ke.xently.expense.tracker.model.Expense
import co.ke.xently.expense.tracker.model.ExpenseCategory
import co.ke.xently.expense.tracker.model.Partner
import co.ke.xently.expense.tracker.model.PartnerLedgerSummary
import co.ke.xently.expense.tracker.model.Project
import co.ke.xently.expense.tracker.model.ProjectInvitation
import co.ke.xently.expense.tracker.model.ProjectMemberRole
import co.ke.xently.expense.tracker.model.ProjectMembership
import co.ke.xently.expense.tracker.model.ProjectUser
import co.ke.xently.expense.tracker.model.SeasonRevenueCalculation
import co.ke.xently.expense.tracker.model.TranchePayment
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

enum class WorkspacePhase {
    CREDENTIALS,
    REGISTRATION,
    PROJECTS,
    PROJECT
}

enum class ThemePreference {
    SYSTEM,
    LIGHT,
    DARK
}

enum class NavigationTab {
    DASHBOARD,
    EXPENSES,
    PARTNERS,
    REVENUE_SPLIT,
    NOTIFICATIONS,
    INVITATIONS
}

enum class WorkspaceForm {
    EXPENSE,
    TRANCHE
}

enum class WorkspaceMessage {
    PROJECT_CREATED,
    INVITATION_SENT,
    EXPENSE_SAVED,
    TRANCHE_SAVED
}

sealed interface WorkspaceIssue {
    data object CredentialsRequired : WorkspaceIssue
    data object ProjectRequired : WorkspaceIssue
    data object ProfileRequired : WorkspaceIssue
    data object PayoutValuesRequired : WorkspaceIssue
    data class RequestFailed(val statusCode: Int?) : WorkspaceIssue
}

data class ProjectWorkspaceState(
    val phase: WorkspacePhase = WorkspacePhase.CREDENTIALS,
    val apiBaseUrl: String = "",
    val accessToken: String = "",
    val profile: ProjectUser? = null,
    val projects: List<Project> = emptyList(),
    val selectedProjectId: String? = null,
    val memberships: List<Pair<ProjectMembership, ProjectUser>> = emptyList(),
    val invitations: List<ProjectInvitation> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val tranches: List<TranchePayment> = emptyList(),
    val selectedTab: NavigationTab = NavigationTab.DASHBOARD,
    val themePreference: ThemePreference = ThemePreference.SYSTEM,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: WorkspaceIssue? = null,
    val message: WorkspaceMessage? = null,
    val activeForm: WorkspaceForm? = null,
    val grossPayout: String = "",
    val harvestCosts: String = "",
    val revenueCalculation: SeasonRevenueCalculation? = null
) {
    val selectedProject: Project? get() = projects.firstOrNull { it.id == selectedProjectId }
    val isProjectOwner: Boolean
        get() = memberships.any {
            it.first.userId == profile?.id && it.first.role == ProjectMemberRole.OWNER
        }
    val partners: List<Partner>
        get() = memberships.map { (_, user) ->
            Partner(id = user.id, name = user.displayName, email = user.email)
        }
    val partnerSummaries: List<PartnerLedgerSummary>
        get() = LedgerCalculator.computePartnerSummaries(
            expenses = expenses,
            tranches = tranches,
            partners = partners,
            asOfDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        )
    val revenueSplit
        get() = grossPayout.toDoubleOrNull()?.let { gross ->
            val costs = harvestCosts.toDoubleOrNull() ?: return@let null
            RevenueSplitCalculator.calculate(gross, costs, partnerSummaries)
        }
}

sealed interface ProjectWorkspaceEvent {
    data class Connect(val apiBaseUrl: String, val accessToken: String) : ProjectWorkspaceEvent
    data class Register(val username: String, val displayName: String) : ProjectWorkspaceEvent
    data class CreateProject(
        val name: String,
        val description: String,
        val expenseCategories: List<String> = emptyList()
    ) : ProjectWorkspaceEvent

    data class SelectProject(val projectId: String) : ProjectWorkspaceEvent
    data object BackToProjects : ProjectWorkspaceEvent
    data object ReturnToProject : ProjectWorkspaceEvent
    data class InvitePartner(val username: String) : ProjectWorkspaceEvent
    data class RespondToInvitation(val invitationId: String, val accept: Boolean) :
        ProjectWorkspaceEvent

    data class CreateExpense(
        val title: String,
        val category: ExpenseCategory,
        val amount: Double,
        val date: LocalDate,
        val payingPartnerId: String,
        val notes: String,
        val categoryName: String? = null
    ) : ProjectWorkspaceEvent

    data class DeleteExpense(val expenseId: String) : ProjectWorkspaceEvent
    data class CreateTranche(
        val amount: Double,
        val date: LocalDate,
        val referenceNumber: String,
        val notes: String
    ) : ProjectWorkspaceEvent

    data class SelectTab(val tab: NavigationTab) : ProjectWorkspaceEvent
    data object OpenExpenseForm : ProjectWorkspaceEvent
    data object OpenTrancheForm : ProjectWorkspaceEvent
    data object DismissForm : ProjectWorkspaceEvent
    data class SetTheme(val preference: ThemePreference) : ProjectWorkspaceEvent
    data class UpdateRevenue(val grossPayout: String, val harvestCosts: String) :
        ProjectWorkspaceEvent

    data object CalculateRevenueSplit : ProjectWorkspaceEvent
    data object Refresh : ProjectWorkspaceEvent
    data object DismissMessage : ProjectWorkspaceEvent
    data object Disconnect : ProjectWorkspaceEvent
}

class ProjectWorkspaceViewModel(
    private val api: ProjectApi,
    private val cache: WorkspaceCache = InMemoryWorkspaceCache(),
    private val themePreferenceStore: ThemePreferenceStore? = null
) : ViewModel() {
    private val _state = MutableStateFlow(ProjectWorkspaceState())
    val state: StateFlow<ProjectWorkspaceState> = _state.asStateFlow()

    init {
        themePreferenceStore?.let { store ->
            viewModelScope.launch {
                store.preference.collect { saved ->
                    _state.update { it.copy(themePreference = saved.toThemePreference()) }
                }
            }
        }
    }

    fun onEvent(event: ProjectWorkspaceEvent) {
        when (event) {
            is ProjectWorkspaceEvent.Connect -> connect(event.apiBaseUrl, event.accessToken)
            is ProjectWorkspaceEvent.Register -> register(event.username, event.displayName)
            is ProjectWorkspaceEvent.CreateProject -> createProject(
                name = event.name,
                description = event.description,
                expenseCategories = event.expenseCategories
            )

            is ProjectWorkspaceEvent.SelectProject -> selectProject(event.projectId)
            ProjectWorkspaceEvent.BackToProjects -> _state.update {
                it.copy(phase = WorkspacePhase.PROJECTS, activeForm = null)
            }

            ProjectWorkspaceEvent.ReturnToProject -> _state.update {
                if (it.phase == WorkspacePhase.PROJECTS && it.selectedProjectId != null) {
                    it.copy(phase = WorkspacePhase.PROJECT, activeForm = null)
                } else it
            }

            is ProjectWorkspaceEvent.InvitePartner -> invitePartner(event.username)
            is ProjectWorkspaceEvent.RespondToInvitation -> respondToInvitation(
                event.invitationId,
                event.accept
            )

            is ProjectWorkspaceEvent.CreateExpense -> createExpense(event)
            is ProjectWorkspaceEvent.DeleteExpense -> deleteExpense(event.expenseId)
            is ProjectWorkspaceEvent.CreateTranche -> createTranche(event)
            is ProjectWorkspaceEvent.SelectTab -> _state.update { it.copy(selectedTab = event.tab) }
            ProjectWorkspaceEvent.OpenExpenseForm -> _state.update {
                if (it.phase == WorkspacePhase.PROJECT) it.copy(activeForm = WorkspaceForm.EXPENSE) else it
            }

            ProjectWorkspaceEvent.OpenTrancheForm -> _state.update {
                if (it.phase == WorkspacePhase.PROJECT) it.copy(activeForm = WorkspaceForm.TRANCHE) else it
            }

            ProjectWorkspaceEvent.DismissForm -> _state.update { it.copy(activeForm = null) }
            is ProjectWorkspaceEvent.SetTheme -> {
                _state.update { it.copy(themePreference = event.preference) }
                themePreferenceStore?.let { store ->
                    viewModelScope.launch { store.save(event.preference.toSavedThemePreference()) }
                }
            }

            is ProjectWorkspaceEvent.UpdateRevenue -> _state.update {
                it.copy(
                    grossPayout = event.grossPayout,
                    harvestCosts = event.harvestCosts,
                    revenueCalculation = null
                )
            }

            ProjectWorkspaceEvent.CalculateRevenueSplit -> calculateRevenueSplit()
            ProjectWorkspaceEvent.Refresh -> refresh()
            ProjectWorkspaceEvent.DismissMessage -> _state.update {
                it.copy(
                    message = null,
                    error = null
                )
            }

            ProjectWorkspaceEvent.Disconnect -> _state.value =
                ProjectWorkspaceState(themePreference = _state.value.themePreference)
        }
    }

    private fun connect(apiBaseUrl: String, accessToken: String) {
        val base = apiBaseUrl.trim().trimEnd('/')
        val token = accessToken.trim()
        if (base.isBlank() || token.isBlank()) {
            _state.update { it.copy(error = WorkspaceIssue.CredentialsRequired) }
            return
        }
        _state.update { it.copy(apiBaseUrl = base, accessToken = token, error = null) }
        launchRequest {
            try {
                val profile = api.getProfile(base, token)
                _state.update { it.copy(profile = profile) }
                loadWorkspace()
            } catch (failure: ApiException) {
                if (failure.statusCode == 404) {
                    _state.update { it.copy(phase = WorkspacePhase.REGISTRATION) }
                } else {
                    throw failure
                }
            }
        }
    }

    private fun register(username: String, displayName: String) = launchRequest {
        val current = _state.value
        val profile = api.register(
            current.apiBaseUrl,
            current.accessToken,
            username.trim(),
            displayName.trim()
        )
        _state.update { it.copy(profile = profile) }
        loadWorkspace()
    }

    private suspend fun loadWorkspace(preferredProjectId: String? = _state.value.selectedProjectId) {
        val current = _state.value
        val projects = api.getProjects(current.apiBaseUrl, current.accessToken).projects
        val invitations = api.getInvitations(current.apiBaseUrl, current.accessToken)
        val automaticProjectId = projects.singleOrNull()?.id
        val selectedId =
            automaticProjectId ?: preferredProjectId?.takeIf { id -> projects.any { it.id == id } }
        _state.update {
            it.copy(
                projects = projects,
                invitations = invitations,
                selectedProjectId = selectedId,
                memberships = if (selectedId == it.selectedProjectId) it.memberships else emptyList(),
                expenses = if (selectedId == it.selectedProjectId) it.expenses else emptyList(),
                tranches = if (selectedId == it.selectedProjectId) it.tranches else emptyList(),
                phase = if (selectedId == null) WorkspacePhase.PROJECTS else WorkspacePhase.PROJECT
            )
        }
        if (selectedId != null) loadProjectData(selectedId)
    }

    private fun createProject(name: String, description: String, expenseCategories: List<String>) =
        launchRequest {
            val current = _state.value
            val project = api.createProject(
                current.apiBaseUrl,
                current.accessToken,
                name.trim(),
                description.trim(),
                expenseCategories.map(String::trim)
            )
            _state.update { it.copy(message = WorkspaceMessage.PROJECT_CREATED) }
            loadWorkspace(preferredProjectId = project.id)
        }

    private fun selectProject(projectId: String) {
        if (_state.value.projects.none { it.id == projectId }) return
        _state.update {
            it.copy(
                selectedProjectId = projectId,
                memberships = emptyList(),
                expenses = emptyList(),
                tranches = emptyList(),
                phase = WorkspacePhase.PROJECT,
                activeForm = null
            )
        }
        launchRequest { loadProjectData(projectId) }
    }

    private suspend fun loadProjectData(projectId: String) {
        val current = _state.value
        val cached = try {
            cache.read(projectId)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Throwable) {
            null
        }
        if (cached != null) {
            _state.update {
                if (it.selectedProjectId != projectId) it else it.copy(
                    memberships = cached.members.map { member -> member.membership to member.user },
                    expenses = cached.expenses,
                    tranches = cached.tranches,
                    phase = WorkspacePhase.PROJECT
                )
            }
        }
        val members = api.getMembers(current.apiBaseUrl, current.accessToken, projectId)
        val expenses = api.getExpenses(current.apiBaseUrl, current.accessToken, projectId)
        val tranches = api.getTranches(current.apiBaseUrl, current.accessToken, projectId)
        _state.update {
            if (it.selectedProjectId != projectId) it else it.copy(
                memberships = members.map { member -> member.membership to member.user },
                expenses = expenses,
                tranches = tranches,
                phase = WorkspacePhase.PROJECT
            )
        }
        saveSnapshot(projectId)
    }

    private fun invitePartner(username: String) {
        if (_state.value.selectedProjectId == null) {
            _state.update { it.copy(error = WorkspaceIssue.ProjectRequired) }
            return
        }
        launchRequest {
            val current = _state.value
            val projectId = current.selectedProjectId ?: return@launchRequest
            api.invite(current.apiBaseUrl, current.accessToken, projectId, username.trim())
            _state.update { it.copy(message = WorkspaceMessage.INVITATION_SENT) }
        }
    }

    private fun respondToInvitation(invitationId: String, accept: Boolean) = launchRequest {
        val current = _state.value
        api.respondToInvitation(current.apiBaseUrl, current.accessToken, invitationId, accept)
        loadWorkspace()
    }

    private fun createExpense(event: ProjectWorkspaceEvent.CreateExpense) {
        if (_state.value.selectedProjectId == null) {
            _state.update { it.copy(error = WorkspaceIssue.ProjectRequired) }
            return
        }
        launchRequest {
            val current = _state.value
            val projectId = current.selectedProjectId ?: return@launchRequest
            val saved = api.createExpense(
                current.apiBaseUrl,
                current.accessToken,
                projectId,
                CreateExpensePayload(
                    title = event.title.trim(),
                    category = event.category.name,
                    categoryName = event.categoryName?.trim()?.takeIf(String::isNotEmpty),
                    totalAmount = event.amount,
                    transactionDate = event.date.toString(),
                    payingPartnerId = event.payingPartnerId,
                    notes = event.notes.trim()
                )
            )
            _state.update {
                it.copy(
                    expenses = (it.expenses + saved).sortedByDescending(Expense::transactionDate),
                    message = WorkspaceMessage.EXPENSE_SAVED
                )
            }
            saveSnapshot(projectId)
        }
    }

    private fun deleteExpense(expenseId: String) {
        if (_state.value.selectedProjectId == null) {
            _state.update { it.copy(error = WorkspaceIssue.ProjectRequired) }
            return
        }
        launchRequest {
            val current = _state.value
            val projectId = current.selectedProjectId ?: return@launchRequest
            api.deleteExpense(current.apiBaseUrl, current.accessToken, projectId, expenseId)
            _state.update { it.copy(expenses = it.expenses.filterNot { expense -> expense.id == expenseId }) }
            saveSnapshot(projectId)
        }
    }

    private fun createTranche(event: ProjectWorkspaceEvent.CreateTranche) {
        if (_state.value.selectedProjectId == null) {
            _state.update { it.copy(error = WorkspaceIssue.ProjectRequired) }
            return
        }
        if (_state.value.profile == null) {
            _state.update { it.copy(error = WorkspaceIssue.ProfileRequired) }
            return
        }
        launchRequest {
            val current = _state.value
            val projectId = current.selectedProjectId ?: return@launchRequest
            val userId = current.profile?.id ?: return@launchRequest
            val saved = api.createTranche(
                current.apiBaseUrl,
                current.accessToken,
                projectId,
                CreateTranchePayload(
                    partnerId = userId,
                    amount = event.amount,
                    paymentDate = event.date.toString(),
                    referenceNumber = event.referenceNumber.trim(),
                    notes = event.notes.trim()
                )
            )
            _state.update {
                it.copy(
                    tranches = listOf(saved) + it.tranches,
                    message = WorkspaceMessage.TRANCHE_SAVED
                )
            }
            saveSnapshot(projectId)
        }
    }

    private suspend fun saveSnapshot(projectId: String) {
        val current = _state.value
        val project = current.selectedProject?.takeIf { it.id == projectId } ?: return
        try {
            cache.write(
                CachedProjectSnapshot(
                    project = project,
                    members = current.memberships.map { (membership, user) ->
                        CachedProjectMember(
                            membership,
                            user
                        )
                    },
                    expenses = current.expenses,
                    tranches = current.tranches
                )
            )
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Throwable) {
            // A cache write must not turn a successful server write into a reported failure.
        }
    }

    private fun refresh() {
        val current = _state.value
        if (current.profile == null || current.isRefreshing) return
        launchRequest(isRefreshing = true) { loadWorkspace() }
    }

    private fun calculateRevenueSplit() {
        val current = _state.value
        val projectId = current.selectedProjectId
        if (projectId == null) {
            _state.update { it.copy(error = WorkspaceIssue.ProjectRequired) }
            return
        }
        val grossPayout = current.grossPayout.toDoubleOrNull()
        val harvestCosts = current.harvestCosts.toDoubleOrNull()
        if (grossPayout == null || harvestCosts == null || !grossPayout.isFinite() || !harvestCosts.isFinite() ||
            grossPayout < 0 || harvestCosts < 0
        ) {
            _state.update { it.copy(error = WorkspaceIssue.PayoutValuesRequired) }
            return
        }
        launchRequest {
            val calculation = api.calculateRevenueSplit(
                current.apiBaseUrl,
                current.accessToken,
                projectId,
                RevenueSplitPayload(grossPayout, harvestCosts)
            )
            _state.update { it.copy(revenueCalculation = calculation) }
        }
    }

    private fun launchRequest(isRefreshing: Boolean = false, block: suspend () -> Unit) {
        viewModelScope.launch {
            _state.update {
                it.copy(
                    isLoading = true,
                    isRefreshing = it.isRefreshing || isRefreshing,
                    error = null,
                    message = null
                )
            }
            try {
                block()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (failure: Throwable) {
                val issue = WorkspaceIssue.RequestFailed((failure as? ApiException)?.statusCode)
                _state.update { it.copy(error = issue) }
            } finally {
                _state.update {
                    it.copy(
                        isLoading = false,
                        isRefreshing = !isRefreshing && it.isRefreshing
                    )
                }
            }
        }
    }

}

private fun SavedThemePreference.toThemePreference(): ThemePreference = when (this) {
    SavedThemePreference.SYSTEM -> ThemePreference.SYSTEM
    SavedThemePreference.LIGHT -> ThemePreference.LIGHT
    SavedThemePreference.DARK -> ThemePreference.DARK
}

private fun ThemePreference.toSavedThemePreference(): SavedThemePreference = when (this) {
    ThemePreference.SYSTEM -> SavedThemePreference.SYSTEM
    ThemePreference.LIGHT -> SavedThemePreference.LIGHT
    ThemePreference.DARK -> SavedThemePreference.DARK
}