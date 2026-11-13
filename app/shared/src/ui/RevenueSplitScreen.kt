package co.ke.xently.expense.tracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import co.ke.xently.expense.tracker.resources.Res
import co.ke.xently.expense.tracker.resources.actual_investment
import co.ke.xently.expense.tracker.resources.calculate_split
import co.ke.xently.expense.tracker.resources.currency_amount
import co.ke.xently.expense.tracker.resources.direct_recovery
import co.ke.xently.expense.tracker.resources.final_total_payout
import co.ke.xently.expense.tracker.resources.gross_payout
import co.ke.xently.expense.tracker.resources.gross_payout_share
import co.ke.xently.expense.tracker.resources.harvest_costs
import co.ke.xently.expense.tracker.resources.investment_share_percent
import co.ke.xently.expense.tracker.resources.investment_total
import co.ke.xently.expense.tracker.resources.net_payout
import co.ke.xently.expense.tracker.resources.net_payout_share
import co.ke.xently.expense.tracker.resources.net_profit
import co.ke.xently.expense.tracker.resources.no_revenue_calculation
import co.ke.xently.expense.tracker.resources.partner_settlements
import co.ke.xently.expense.tracker.resources.return_on_investment
import co.ke.xently.expense.tracker.resources.revenue_split
import co.ke.xently.expense.tracker.resources.settlement_summary
import co.ke.xently.expense.tracker.theme.ExpenseTrackerTheme
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceEvent
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RevenueSplitScreen(
    state: ProjectWorkspaceState,
    onEvent: (ProjectWorkspaceEvent) -> Unit
) {
    val calculation = state.revenueCalculation ?: state.revenueSplit
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.6f
                    )
                )
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        stringResource(Res.string.revenue_split),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    OutlinedTextField(
                        state.grossPayout,
                        { onEvent(ProjectWorkspaceEvent.UpdateRevenue(it, state.harvestCosts)) },
                        label = { Text(stringResource(Res.string.gross_payout)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        state.harvestCosts,
                        { onEvent(ProjectWorkspaceEvent.UpdateRevenue(state.grossPayout, it)) },
                        label = { Text(stringResource(Res.string.harvest_costs)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = { onEvent(ProjectWorkspaceEvent.CalculateRevenueSplit) },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(stringResource(Res.string.calculate_split)) }
                }
            }
        }
        if (calculation == null) {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        stringResource(Res.string.no_revenue_calculation),
                        Modifier.padding(18.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        } else {
            item {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            stringResource(Res.string.settlement_summary),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        SettlementAmount(Res.string.gross_payout, calculation.grossFactoryPayout)
                        SettlementAmount(
                            Res.string.harvest_costs,
                            calculation.harvestOperationsExpenses
                        )
                        SettlementAmount(
                            Res.string.investment_total,
                            calculation.totalProjectInvestment
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                stringResource(Res.string.net_payout),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(
                                    Res.string.currency_amount,
                                    calculation.netFactoryPayout.toLong().toString()
                                ),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
            item {
                Text(
                    stringResource(Res.string.partner_settlements),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            items(calculation.partnerSettlements, key = { it.partnerId }) { settlement ->
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.75f)
                    )
                ) {
                    Column(
                        Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        settlement.partnerName.take(1).uppercase(),
                                        Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(
                                        settlement.partnerName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        stringResource(
                                            Res.string.investment_share_percent,
                                            settlement.investmentSharePercentage
                                        ),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }
                        }
                        HorizontalDivider()
                        SettlementAmount(Res.string.actual_investment, settlement.actualInvestment)
                        SettlementAmount(Res.string.gross_payout_share, settlement.grossPayoutShare)
                        SettlementAmount(Res.string.net_payout_share, settlement.netPayoutShare)
                        SettlementAmount(
                            Res.string.direct_recovery,
                            settlement.directOutOfPocketRecovery
                        )
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(Res.string.final_total_payout))
                            Text(
                                stringResource(
                                    Res.string.currency_amount,
                                    settlement.finalTotalPayout.toLong().toString()
                                ),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(stringResource(Res.string.net_profit))
                            Text(
                                stringResource(
                                    Res.string.currency_amount,
                                    settlement.netProfit.toLong().toString()
                                ),
                                color = if (settlement.netProfit >= 0.0) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                            )
                        }
                        Text(
                            stringResource(
                                Res.string.return_on_investment,
                                settlement.returnOnInvestmentPercentage
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettlementAmount(
    label: org.jetbrains.compose.resources.StringResource,
    amount: Double
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(stringResource(label), style = MaterialTheme.typography.bodyMedium)
        Text(
            stringResource(Res.string.currency_amount, amount.toLong().toString()),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@PreviewLightDark
@PreviewFontScale
@PreviewDynamicColors
@PreviewScreenSizes
@Composable
private fun RevenueSplitScreenPreview() {
    ExpenseTrackerTheme { RevenueSplitScreen(previewWorkspaceState) {} }
}

@Preview(name = "Calculated split")
@PreviewLightDark
@Composable
private fun CalculatedRevenueSplitScreenPreview() {
    ExpenseTrackerTheme {
        RevenueSplitScreen(previewWorkspaceState.copy(revenueCalculation = previewWorkspaceState.revenueSplit)) {}
    }
}