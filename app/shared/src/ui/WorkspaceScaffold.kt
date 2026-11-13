package co.ke.xently.expense.tracker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import co.ke.xently.expense.tracker.WorkspaceDestination
import co.ke.xently.expense.tracker.resources.Res
import co.ke.xently.expense.tracker.resources.app_name
import co.ke.xently.expense.tracker.resources.app_tagline
import co.ke.xently.expense.tracker.resources.back_to_project
import co.ke.xently.expense.tracker.resources.change_project
import co.ke.xently.expense.tracker.resources.credentials_required
import co.ke.xently.expense.tracker.resources.dashboard
import co.ke.xently.expense.tracker.resources.expense_saved
import co.ke.xently.expense.tracker.resources.expenses
import co.ke.xently.expense.tracker.resources.invitation_sent
import co.ke.xently.expense.tracker.resources.invitations
import co.ke.xently.expense.tracker.resources.more_actions
import co.ke.xently.expense.tracker.resources.navigation_menu
import co.ke.xently.expense.tracker.resources.notifications
import co.ke.xently.expense.tracker.resources.partner_ledgers
import co.ke.xently.expense.tracker.resources.payout_values_required
import co.ke.xently.expense.tracker.resources.profile_required
import co.ke.xently.expense.tracker.resources.project_created
import co.ke.xently.expense.tracker.resources.project_partners
import co.ke.xently.expense.tracker.resources.project_required
import co.ke.xently.expense.tracker.resources.request_failed
import co.ke.xently.expense.tracker.resources.request_unavailable
import co.ke.xently.expense.tracker.resources.revenue_split
import co.ke.xently.expense.tracker.resources.sign_out
import co.ke.xently.expense.tracker.resources.theme_dark
import co.ke.xently.expense.tracker.resources.theme_light
import co.ke.xently.expense.tracker.resources.theme_options
import co.ke.xently.expense.tracker.resources.theme_system
import co.ke.xently.expense.tracker.resources.tranche_saved
import co.ke.xently.expense.tracker.theme.ExpenseTrackerTheme
import co.ke.xently.expense.tracker.theme.Icons
import co.ke.xently.expense.tracker.viewmodel.NavigationTab
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceEvent
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceState
import co.ke.xently.expense.tracker.viewmodel.ThemePreference
import co.ke.xently.expense.tracker.viewmodel.WorkspaceIssue
import co.ke.xently.expense.tracker.viewmodel.WorkspaceMessage
import co.ke.xently.expense.tracker.viewmodel.WorkspacePhase
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

internal const val ExpandedNavigationWidthDp = 840
internal val primaryNavigationTabs = listOf(
    NavigationTab.DASHBOARD,
    NavigationTab.EXPENSES,
    NavigationTab.PARTNERS,
    NavigationTab.REVENUE_SPLIT
)
internal val drawerOnlyNavigationTabs = listOf(
    NavigationTab.NOTIFICATIONS,
    NavigationTab.INVITATIONS
)
internal val workspaceNavigationTabs = primaryNavigationTabs + drawerOnlyNavigationTabs

internal fun usesPermanentNavigationDrawer(width: Dp): Boolean =
    width >= ExpandedNavigationWidthDp.dp

