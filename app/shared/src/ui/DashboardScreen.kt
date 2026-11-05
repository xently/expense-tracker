package co.ke.xently.expense.tracker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import co.ke.xently.expense.tracker.categoryLabel
import co.ke.xently.expense.tracker.model.ExpenseCategory
import co.ke.xently.expense.tracker.resources.Res
import co.ke.xently.expense.tracker.resources.category_amount_percent
import co.ke.xently.expense.tracker.resources.category_breakdown
import co.ke.xently.expense.tracker.resources.currency_amount
import co.ke.xently.expense.tracker.resources.end_season_revenue_banner
import co.ke.xently.expense.tracker.resources.financial_overview
import co.ke.xently.expense.tracker.resources.log_expense
import co.ke.xently.expense.tracker.resources.metric_contribution_sources
import co.ke.xently.expense.tracker.resources.metric_expense_operations
import co.ke.xently.expense.tracker.resources.metric_monthly_due_date
import co.ke.xently.expense.tracker.resources.metric_remaining_shares
import co.ke.xently.expense.tracker.resources.no_expenses
import co.ke.xently.expense.tracker.resources.no_partners
import co.ke.xently.expense.tracker.resources.partner_balances
import co.ke.xently.expense.tracker.resources.partner_ledgers
import co.ke.xently.expense.tracker.resources.partner_ledgers_compliance
import co.ke.xently.expense.tracker.resources.pay_tranche
import co.ke.xently.expense.tracker.resources.project_partners
import co.ke.xently.expense.tracker.resources.project_workspace_status
import co.ke.xently.expense.tracker.resources.refresh
import co.ke.xently.expense.tracker.resources.revenue_split
import co.ke.xently.expense.tracker.resources.review_revenue_split
import co.ke.xently.expense.tracker.resources.total_contributions
import co.ke.xently.expense.tracker.resources.total_expenses
import co.ke.xently.expense.tracker.resources.total_outstanding
import co.ke.xently.expense.tracker.resources.total_overdue
import co.ke.xently.expense.tracker.resources.view_all_expenses
import co.ke.xently.expense.tracker.theme.ExpenseTrackerTheme
import co.ke.xently.expense.tracker.theme.Icons
import co.ke.xently.expense.tracker.viewmodel.NavigationTab
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceEvent
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DashboardScreen(
    state: ProjectWorkspaceState,
    onEvent: (ProjectWorkspaceEvent) -> Unit
) {
    val (numPartners, dotSeparatedPartners) = remember(state.partners) {
        state.partners.size to state.partners.joinToString(" · ") { it.name }
    }
    val totals = financialOverviewTotals(state.expenses, state.partnerSummaries)
    val categoryTotals = state.expenses.groupBy { it.category }
        .map { (category, expenses) -> category to expenses.sumOf { it.totalAmount } }
        .filter { it.second > 0.0 }
        .sortedByDescending { it.second }
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(
                            Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                state.selectedProject?.name.orEmpty(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            AnimatedVisibility(dotSeparatedPartners.isNotBlank()) {
                                Text(
                                    stringResource(
                                        Res.string.project_partners,
                                        numPartners,
                                        dotSeparatedPartners
                                    ),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                stringResource(Res.string.project_workspace_status),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    val description = remember(state.selectedProject?.description) {
                        state.selectedProject?.description?.takeIf { it.isNotBlank() }.orEmpty()
                    }
                    AnimatedVisibility(description.isNotBlank()) {
                        Text(
                            description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onEvent(ProjectWorkspaceEvent.OpenExpenseForm) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.add, contentDescription = null)
                            Text(stringResource(Res.string.log_expense))
                        }
                        OutlinedButton(
                            onClick = { onEvent(ProjectWorkspaceEvent.OpenTrancheForm) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(imageVector = Icons.add, contentDescription = null)
                            Text(stringResource(Res.string.pay_tranche))
                        }
                    }
                }
            }
        }
        item {
            ListItem(
                headlineContent = {
                    Text(
                        stringResource(Res.string.financial_overview),
                        fontWeight = FontWeight.Bold
                    )
                },
            )
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FinancialMetricCard(
                        stringResource(Res.string.total_expenses), totals.expenses,
                        Modifier.weight(1f),
                        supportingText = stringResource(Res.string.metric_expense_operations),
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FinancialMetricCard(
                        stringResource(Res.string.total_contributions), totals.contributions,
                        Modifier.weight(1f),
                        supportingText = stringResource(Res.string.metric_contribution_sources),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FinancialMetricCard(
                        stringResource(Res.string.total_outstanding), totals.outstanding,
                        Modifier.weight(1f),
                        supportingText = stringResource(Res.string.metric_remaining_shares),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    FinancialMetricCard(
                        stringResource(Res.string.total_overdue), totals.overdue,
                        Modifier.weight(1f),
                        supportingText = stringResource(Res.string.metric_monthly_due_date),
                        containerColor = if (totals.overdue > 0) MaterialTheme.colorScheme.errorContainer
                        else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (totals.overdue > 0) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        highlighted = totals.overdue > 0.0
                    )
                }
            }
        }
        item {
            ListItem(
                headlineContent = {
                    Text(
                        stringResource(Res.string.partner_ledgers_compliance),
                        fontWeight = FontWeight.Bold
                    )
                },
                supportingContent = {
                    Text(stringResource(Res.string.partner_balances))
                },
                trailingContent = {
                    TextButton(onClick = {
                        onEvent(ProjectWorkspaceEvent.SelectTab(NavigationTab.PARTNERS))
                    }) { Text(stringResource(Res.string.partner_ledgers)) }
                },
            )
        }
        if (state.partnerSummaries.isEmpty()) item { Text(stringResource(Res.string.no_partners)) }
        items(state.partnerSummaries, key = { it.partnerId }) { summary ->
            PartnerSummaryCard(summary = summary, numPartners = numPartners)
        }
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.55f
                    )
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    ListItem(
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                        headlineContent = {
                            Text(
                                stringResource(Res.string.category_breakdown),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        trailingContent = {
                            TextButton(onClick = {
                                onEvent(ProjectWorkspaceEvent.SelectTab(NavigationTab.EXPENSES))
                            }) {
                                Text(stringResource(Res.string.view_all_expenses))
                                Icon(imageVector = Icons.arrow_forward, contentDescription = null)
                            }
                        },
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp),
                    )
                    if (categoryTotals.isEmpty()) Text(
                        stringResource(Res.string.no_expenses),
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 24.dp),
                    )
                    else categoryTotals.forEach { (category, amount) ->
                        CategorySpendRow(
                            modifier = Modifier
                                .padding(bottom = 24.dp)
                                .padding(horizontal = 16.dp),
                            category = category,
                            amount = amount,
                            progress = categorySpendProgress(amount, totals.expenses),
                        )
                    }
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        stringResource(Res.string.revenue_split),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        stringResource(Res.string.end_season_revenue_banner),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { onEvent(ProjectWorkspaceEvent.SelectTab(NavigationTab.REVENUE_SPLIT)) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(Res.string.review_revenue_split)) }
                }
            }
        }
        item {
            OutlinedButton(
                onClick = { onEvent(ProjectWorkspaceEvent.Refresh) },
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(Res.string.refresh)) }
        }
    }
}

@Composable
private fun CategorySpendRow(
    category: ExpenseCategory,
    amount: Double,
    progress: Float,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(6.dp),
) {
    Column(modifier = modifier, verticalArrangement = verticalArrangement) {
        val percentage = remember(progress) { (progress * 100).toInt() }
        Text(
            stringResource(
                Res.string.category_amount_percent,
                categoryLabel(category),
                stringResource(Res.string.currency_amount, amount.toLong().toString()),
                percentage
            ),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@PreviewLightDark
@PreviewFontScale
@PreviewDynamicColors
@PreviewScreenSizes
@Composable
private fun DashboardScreenPreview() {
    ExpenseTrackerTheme { DashboardScreen(previewWorkspaceState) {} }
}

@Preview(name = "Empty project")
@PreviewLightDark
@Composable
private fun EmptyDashboardScreenPreview() {
    ExpenseTrackerTheme { DashboardScreen(previewEmptyWorkspaceState) {} }
}