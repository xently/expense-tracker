package co.ke.xently.expense.tracker

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import co.ke.xently.expense.tracker.model.ExpenseCategory
import co.ke.xently.expense.tracker.theme.SugarcaneTheme
import co.ke.xently.expense.tracker.ui.DashboardScreen
import co.ke.xently.expense.tracker.ui.ExpensesScreen
import co.ke.xently.expense.tracker.ui.NotificationsScreen
import co.ke.xently.expense.tracker.ui.PartnerLedgersScreen
import co.ke.xently.expense.tracker.ui.RevenueSplitScreen
import co.ke.xently.expense.tracker.viewmodel.ExpenseTrackerUiState
import co.ke.xently.expense.tracker.viewmodel.ExpenseTrackerViewModel
import co.ke.xently.expense.tracker.viewmodel.NavigationTab
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.reload.DevelopmentEntryPoint

@Composable
@DevelopmentEntryPoint
fun App(viewModel: ExpenseTrackerViewModel = viewModel { ExpenseTrackerViewModel() }) {
    val state by viewModel.uiState.collectAsState()

    App(
        state = state,
        toggleTheme = { viewModel.toggleTheme() },
        selectTab = { viewModel.selectTab(it) },
        onClearInfoMessage = { viewModel.clearInfoMessage() },
        onAddExpenseClick = { viewModel.setShowAddExpenseDialog(true) },
        onAddTrancheClick = { viewModel.setShowAddTrancheDialog(true) },
        onCategoryFilterSelect = { viewModel.setCategoryFilter(it) },
        onDismissAddExpenseDialog = { viewModel.setShowAddExpenseDialog(false) },
        onSaveExpense = { title, cat, amt, date, partner, notes ->
            viewModel.addExpense(title, cat, amt, date, partner, notes)
        },
        onDeleteExpense = { viewModel.deleteExpense(it) },
        onDismissAddTrancheDialog = { viewModel.setShowAddTrancheDialog(false) },
        onSaveTranche = { partner, amt, date, ref, notes ->
            viewModel.addTranche(partner, amt, date, ref, notes)
        },
        onGrossChange = { gross ->
            viewModel.updateRevenueInputs(gross, state.revenueHarvestExpensesInput)
        },
        onHarvestExpensesChange = { harvestCosts ->
            viewModel.updateRevenueInputs(
                state.revenueGrossPayoutInput,
                harvestCosts
            )
        },
        onTriggerCheck = { viewModel.triggerOverdueCheck() }
    )
}

@Composable
fun App(
    state: ExpenseTrackerUiState,
    toggleTheme: () -> Unit,
    onTriggerCheck: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onAddTrancheClick: () -> Unit,
    onClearInfoMessage: () -> Unit,
    onGrossChange: (String) -> Unit,
    onDeleteExpense: (String) -> Unit,
    selectTab: (NavigationTab) -> Unit,
    onDismissAddExpenseDialog: () -> Unit,
    onDismissAddTrancheDialog: () -> Unit,
    onHarvestExpensesChange: (String) -> Unit,
    onCategoryFilterSelect: (ExpenseCategory?) -> Unit,
    onSaveTranche: (String, Double, LocalDate, String, String) -> Unit,
    onSaveExpense: (String, ExpenseCategory, Double, LocalDate, String, String) -> Unit
) {
    val hostState = remember { SnackbarHostState() }
    LaunchedEffect(state.infoMessage) {
        state.infoMessage?.let { message ->
            val snackbarResult = hostState.showSnackbar(message = message, withDismissAction = true)
            if (snackbarResult == SnackbarResult.Dismissed) onClearInfoMessage()
        }
    }
    SugarcaneTheme(darkTheme = state.isDarkTheme) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🌾 CaneTracker",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = MaterialTheme.shapes.extraSmall,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "2026 Season",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(
                                            horizontal = 6.dp,
                                            vertical = 2.dp
                                        )
                                    )
                                }
                            }
                            Text(
                                text = "Harrison • Herman • Vin",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        FilledTonalButton(
                            onClick = toggleTheme,
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(if (state.isDarkTheme) "☀️ Light" else "🌙 Dark", fontSize = 12.sp)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationTab.entries.forEach { tab ->
                        val isSelected = state.selectedTab == tab
                        val iconText = when (tab) {
                            NavigationTab.DASHBOARD -> "📊"
                            NavigationTab.EXPENSES -> "💸"
                            NavigationTab.PARTNERS -> "👥"
                            NavigationTab.REVENUE_SPLIT -> "⚖️"
                            NavigationTab.NOTIFICATIONS -> "🔔"
                        }

                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { selectTab(tab) },
                            icon = { Text(iconText, fontSize = 18.sp) },
                            label = {
                                Text(
                                    tab.title,
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = hostState)
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (state.selectedTab) {
                    NavigationTab.DASHBOARD -> {
                        DashboardScreen(
                            state = state,
                            onNavigateToTab = selectTab,
                            onAddExpenseClick = onAddExpenseClick,
                            onAddTrancheClick = onAddTrancheClick
                        )
                    }

                    NavigationTab.EXPENSES -> {
                        ExpensesScreen(
                            state = state,
                            onCategoryFilterSelect = onCategoryFilterSelect,
                            onAddExpenseClick = onAddExpenseClick,
                            onDismissAddExpenseDialog = onDismissAddExpenseDialog,
                            onSaveExpense = onSaveExpense,
                            onDeleteExpense = onDeleteExpense
                        )
                    }

                    NavigationTab.PARTNERS -> {
                        PartnerLedgersScreen(
                            state = state,
                            onAddTrancheClick = onAddTrancheClick,
                            onDismissAddTrancheDialog = onDismissAddTrancheDialog,
                            onSaveTranche = onSaveTranche
                        )
                    }

                    NavigationTab.REVENUE_SPLIT -> {
                        RevenueSplitScreen(
                            state = state,
                            onGrossChange = onGrossChange,
                            onHarvestExpensesChange = onHarvestExpensesChange
                        )
                    }

                    NavigationTab.NOTIFICATIONS -> {
                        NotificationsScreen(
                            state = state,
                            onTriggerCheck = onTriggerCheck
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
@PreviewLightDark
@PreviewFontScale
@PreviewDynamicColors
@PreviewScreenSizes
fun AppAndroidPreview() {
    App()
}
