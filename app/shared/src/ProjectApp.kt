package co.ke.xently.expense.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import co.ke.xently.expense.tracker.data.KtorProjectApi
import co.ke.xently.expense.tracker.data.ProjectApi
import co.ke.xently.expense.tracker.data.ThemePreferenceStore
import co.ke.xently.expense.tracker.data.WorkspaceCache
import co.ke.xently.expense.tracker.data.createThemePreferenceStore
import co.ke.xently.expense.tracker.data.createWorkspaceCache
import co.ke.xently.expense.tracker.model.ComplianceStatus
import co.ke.xently.expense.tracker.model.ExpenseCategory
import co.ke.xently.expense.tracker.model.ProjectInvitation
import co.ke.xently.expense.tracker.resources.Res
import co.ke.xently.expense.tracker.resources.accept
import co.ke.xently.expense.tracker.resources.amount_kes
import co.ke.xently.expense.tracker.resources.cancel
import co.ke.xently.expense.tracker.resources.category
import co.ke.xently.expense.tracker.resources.category_chopping
import co.ke.xently.expense.tracker.resources.category_fertilizer
import co.ke.xently.expense.tracker.resources.category_harvest_operations
import co.ke.xently.expense.tracker.resources.category_land_prep
import co.ke.xently.expense.tracker.resources.category_miscellaneous
import co.ke.xently.expense.tracker.resources.category_seedlings
import co.ke.xently.expense.tracker.resources.category_transport
import co.ke.xently.expense.tracker.resources.category_weeding
import co.ke.xently.expense.tracker.resources.decline
import co.ke.xently.expense.tracker.resources.expense_title
import co.ke.xently.expense.tracker.resources.invitation_from
import co.ke.xently.expense.tracker.resources.invite_partner
import co.ke.xently.expense.tracker.resources.invite_username
import co.ke.xently.expense.tracker.resources.log_expense
import co.ke.xently.expense.tracker.resources.notes
import co.ke.xently.expense.tracker.resources.paid_by
import co.ke.xently.expense.tracker.resources.payment_date
import co.ke.xently.expense.tracker.resources.record_tranche
import co.ke.xently.expense.tracker.resources.reference_number
import co.ke.xently.expense.tracker.resources.save
import co.ke.xently.expense.tracker.resources.select_date
import co.ke.xently.expense.tracker.resources.send_invitation
import co.ke.xently.expense.tracker.resources.status_overdue
import co.ke.xently.expense.tracker.resources.status_paid
import co.ke.xently.expense.tracker.resources.status_partial
import co.ke.xently.expense.tracker.resources.tranche_amount
import co.ke.xently.expense.tracker.resources.transaction_date
import co.ke.xently.expense.tracker.theme.ExpenseTrackerTheme
import co.ke.xently.expense.tracker.ui.PreviewFontScale
import co.ke.xently.expense.tracker.ui.WorkspaceScaffold
import co.ke.xently.expense.tracker.ui.destination
import co.ke.xently.expense.tracker.ui.previewWorkspaceState
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceEvent
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceState
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceViewModel
import co.ke.xently.expense.tracker.viewmodel.ThemePreference
import co.ke.xently.expense.tracker.viewmodel.WorkspaceForm
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.jetbrains.compose.resources.stringResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.startKoin
import org.koin.dsl.module
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
internal sealed interface WorkspaceDestination : NavKey {
    @Serializable
    data object Dashboard : WorkspaceDestination

    @Serializable
    data object Expenses : WorkspaceDestination

    @Serializable
    data object Partners : WorkspaceDestination

    @Serializable
    data object Settlement : WorkspaceDestination

    @Serializable
    data object Notifications : WorkspaceDestination

    @Serializable
    data object Invitations : WorkspaceDestination
}

private val workspaceNavigationConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(
                WorkspaceDestination.Dashboard::class,
                WorkspaceDestination.Dashboard.serializer()
            )
            subclass(
                WorkspaceDestination.Expenses::class,
                WorkspaceDestination.Expenses.serializer()
            )
            subclass(
                WorkspaceDestination.Partners::class,
                WorkspaceDestination.Partners.serializer()
            )
            subclass(
                WorkspaceDestination.Settlement::class,
                WorkspaceDestination.Settlement.serializer()
            )
            subclass(
                WorkspaceDestination.Notifications::class,
                WorkspaceDestination.Notifications.serializer()
            )
            subclass(
                WorkspaceDestination.Invitations::class,
                WorkspaceDestination.Invitations.serializer()
            )
        }
    }
}

private object WorkspaceDi : KoinComponent {
    private val application by lazy {
        startKoin {
            modules(module {
                single<ProjectApi> { KtorProjectApi() }
                single<WorkspaceCache> { createWorkspaceCache() }
                single<ThemePreferenceStore> { createThemePreferenceStore() }
                single { ProjectWorkspaceViewModel(get(), get(), get()) }
            })
        }.koin
    }

