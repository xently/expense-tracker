package co.ke.xently.expense.tracker.calculator

import co.ke.xently.expense.tracker.model.AppNotification
import co.ke.xently.expense.tracker.model.ComplianceStatus
import co.ke.xently.expense.tracker.model.DashboardSummary
import co.ke.xently.expense.tracker.model.Expense
import co.ke.xently.expense.tracker.model.ExpenseCategory
import co.ke.xently.expense.tracker.model.ExpenseSplit
import co.ke.xently.expense.tracker.model.Partner
import co.ke.xently.expense.tracker.model.PartnerId
import co.ke.xently.expense.tracker.model.PartnerLedgerSummary
import co.ke.xently.expense.tracker.model.PartnerSettlement
import co.ke.xently.expense.tracker.model.SeasonRevenueCalculation
import co.ke.xently.expense.tracker.model.TranchePayment
import kotlinx.datetime.LocalDate
import kotlin.math.round

object ExpenseCalculator {

    val DEFAULT_PARTNERS = listOf(
        Partner(
            id = PartnerId.HARRISON.name,
            name = PartnerId.HARRISON.displayName,
            email = PartnerId.HARRISON.email
        ),
        Partner(
            id = PartnerId.HERMAN.name,
            name = PartnerId.HERMAN.displayName,
            email = PartnerId.HERMAN.email
        ),
        Partner(
            id = PartnerId.VIN.name,
            name = PartnerId.VIN.displayName,
            email = PartnerId.VIN.email
        )
    )

    /**
     * Calculates the due date as the 10th day of the month following the transaction date
     * (EOMONTH(date) + 10 days).
     *
     * Example:
     * 2026-05-15 -> 2026-06-10
     * 2026-12-04 -> 2027-01-10
     */
    fun calculateDueDate(transactionDate: LocalDate): LocalDate {
        val currentMonthNum = transactionDate.month.ordinal + 1
        val nextYear = if (currentMonthNum == 12) transactionDate.year + 1 else transactionDate.year
        val nextMonth = if (currentMonthNum == 12) 1 else currentMonthNum + 1
        return LocalDate(nextYear, nextMonth, 10)
    }

    /**
     * Splits an expense total equally across all participating partners.
     */
    fun calculateSplits(
        totalAmount: Double,
        transactionDate: LocalDate,
        partners: List<Partner> = DEFAULT_PARTNERS
    ): List<ExpenseSplit> {
        if (partners.isEmpty()) return emptyList()
        val count = partners.size
        val dueDate = calculateDueDate(transactionDate)
        val sharePerPartner = roundDouble(totalAmount / count)

        // Adjust possible rounding pennies on the last partner to match totalAmount exactly
        return partners.mapIndexed { index, partner ->
            val share = if (index == count - 1) {
                roundDouble(totalAmount - (sharePerPartner * (count - 1)))
            } else {
                sharePerPartner
            }
            ExpenseSplit(
                partnerId = partner.id,
                shareAmount = share,
                dueDate = dueDate
            )
        }
    }

    fun roundDouble(value: Double, decimals: Int = 2): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(value * multiplier) / multiplier
    }
}

object LedgerCalculator {

    fun computePartnerSummaries(
        expenses: List<Expense>,
        tranches: List<TranchePayment>,
        partners: List<Partner> = ExpenseCalculator.DEFAULT_PARTNERS,
        asOfDate: LocalDate
    ): List<PartnerLedgerSummary> {
        val totalProjectInvestment = partners.sumOf { partner ->
            val outOfPocket =
                expenses.filter { it.payingPartnerId.equals(partner.id, ignoreCase = true) }
                    .sumOf { it.totalAmount }
            val cashTranches =
                tranches.filter { it.partnerId.equals(partner.id, ignoreCase = true) }
                    .sumOf { it.amount }
            outOfPocket + cashTranches
        }

        return partners.map { partner ->
            val outOfPocket = expenses
                .filter { it.payingPartnerId.equals(partner.id, ignoreCase = true) }
                .sumOf { it.totalAmount }

            val cashTranches = tranches
                .filter { it.partnerId.equals(partner.id, ignoreCase = true) }
                .sumOf { it.amount }

            val totalContributed = ExpenseCalculator.roundDouble(outOfPocket + cashTranches)

            // Obligations from splits
            val splits = expenses.flatMap { expense ->
                expense.splits.filter { it.partnerId.equals(partner.id, ignoreCase = true) }
            }.sortedBy { it.dueDate }

            val totalObligated = ExpenseCalculator.roundDouble(splits.sumOf { it.shareAmount })
            val netBalance = ExpenseCalculator.roundDouble(totalContributed - totalObligated)

            // Determine overdue amount by matching contributions against chronological split obligations
            var availablePool = totalContributed
            var overdueSum = 0.0

            for (split in splits) {
                val needed = split.shareAmount
                if (availablePool >= needed) {
                    availablePool -= needed
                } else {
                    val unpaidPart = needed - availablePool
                    availablePool = 0.0
                    if (split.dueDate < asOfDate) {
                        overdueSum += unpaidPart
                    }
                }
            }

            val overdueAmount = ExpenseCalculator.roundDouble(overdueSum)

            val status = when {
                netBalance >= -0.01 -> ComplianceStatus.PAID_IN_FULL
                overdueAmount > 0.01 -> ComplianceStatus.OVERDUE
                else -> ComplianceStatus.PARTIAL
            }

            val sharePct = if (totalProjectInvestment > 0.0) {
                ExpenseCalculator.roundDouble((totalContributed / totalProjectInvestment) * 100.0)
            } else {
                ExpenseCalculator.roundDouble(100.0 / partners.size.coerceAtLeast(1))
            }

            PartnerLedgerSummary(
                partnerId = partner.id,
                partnerName = partner.name,
                directOutOfPocket = ExpenseCalculator.roundDouble(outOfPocket),
                cashTranches = ExpenseCalculator.roundDouble(cashTranches),
                totalContributed = totalContributed,
                totalObligated = totalObligated,
                netBalance = netBalance,
                overdueAmount = overdueAmount,
                complianceStatus = status,
                investmentSharePercentage = sharePct
            )
        }
    }

