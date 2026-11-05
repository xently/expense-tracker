package co.ke.xently.expense.tracker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.minus
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import co.ke.xently.expense.tracker.resources.Res
import co.ke.xently.expense.tracker.resources.currency_amount
import co.ke.xently.expense.tracker.resources.individual_partner_summaries
import co.ke.xently.expense.tracker.resources.no_partners
import co.ke.xently.expense.tracker.resources.no_payments
import co.ke.xently.expense.tracker.resources.partner_no_longer_listed
import co.ke.xently.expense.tracker.resources.partner_section_subtitle
import co.ke.xently.expense.tracker.resources.partner_section_title
import co.ke.xently.expense.tracker.resources.payment_count
import co.ke.xently.expense.tracker.resources.payment_history
import co.ke.xently.expense.tracker.resources.record_tranche
import co.ke.xently.expense.tracker.resources.reference_number_value
import co.ke.xently.expense.tracker.theme.ExpenseTrackerTheme
import co.ke.xently.expense.tracker.theme.Icons
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceEvent
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PartnerLedgersScreen(
    state: ProjectWorkspaceState,
    onEvent: (ProjectWorkspaceEvent) -> Unit
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(Res.string.partner_section_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.partner_section_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }
        item {
            Text(
                stringResource(Res.string.individual_partner_summaries),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
        }
        if (state.partnerSummaries.isEmpty()) item { Text(stringResource(Res.string.no_partners)) }
        items(state.partnerSummaries, key = { it.partnerId }) { summary ->
            PartnerDetailedLedgerCard(summary = summary, numPartners = state.partners.size)
        }
        item {
            ListItem(
                headlineContent = {
                    Text(
                        stringResource(Res.string.payment_history),
                        fontWeight = FontWeight.Bold
                    )
                },
                supportingContent = {
                    Text(stringResource(Res.string.payment_count, state.tranches.size))
                },
                trailingContent = {
                    OutlinedButton(
                        onClick = { onEvent(ProjectWorkspaceEvent.OpenTrancheForm) },
                        contentPadding = ButtonDefaults.SmallContentPadding.minus(
                            PaddingValues(start = 10.dp, end = 6.dp)
                        ),
                        shape = MaterialTheme.shapes.small.copy(CornerSize(25)),
                    ) {
                        Icon(imageVector = Icons.add, contentDescription = null)
                        Text(stringResource(Res.string.record_tranche).toUpperCase(Locale.current))
                    }
                },
            )
        }
        item { HorizontalDivider() }
        val orderedPayments = state.tranches.sortedByDescending { it.paymentDate }
        if (orderedPayments.isEmpty()) item { Text(stringResource(Res.string.no_payments)) }
        items(orderedPayments, key = { it.id }) { tranche ->
            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(
                    Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(
                                Res.string.currency_amount,
                                tranche.amount.toLong().toString()
                            ),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            tranche.paymentDate.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider()
                    Text(
                        state.partners.firstOrNull { it.id == tranche.partnerId }?.name
                            ?: stringResource(Res.string.partner_no_longer_listed),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    AnimatedVisibility(tranche.referenceNumber.isNotBlank()) {
                        Text(
                            stringResource(
                                Res.string.reference_number_value,
                                tranche.referenceNumber
                            ),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    AnimatedVisibility(tranche.notes.isNotBlank()) { Text(tranche.notes) }
                }
            }
        }
    }
}

@PreviewLightDark
@PreviewFontScale
@PreviewDynamicColors
@PreviewScreenSizes
@Composable
private fun PartnerLedgersScreenPreview() {
    ExpenseTrackerTheme { PartnerLedgersScreen(previewWorkspaceState) {} }
}

@Preview(name = "No payments")
@PreviewLightDark
@Composable
private fun EmptyPartnerLedgersScreenPreview() {
    ExpenseTrackerTheme { PartnerLedgersScreen(previewEmptyWorkspaceState) {} }
}