    fun viewModel(): ProjectWorkspaceViewModel {
        application
        return get()
    }
}

@Composable
fun App() {
    val workspace = remember { WorkspaceDi.viewModel() }
    val state by workspace.state.collectAsState()
    ProjectAppContent(state, workspace::onEvent)
}

@Composable
internal fun ProjectAppContent(
    state: ProjectWorkspaceState,
    onEvent: (ProjectWorkspaceEvent) -> Unit
) {
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val darkTheme = when (state.themePreference) {
        ThemePreference.SYSTEM -> isSystemDark
        ThemePreference.LIGHT -> false
        ThemePreference.DARK -> true
    }

    ExpenseTrackerTheme(darkTheme = darkTheme) {
        val backStack = rememberNavBackStack(
            workspaceNavigationConfiguration,
            WorkspaceDestination.Dashboard
        )
        LaunchedEffect(state.selectedTab) {
            val destination = state.selectedTab.destination()
            if (backStack.lastOrNull() != destination) {
                backStack.clear()
                backStack.add(destination)
            }
        }
        WorkspaceScaffold(state, backStack, onEvent)
        if (state.phase == co.ke.xently.expense.tracker.viewmodel.WorkspacePhase.PROJECT) {
            when (state.activeForm) {
                WorkspaceForm.EXPENSE -> ExpenseDialog(state, onEvent) {
                    onEvent(ProjectWorkspaceEvent.DismissForm)
                }

                WorkspaceForm.TRANCHE -> TrancheDialog(onEvent) {
                    onEvent(ProjectWorkspaceEvent.DismissForm)
                }

                null -> Unit
            }
        }
    }
}


@Composable
internal fun InvitationCard(
    invitation: ProjectInvitation,
    onEvent: (ProjectWorkspaceEvent) -> Unit
) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(invitation.projectName, style = MaterialTheme.typography.titleMedium)
            Text(stringResource(Res.string.invitation_from, invitation.inviterUsername))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    onEvent(
                        ProjectWorkspaceEvent.RespondToInvitation(
                            invitation.id,
                            true
                        )
                    )
                }) { Text(stringResource(Res.string.accept)) }
                OutlinedButton(onClick = {
                    onEvent(
                        ProjectWorkspaceEvent.RespondToInvitation(
                            invitation.id,
                            false
                        )
                    )
                }) { Text(stringResource(Res.string.decline)) }
            }
        }
    }
}

@PreviewLightDark
@PreviewFontScale
@PreviewDynamicColors
@PreviewScreenSizes
@Composable
private fun ProjectAppWorkspacePreview() {
    var state by remember {
        mutableStateOf(previewWorkspaceState.copy(phase = co.ke.xently.expense.tracker.viewmodel.WorkspacePhase.PROJECT))
    }
    ProjectAppContent(state) { event ->
        if (event is ProjectWorkspaceEvent.SelectTab) {
            state = state.copy(selectedTab = event.tab)
        }
    }
}

@PreviewLightDark
@PreviewFontScale
@Preview
@Composable
private fun ProjectAppCredentialsPreview() {
    ProjectAppContent(ProjectWorkspaceState()) {}
}

@PreviewLightDark
@Composable
private fun InvitationCardPreview() {
    ExpenseTrackerTheme {
        InvitationCard(previewWorkspaceState.invitations.first()) {}
    }
}

@PreviewLightDark
@Composable
private fun InvitePartnerFormPreview() {
    ExpenseTrackerTheme { InvitePartnerForm {} }
}

@PreviewLightDark
@Composable
private fun ExpenseDialogPreview() {
    ExpenseTrackerTheme { ExpenseDialog(previewWorkspaceState, {}, {}) }
}

@PreviewLightDark
@Composable
private fun TrancheDialogPreview() {
    ExpenseTrackerTheme { TrancheDialog({}, {}) }
}

@Composable
internal fun InvitePartnerForm(onEvent: (ProjectWorkspaceEvent) -> Unit) {
    var username by remember { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(Res.string.invite_partner),
            style = MaterialTheme.typography.titleMedium
        )
        OutlinedTextField(
            username,
            { username = it },
            label = { Text(stringResource(Res.string.invite_username)) },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { onEvent(ProjectWorkspaceEvent.InvitePartner(username)); username = "" },
            enabled = username.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.send_invitation))
        }
    }
}

