package co.ke.xently.expense.tracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import co.ke.xently.expense.tracker.model.ComplianceStatus
import co.ke.xently.expense.tracker.model.PartnerLedgerSummary
import co.ke.xently.expense.tracker.resources.Res
import co.ke.xently.expense.tracker.resources.cash_installment_tranches
import co.ke.xently.expense.tracker.resources.contributed
import co.ke.xently.expense.tracker.resources.credit
import co.ke.xently.expense.tracker.resources.currency_amount
import co.ke.xently.expense.tracker.resources.direct_out_of_pocket
import co.ke.xently.expense.tracker.resources.net_balance
import co.ke.xently.expense.tracker.resources.net_standing_balance
import co.ke.xently.expense.tracker.resources.obligated_share
import co.ke.xently.expense.tracker.resources.owes
import co.ke.xently.expense.tracker.resources.partner_investment_share
import co.ke.xently.expense.tracker.resources.past_due_balance
import co.ke.xently.expense.tracker.resources.signed_currency_amount
import co.ke.xently.expense.tracker.resources.signed_currency_amount_desc
import co.ke.xently.expense.tracker.resources.total_contributed
import co.ke.xently.expense.tracker.resources.total_obligated_share
import co.ke.xently.expense.tracker.statusLabel
import co.ke.xently.expense.tracker.theme.ComplianceAmber
import co.ke.xently.expense.tracker.theme.ComplianceGreen
import co.ke.xently.expense.tracker.theme.ComplianceRed
import co.ke.xently.expense.tracker.theme.ExpenseTrackerTheme
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun FinancialMetricCard(
    title: String,
    amount: Double,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    containerColor: Color? = null,
    contentColor: Color? = null,
    highlighted: Boolean = false,
) {
    val cardColor = containerColor ?: if (highlighted) MaterialTheme.colorScheme.errorContainer
    else MaterialTheme.colorScheme.secondaryContainer
    val foregroundColor =
        contentColor ?: if (highlighted) MaterialTheme.colorScheme.onErrorContainer
        else MaterialTheme.colorScheme.onSecondaryContainer
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = foregroundColor
            )
            Text(
                stringResource(Res.string.currency_amount, amount.toLong().toString()),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = foregroundColor
            )
            if (supportingText != null) {
                Text(
                    supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = foregroundColor
                )
            }
        }
    }
}

@Composable
fun PartnerSummaryCard(summary: PartnerLedgerSummary, numPartners: Int) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.7f
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            PartnerLedgerHeadline(summary = summary)

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(Res.string.contributed),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(
                            Res.string.currency_amount,
                            summary.totalContributed.toLong().toString()
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column {
                    Text(
                        text = stringResource(Res.string.obligated_share, 1, numPartners),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(
                            Res.string.currency_amount,
                            summary.totalObligated.toLong().toString()
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(Res.string.net_balance),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (summary.netBalance >= 0) {
                            stringResource(
                                Res.string.signed_currency_amount,
                                "+",
                                summary.netBalance.toLong().toString()
                            )
                        } else {
                            stringResource(
                                Res.string.signed_currency_amount,
                                "-",
                                (-summary.netBalance).toLong().toString()
                            )
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.netBalance >= 0) {
                            ComplianceGreen
                        } else if (summary.overdueAmount > 0) {
                            ComplianceRed
                        } else {
                            ComplianceAmber
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun PartnerDetailedLedgerCard(summary: PartnerLedgerSummary, numPartners: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.7f
            )
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            PartnerLedgerHeadline(summary)

            Spacer(modifier = Modifier.height(14.dp))

            // Breakdown Grid
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.direct_out_of_pocket),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(
                            Res.string.currency_amount,
                            summary.directOutOfPocket.toLong()
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.cash_installment_tranches),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(
                            Res.string.currency_amount,
                            summary.cashTranches.toLong()
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.total_contributed),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = stringResource(
                            Res.string.currency_amount,
                            summary.totalContributed.toLong()
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.total_obligated_share, 1, numPartners),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "KES ${summary.totalObligated.toLong()}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(Res.string.net_standing_balance),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = if (summary.netBalance >= 0) {
                            stringResource(
                                Res.string.signed_currency_amount_desc,
                                "+",
                                summary.netBalance.toLong(),
                                stringResource(Res.string.credit),
                            )
                        } else {
                            stringResource(
                                Res.string.signed_currency_amount_desc,
                                "-",
                                (-summary.netBalance).toLong(),
                                stringResource(Res.string.owes),
                            )
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (summary.netBalance >= 0) {
                            ComplianceGreen
                        } else if (summary.overdueAmount > 0) {
                            ComplianceRed
                        } else {
                            ComplianceAmber
                        },
                    )
                }

                if (summary.overdueAmount > 0) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(Res.string.past_due_balance),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = stringResource(
                                Res.string.currency_amount,
                                summary.overdueAmount.toLong(),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PartnerLedgerHeadline(summary: PartnerLedgerSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = summary.partnerName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Column {
                Text(
                    text = summary.partnerName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(
                        Res.string.partner_investment_share,
                        summary.investmentSharePercentage
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        ComplianceBadge(status = summary.complianceStatus)
    }
}

@Composable
private fun FinancialValue(label: org.jetbrains.compose.resources.StringResource, amount: Double) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(stringResource(label), style = MaterialTheme.typography.bodyMedium)
        Text(
            stringResource(Res.string.currency_amount, amount.toLong().toString()),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
internal fun ComplianceBadge(status: ComplianceStatus) {
    val (container, content) = when (status) {
        ComplianceStatus.PAID_IN_FULL -> MaterialTheme.colorScheme.primaryContainer to
                MaterialTheme.colorScheme.onPrimaryContainer

        ComplianceStatus.PARTIAL -> MaterialTheme.colorScheme.tertiaryContainer to
                MaterialTheme.colorScheme.onTertiaryContainer

        ComplianceStatus.OVERDUE -> MaterialTheme.colorScheme.errorContainer to
                MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(color = container, shape = MaterialTheme.shapes.small) {
        Text(
            statusLabel(status),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelMedium,
            color = content
        )
    }
}

private fun signedAmount(amount: Double): String = when {
    amount > 0.0 -> "+${amount.toLong()}"
    else -> amount.toLong().toString()
}

private class ComplianceStatusParameter : PreviewParameterProvider<ComplianceStatus> {
    override val values: Sequence<ComplianceStatus>
        get() = ComplianceStatus.entries.asSequence()
}

@Preview
@PreviewLightDark
@Composable
private fun ComplianceBadgePreview(
    @PreviewParameter(ComplianceStatusParameter::class)
    status: ComplianceStatus
) {
    ExpenseTrackerTheme { ComplianceBadge(status = status) }
}