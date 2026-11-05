package co.ke.xently.expense.tracker.ui

import co.ke.xently.expense.tracker.model.ComplianceStatus
import co.ke.xently.expense.tracker.model.Expense
import co.ke.xently.expense.tracker.model.ExpenseCategory
import co.ke.xently.expense.tracker.model.PartnerLedgerSummary
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class FinancialDisplayTest {
    @Test
    fun overviewTotalsSeparateOutstandingFromOverdue() {
        val expenses = listOf(
            expense("land", ExpenseCategory.LAND_PREP, 30_000.0),
            expense("seedlings", ExpenseCategory.SEEDLINGS, 10_000.0)
        )

        val totals = financialOverviewTotals(
            expenses,
            listOf(
                partnerSummary("owes", contributed = 1_000.0, obligated = 2_500.0, overdue = 400.0),
                partnerSummary("credit", contributed = 2_000.0, obligated = 1_800.0, overdue = 0.0)
            )
        )

        assertEquals(FinancialOverviewTotals(40_000.0, 3_000.0, 1_500.0, 400.0), totals)
    }

    @Test
    fun emptyOverviewAndZeroExpenseProgressAreSafe() {
        assertEquals(FinancialOverviewTotals(0.0, 0.0, 0.0, 0.0), financialOverviewTotals(emptyList(), emptyList()))
        assertEquals(0f, categorySpendProgress(categoryTotal = 25.0, totalExpenses = 0.0))
    }

    @Test
    fun categoryFilterKeepsAllExpensesOrOnlySelectedCategory() {
        val expenses = listOf(
            expense("prep", ExpenseCategory.LAND_PREP, 30_000.0),
            expense("seedlings", ExpenseCategory.SEEDLINGS, 10_000.0),
            expense("more-prep", ExpenseCategory.LAND_PREP, 5_000.0)
        )

        assertEquals(expenses, filterExpensesByCategory(expenses, null))
        assertEquals(
            listOf("prep", "more-prep"),
            filterExpensesByCategory(expenses, ExpenseCategory.LAND_PREP).map { it.id }
        )
    }

    @Test
    fun categoryProgressIsBoundedToAValidFraction() {
        assertEquals(0.25f, categorySpendProgress(categoryTotal = 25.0, totalExpenses = 100.0))
        assertEquals(1f, categorySpendProgress(categoryTotal = 120.0, totalExpenses = 100.0))
        assertEquals(0f, categorySpendProgress(categoryTotal = 0.0, totalExpenses = 100.0))
    }

    private fun expense(id: String, category: ExpenseCategory, amount: Double) = Expense(
        id = id,
        title = id,
        category = category,
        totalAmount = amount,
        transactionDate = LocalDate(2026, 1, 15),
        payingPartnerId = "partner"
    )

    private fun partnerSummary(
        id: String,
        contributed: Double,
        obligated: Double,
        overdue: Double
    ) = PartnerLedgerSummary(
        partnerId = id,
        partnerName = id,
        directOutOfPocket = 0.0,
        cashTranches = 0.0,
        totalContributed = contributed,
        totalObligated = obligated,
        netBalance = contributed - obligated,
        overdueAmount = overdue,
        complianceStatus = if (overdue > 0) ComplianceStatus.OVERDUE else ComplianceStatus.PARTIAL,
        investmentSharePercentage = 0.0
    )
}