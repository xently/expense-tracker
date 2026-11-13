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

internal sealed interface DisplayCategory {
    data class Project(val name: String) : DisplayCategory
    data class Legacy(val category: ExpenseCategory) : DisplayCategory
}

internal fun expenseCategoryKey(expense: Expense): DisplayCategory = expense.categoryName?.let {
    DisplayCategory.Project(it)
} ?: DisplayCategory.Legacy(expense.category)

internal fun expenseCategoryOptions(
    projectCategories: List<String>,
    expenses: List<Expense>
): List<DisplayCategory> {
    val configured = if (projectCategories.isNotEmpty()) {
        projectCategories.map { DisplayCategory.Project(it) }
    } else {
        ExpenseCategory.entries.map { DisplayCategory.Legacy(it) }
    }
    return (configured + expenses.map(::expenseCategoryKey)).distinct()
}

internal fun filterExpensesByCategoryKey(
    expenses: List<Expense>,
    category: DisplayCategory?
): List<Expense> = if (category == null) expenses else expenses.filter {
    expenseCategoryKey(it) == category
}

internal fun filterExpensesByCategory(
    expenses: List<Expense>,
    category: ExpenseCategory?
): List<Expense> = filterExpensesByCategoryKey(expenses, category?.let(DisplayCategory::Legacy))

internal fun filterExpensesByCategoryName(
    expenses: List<Expense>,
    categoryName: String?
): List<Expense> =
    filterExpensesByCategoryKey(expenses, categoryName?.let(DisplayCategory::Project))

internal fun categorySpendProgress(categoryTotal: Double, totalExpenses: Double): Float =
    if (totalExpenses <= 0.0) 0f else (categoryTotal / totalExpenses).coerceIn(0.0, 1.0).toFloat()