@Composable
internal fun ExpenseDialog(
    state: ProjectWorkspaceState,
    onEvent: (ProjectWorkspaceEvent) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategory.MISCELLANEOUS) }
    var payer by remember { mutableStateOf(state.profile?.id.orEmpty()) }
    var notes by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.log_expense)) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        title,
                        { title = it },
                        label = { Text(stringResource(Res.string.expense_title)) })
                }
                item {
                    OutlinedTextField(
                        amount,
                        { amount = it },
                        label = { Text(stringResource(Res.string.amount_kes)) })
                }
                item { CategoryPicker(category) { category = it } }
                item { PartnerPicker(state, payer) { payer = it } }
                item {
                    DatePickerField(stringResource(Res.string.transaction_date), date) {
                        date = it
                    }
                }
                item {
                    OutlinedTextField(
                        notes,
                        { notes = it },
                        label = { Text(stringResource(Res.string.notes)) })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val value = amount.toDoubleOrNull()
                if (title.isNotBlank() && value != null && value > 0 && payer.isNotBlank()) {
                    onEvent(
                        ProjectWorkspaceEvent.CreateExpense(
                            title,
                            category,
                            value,
                            date,
                            payer,
                            notes
                        )
                    )
                    onDismiss()
                }
            }) { Text(stringResource(Res.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) } }
    )
}

@Composable
internal fun TrancheDialog(onEvent: (ProjectWorkspaceEvent) -> Unit, onDismiss: () -> Unit) {
    var amount by remember { mutableStateOf("") }
    var reference by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(today()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.record_tranche)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    amount,
                    { amount = it },
                    label = { Text(stringResource(Res.string.tranche_amount)) })
                DatePickerField(stringResource(Res.string.payment_date), date) { date = it }
                OutlinedTextField(
                    reference,
                    { reference = it },
                    label = { Text(stringResource(Res.string.reference_number)) })
                OutlinedTextField(
                    notes,
                    { notes = it },
                    label = { Text(stringResource(Res.string.notes)) })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val value = amount.toDoubleOrNull()
                if (value != null && value > 0) {
                    onEvent(ProjectWorkspaceEvent.CreateTranche(value, date, reference, notes))
                    onDismiss()
                }
            }) { Text(stringResource(Res.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(Res.string.cancel)) } }
    )
}

@Composable
private fun CategoryPicker(category: ExpenseCategory, onSelect: (ExpenseCategory) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = {
            expanded = true
        }) { Text("${stringResource(Res.string.category)}: ${categoryLabel(category)}") }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            ExpenseCategory.entries.forEach { entry ->
                DropdownMenuItem(
                    text = { Text(categoryLabel(entry)) },
                    onClick = { onSelect(entry); expanded = false })
            }
        }
    }
}

@Composable
private fun PartnerPicker(
    state: ProjectWorkspaceState,
    partnerId: String,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text("${stringResource(Res.string.paid_by)}: ${state.partners.firstOrNull { it.id == partnerId }?.name.orEmpty()}")
        }
        DropdownMenu(expanded, onDismissRequest = { expanded = false }) {
            state.partners.forEach { partner ->
                DropdownMenuItem(
                    text = { Text(partner.name) },
                    onClick = { onSelect(partner.id); expanded = false })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerField(label: String, selected: LocalDate, onSelect: (LocalDate) -> Unit) {
    var open by remember { mutableStateOf(false) }
    OutlinedButton(
        onClick = { open = true },
        modifier = Modifier.fillMaxWidth()
    ) { Text("$label: $selected") }
    if (open) {
        val pickerState =
            rememberDatePickerState(initialSelectedDateMillis = selected.toEpochDays() * 86_400_000L)
        DatePickerDialog(
            onDismissRequest = { open = false },
            confirmButton = {
                TextButton(onClick = {
                    pickerState.selectedDateMillis?.let { millis ->
                        onSelect(
                            Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                        )
                    }
                    open = false
                }) { Text(stringResource(Res.string.select_date)) }
            },
            dismissButton = {
                TextButton(onClick = {
                    open = false
                }) { Text(stringResource(Res.string.cancel)) }
            }
        ) { DatePicker(state = pickerState) }
    }
}

@Composable
internal fun categoryLabel(category: ExpenseCategory): String = stringResource(
    when (category) {
        ExpenseCategory.LAND_PREP -> Res.string.category_land_prep
        ExpenseCategory.SEEDLINGS -> Res.string.category_seedlings
        ExpenseCategory.WEEDING -> Res.string.category_weeding
        ExpenseCategory.FERTILIZER -> Res.string.category_fertilizer
        ExpenseCategory.CHOPPING -> Res.string.category_chopping
        ExpenseCategory.TRANSPORT -> Res.string.category_transport
        ExpenseCategory.HARVEST_OPERATIONS -> Res.string.category_harvest_operations
        ExpenseCategory.MISCELLANEOUS -> Res.string.category_miscellaneous
    }
)

@Composable
internal fun statusLabel(status: ComplianceStatus): String = stringResource(
    when (status) {
        ComplianceStatus.PAID_IN_FULL -> Res.string.status_paid
        ComplianceStatus.PARTIAL -> Res.string.status_partial
        ComplianceStatus.OVERDUE -> Res.string.status_overdue
    }
)

private fun today(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date