@Composable
internal fun WorkspaceScaffold(
    state: ProjectWorkspaceState,
    backStack: NavBackStack<NavKey>,
    onEvent: (ProjectWorkspaceEvent) -> Unit
) {
    var showOverflowMenu by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val latestOnEvent by rememberUpdatedState(onEvent)
    val errorMessage = state.error?.let { issueLabel(it) }
    val successMessage = state.message?.let { messageLabel(it) }
    LaunchedEffect(errorMessage, successMessage) {
        listOfNotNull(errorMessage, successMessage).forEach { snackbarHostState.showSnackbar(it) }
        if (errorMessage != null || successMessage != null) {
            latestOnEvent(ProjectWorkspaceEvent.DismissMessage)
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val permanentDrawer = state.phase == WorkspacePhase.PROJECT &&
                usesPermanentNavigationDrawer(maxWidth)
        LaunchedEffect(permanentDrawer, state.phase) {
            drawerState.close()
            showOverflowMenu = false
        }

        if (permanentDrawer) {
            Row(Modifier.fillMaxSize()) {
                Surface(
                    modifier = Modifier
                        .width(280.dp)
                        .fillMaxHeight(),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    WorkspaceNavigationDrawer(
                        selectedTab = state.selectedTab,
                        projectName = state.selectedProject?.name,
                        modifier = Modifier.fillMaxSize(),
                        onSelect = { onEvent(ProjectWorkspaceEvent.SelectTab(it)) }
                    )
                }
                WorkspaceScaffoldLayout(
                    state = state,
                    backStack = backStack,
                    onEvent = onEvent,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    showBottomNavigation = false,
                    showNavigationButton = false,
                    snackbarHostState = snackbarHostState,
                    showOverflowMenu = showOverflowMenu,
                    onOverflowMenuChange = { showOverflowMenu = it }
                )
            }
        } else {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = state.phase == WorkspacePhase.PROJECT,
                drawerContent = {
                    if (state.phase == WorkspacePhase.PROJECT) {
                        ModalDrawerSheet {
                            WorkspaceNavigationDrawer(
                                selectedTab = state.selectedTab,
                                projectName = state.selectedProject?.name,
                                modifier = Modifier.fillMaxHeight(),
                                onSelect = { tab ->
                                    onEvent(ProjectWorkspaceEvent.SelectTab(tab))
                                    scope.launch { drawerState.close() }
                                }
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            ) {
                WorkspaceScaffoldLayout(
                    state = state,
                    backStack = backStack,
                    onEvent = onEvent,
                    showBottomNavigation = state.phase == WorkspacePhase.PROJECT,
                    showNavigationButton = state.phase == WorkspacePhase.PROJECT,
                    snackbarHostState = snackbarHostState,
                    onOpenNavigation = { scope.launch { drawerState.open() } },
                    showOverflowMenu = showOverflowMenu,
                    onOverflowMenuChange = { showOverflowMenu = it }
                )
            }
        }
    }
}

@Composable
private fun WorkspaceScaffoldLayout(
    state: ProjectWorkspaceState,
    backStack: NavBackStack<NavKey>,
    onEvent: (ProjectWorkspaceEvent) -> Unit,
    modifier: Modifier = Modifier,
    showBottomNavigation: Boolean,
    showNavigationButton: Boolean,
    snackbarHostState: SnackbarHostState,
    onOpenNavigation: () -> Unit = {},
    showOverflowMenu: Boolean,
    onOverflowMenuChange: (Boolean) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            WorkspaceTopBar(
                state = state,
                onEvent = onEvent,
                showNavigationButton = showNavigationButton,
                onOpenNavigation = onOpenNavigation,
                showOverflowMenu = showOverflowMenu,
                onOverflowMenuChange = onOverflowMenuChange
            )
        },
        bottomBar = {
            if (showBottomNavigation) {
                NavigationBar {
                    primaryNavigationTabs.forEach { tab ->
                        NavigationBarItem(
                            selected = state.selectedTab == tab,
                            onClick = { onEvent(ProjectWorkspaceEvent.SelectTab(tab)) },
                            icon = { NavigationGlyph(tab, selected = state.selectedTab == tab) },
                            label = {
                                Text(
                                    tabLabel(tab),
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { snackbarData ->
                val isError = state.error != null
                Snackbar(
                    snackbarData = snackbarData,
                    containerColor = if (isError) MaterialTheme.colorScheme.errorContainer
                    else MaterialTheme.colorScheme.inverseSurface,
                    contentColor = if (isError) MaterialTheme.colorScheme.onErrorContainer
                    else MaterialTheme.colorScheme.inverseOnSurface
                )
            }
        },
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (state.isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
            when (state.phase) {
                WorkspacePhase.CREDENTIALS -> CredentialsScreen(state, onEvent)
                WorkspacePhase.REGISTRATION -> RegistrationScreen(state, onEvent)
                WorkspacePhase.PROJECTS -> ProjectsScreen(state, onEvent)
                WorkspacePhase.PROJECT -> NavDisplay(
                    backStack = backStack,
                    onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                    entryProvider = { key ->
                        when (key) {
                            WorkspaceDestination.Dashboard -> NavEntry(key) {
                                DashboardScreen(state, onEvent)
                            }

                            WorkspaceDestination.Expenses -> NavEntry(key) {
                                ExpensesScreen(state, onEvent)
                            }

                            WorkspaceDestination.Partners -> NavEntry(key) {
                                PartnerLedgersScreen(state, onEvent)
                            }

                            WorkspaceDestination.Settlement -> NavEntry(key) {
                                RevenueSplitScreen(state, onEvent)
                            }

                            WorkspaceDestination.Notifications -> NavEntry(key) { NotificationsScreen() }
                            WorkspaceDestination.Invitations -> NavEntry(key) {
                                InvitationsScreen(state, onEvent)
                            }

                            else -> NavEntry(key) { DashboardScreen(state, onEvent) }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun WorkspaceTopBar(
    state: ProjectWorkspaceState,
    onEvent: (ProjectWorkspaceEvent) -> Unit,
    showNavigationButton: Boolean,
    onOpenNavigation: () -> Unit,
    showOverflowMenu: Boolean,
    onOverflowMenuChange: (Boolean) -> Unit
) {
    val navigationMenuLabel = stringResource(Res.string.navigation_menu)
    val moreActionsLabel = stringResource(Res.string.more_actions)
    TopAppBar(
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌱", style = MaterialTheme.typography.titleLarge)
                Column(
                    Modifier
                        .weight(1f)
                        .padding(start = 8.dp)
                ) {
                    Text(
                        state.selectedProject?.name ?: stringResource(Res.string.app_name),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    val (numPartners, partners) = remember(state.partners, state.phase) {
                        val partners =
                            state.partners.takeIf { state.phase == WorkspacePhase.PROJECT && it.isNotEmpty() }
                                .orEmpty()
                        partners.size to partners.joinToString(" · ") { it.name }
                    }
                    Text(
                        if (partners.isBlank()) stringResource(Res.string.app_tagline)
                        else stringResource(Res.string.project_partners, numPartners, partners),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        navigationIcon = {
            if (state.phase == WorkspacePhase.PROJECTS && state.selectedProjectId != null) {
                IconButton(onClick = { onEvent(ProjectWorkspaceEvent.ReturnToProject) }) {
                    Icon(
                        imageVector = Icons.arrow_forward,
                        contentDescription = stringResource(Res.string.back_to_project),
                        modifier = Modifier.graphicsLayer(scaleX = -1f)
                    )
                }
            } else if (showNavigationButton) {
                IconButton(onClick = onOpenNavigation) {
                    Icon(imageVector = Icons.menu, contentDescription = navigationMenuLabel)
                }
            }
        },
        actions = {
            Box {
                IconButton(onClick = { onOverflowMenuChange(true) }) {
                    Icon(imageVector = Icons.more_vert, contentDescription = moreActionsLabel)
                }
                DropdownMenu(
                    expanded = showOverflowMenu,
                    onDismissRequest = { onOverflowMenuChange(false) }
                ) {
                    if (state.phase == WorkspacePhase.PROJECT) {
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.change_project)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.sync_alt,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                onEvent(ProjectWorkspaceEvent.BackToProjects)
                                onOverflowMenuChange(false)
                            }
                        )
                    }
                    Text(
                        stringResource(Res.string.theme_options),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    ThemePreference.entries.forEach { preference ->
                        DropdownMenuItem(
                            text = { Text(themeLabel(preference)) },
                            leadingIcon = {
                                if (state.themePreference == preference) {
                                    Icon(
                                        imageVector = Icons.toggle_on,
                                        contentDescription = null
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.toggle_off,
                                        contentDescription = null
                                    )
                                }
                            },
                            onClick = {
                                onEvent(ProjectWorkspaceEvent.SetTheme(preference))
                                onOverflowMenuChange(false)
                            }
                        )
                    }
                    if (state.profile != null) {
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text(stringResource(Res.string.sign_out)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.logout,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                onEvent(ProjectWorkspaceEvent.Disconnect)
                                onOverflowMenuChange(false)
                            }
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

@Composable
private fun WorkspaceNavigationDrawer(
    selectedTab: NavigationTab,
    projectName: String?,
    modifier: Modifier = Modifier,
    onSelect: (NavigationTab) -> Unit
) {
    Column(modifier.verticalScroll(rememberScrollState())) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Text(
                stringResource(Res.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                projectName ?: stringResource(Res.string.app_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        HorizontalDivider()
        Column(Modifier.padding(horizontal = 12.dp, vertical = 12.dp)) {
            workspaceNavigationTabs.forEach { tab ->
                NavigationDrawerItem(
                    selected = selectedTab == tab,
                    onClick = { onSelect(tab) },
                    icon = { NavigationGlyph(tab, selected = selectedTab == tab) },
                    label = { Text(tabLabel(tab), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun tabLabel(tab: NavigationTab): String = stringResource(
    when (tab) {
        NavigationTab.DASHBOARD -> Res.string.dashboard
        NavigationTab.EXPENSES -> Res.string.expenses
        NavigationTab.PARTNERS -> Res.string.partner_ledgers
        NavigationTab.REVENUE_SPLIT -> Res.string.revenue_split
        NavigationTab.NOTIFICATIONS -> Res.string.notifications
        NavigationTab.INVITATIONS -> Res.string.invitations
    }
)

@Composable
private fun NavigationGlyph(tab: NavigationTab, selected: Boolean) {
    val glyph = when (tab) {
        NavigationTab.DASHBOARD -> Icons.dashboard
        NavigationTab.EXPENSES -> Icons.expenses
        NavigationTab.PARTNERS -> Icons.partners
        NavigationTab.REVENUE_SPLIT -> Icons.revenue_split
        NavigationTab.NOTIFICATIONS -> Icons.notifications_active
        NavigationTab.INVITATIONS -> Icons.mail
    }
    Icon(
        imageVector = glyph,
        contentDescription = tabLabel(tab),
        tint = if (selected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

internal fun NavigationTab.destination(): WorkspaceDestination = when (this) {
    NavigationTab.DASHBOARD -> WorkspaceDestination.Dashboard
    NavigationTab.EXPENSES -> WorkspaceDestination.Expenses
    NavigationTab.PARTNERS -> WorkspaceDestination.Partners
    NavigationTab.REVENUE_SPLIT -> WorkspaceDestination.Settlement
    NavigationTab.NOTIFICATIONS -> WorkspaceDestination.Notifications
    NavigationTab.INVITATIONS -> WorkspaceDestination.Invitations
}

@Preview(name = "Compact drawer · invitations", widthDp = 400, heightDp = 760)
@Composable
private fun CompactWorkspaceDrawerPreview() {
    ExpenseTrackerTheme {
        Surface(
            Modifier
                .width(320.dp)
                .fillMaxHeight()
        ) {
            WorkspaceNavigationDrawer(
                selectedTab = NavigationTab.INVITATIONS,
                projectName = "A very long community project name that should truncate",
                modifier = Modifier.fillMaxSize(),
                onSelect = {}
            )
        }
    }
}

@Preview(name = "Expanded drawer · notifications", widthDp = 1200, heightDp = 800)
@Composable
private fun ExpandedWorkspaceDrawerPreview() {
    ExpenseTrackerTheme {
        Row(Modifier.fillMaxSize()) {
            Surface(
                Modifier
                    .width(280.dp)
                    .fillMaxHeight(),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                WorkspaceNavigationDrawer(
                    selectedTab = NavigationTab.NOTIFICATIONS,
                    projectName = "Planting Season",
                    modifier = Modifier.fillMaxSize(),
                    onSelect = {}
                )
            }
            Surface(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {}
        }
    }
}

@Composable
private fun themeLabel(theme: ThemePreference): String = stringResource(
    when (theme) {
        ThemePreference.SYSTEM -> Res.string.theme_system
        ThemePreference.LIGHT -> Res.string.theme_light
        ThemePreference.DARK -> Res.string.theme_dark
    }
)

@Composable
private fun issueLabel(issue: WorkspaceIssue): String = when (issue) {
    WorkspaceIssue.CredentialsRequired -> stringResource(Res.string.credentials_required)
    WorkspaceIssue.ProjectRequired -> stringResource(Res.string.project_required)
    WorkspaceIssue.ProfileRequired -> stringResource(Res.string.profile_required)
    WorkspaceIssue.PayoutValuesRequired -> stringResource(Res.string.payout_values_required)
    is WorkspaceIssue.RequestFailed -> issue.statusCode?.let {
        stringResource(Res.string.request_failed, it.toString())
    } ?: stringResource(Res.string.request_unavailable)
}

@Composable
private fun messageLabel(message: WorkspaceMessage): String = stringResource(
    when (message) {
        WorkspaceMessage.PROJECT_CREATED -> Res.string.project_created
        WorkspaceMessage.INVITATION_SENT -> Res.string.invitation_sent
        WorkspaceMessage.EXPENSE_SAVED -> Res.string.expense_saved
        WorkspaceMessage.TRANCHE_SAVED -> Res.string.tranche_saved
    }
)