    fun computeDashboardSummary(
        expenses: List<Expense>,
        tranches: List<TranchePayment>,
        notifications: List<AppNotification>,
        partners: List<Partner> = ExpenseCalculator.DEFAULT_PARTNERS,
        asOfDate: LocalDate
    ): DashboardSummary {
        val partnerSummaries = computePartnerSummaries(expenses, tranches, partners, asOfDate)
        val totalExpenses = ExpenseCalculator.roundDouble(expenses.sumOf { it.totalAmount })
        val totalContributions =
            ExpenseCalculator.roundDouble(partnerSummaries.sumOf { it.totalContributed })
        val totalOutstanding = ExpenseCalculator.roundDouble(
            partnerSummaries.filter { it.netBalance < 0 }.sumOf { -it.netBalance }
        )
        val totalOverdue =
            ExpenseCalculator.roundDouble(partnerSummaries.sumOf { it.overdueAmount })

        val categoryBreakdowns = ExpenseCategory.entries.associateWith { cat ->
            ExpenseCalculator.roundDouble(expenses.filter { it.category == cat }
                .sumOf { it.totalAmount })
        }
        val categoryBreakdownsByName =
            expenses.groupBy { it.categoryName ?: it.category.displayName }
                .mapValues { (_, groupedExpenses) ->
                    ExpenseCalculator.roundDouble(groupedExpenses.sumOf { it.totalAmount })
                }

        return DashboardSummary(
            totalExpenses = totalExpenses,
            totalContributions = totalContributions,
            totalOutstandingObligations = totalOutstanding,
            totalOverdueAmount = totalOverdue,
            partnerSummaries = partnerSummaries,
            categoryBreakdowns = categoryBreakdowns,
            recentExpenses = expenses.sortedByDescending { it.transactionDate }.take(10),
            recentTranches = tranches.sortedByDescending { it.paymentDate }.take(10),
            notifications = notifications.sortedByDescending { it.timestampEpochMs }.take(10),
            categoryBreakdownsByName = categoryBreakdownsByName
        )
    }
}

object RevenueSplitCalculator {

    /**
     * Calculates the end-of-season revenue split pro-rata based on actual partner investment shares.
     */
    fun calculate(
        grossFactoryPayout: Double,
        harvestOperationsExpenses: Double,
        partnerLedgerSummaries: List<PartnerLedgerSummary>
    ): SeasonRevenueCalculation {
        val netFactoryPayout = ExpenseCalculator.roundDouble(
            (grossFactoryPayout - harvestOperationsExpenses).coerceAtLeast(0.0)
        )
        val totalProjectInvestment = ExpenseCalculator.roundDouble(
            partnerLedgerSummaries.sumOf { it.totalContributed }
        )

        val settlements = partnerLedgerSummaries.map { summary ->
            val actualInvestment = summary.totalContributed
            val sharePct = if (totalProjectInvestment > 0.0) {
                ExpenseCalculator.roundDouble((actualInvestment / totalProjectInvestment) * 100.0)
            } else {
                ExpenseCalculator.roundDouble(100.0 / partnerLedgerSummaries.size.coerceAtLeast(1))
            }

            val fraction = sharePct / 100.0
            val grossPayoutShare = ExpenseCalculator.roundDouble(grossFactoryPayout * fraction)
            val netPayoutShare = ExpenseCalculator.roundDouble(netFactoryPayout * fraction)
            val netProfit = ExpenseCalculator.roundDouble(netPayoutShare - actualInvestment)
            val roi = if (actualInvestment > 0.0) {
                ExpenseCalculator.roundDouble((netProfit / actualInvestment) * 100.0)
            } else {
                0.0
            }

            PartnerSettlement(
                partnerId = summary.partnerId,
                partnerName = summary.partnerName,
                actualInvestment = actualInvestment,
                investmentSharePercentage = sharePct,
                grossPayoutShare = grossPayoutShare,
                netPayoutShare = netPayoutShare,
                directOutOfPocketRecovery = summary.directOutOfPocket,
                finalTotalPayout = netPayoutShare,
                netProfit = netProfit,
                returnOnInvestmentPercentage = roi
            )
        }

        return SeasonRevenueCalculation(
            grossFactoryPayout = ExpenseCalculator.roundDouble(grossFactoryPayout),
            harvestOperationsExpenses = ExpenseCalculator.roundDouble(harvestOperationsExpenses),
            netFactoryPayout = netFactoryPayout,
            totalProjectInvestment = totalProjectInvestment,
            partnerSettlements = settlements
        )
    }
}
