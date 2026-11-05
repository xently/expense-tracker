package co.ke.xently.expense.tracker.ui

import co.ke.xently.expense.tracker.model.Expense
import co.ke.xently.expense.tracker.model.ExpenseCategory
import co.ke.xently.expense.tracker.model.PartnerLedgerSummary

internal data class FinancialOverviewTotals(
    val expenses: Double,
    val contributions: Double,
    val outstanding: Double,
    val overdue: Double
)

internal fun financialOverviewTotals(
    expenses: List<Expense>,
    partners: List<PartnerLedgerSummary>
): FinancialOverviewTotals = FinancialOverviewTotals(
    expenses = expenses.sumOf { it.totalAmount },
    contributions = partners.sumOf { it.totalContributed },
    outstanding = partners.sumOf { (-it.netBalance).coerceAtLeast(0.0) },
    overdue = partners.sumOf { it.overdueAmount }
)

internal fun filterExpensesByCategory(
    expenses: List<Expense>,
    category: ExpenseCategory?
): List<Expense> = if (category == null) expenses else expenses.filter { it.category == category }

internal fun categorySpendProgress(categoryTotal: Double, totalExpenses: Double): Float =
    if (totalExpenses <= 0.0) 0f else (categoryTotal / totalExpenses).coerceIn(0.0, 1.0).toFloat()