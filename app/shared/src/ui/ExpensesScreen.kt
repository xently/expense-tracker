package co.ke.xently.expense.tracker.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import co.ke.xently.expense.tracker.categoryLabel
import co.ke.xently.expense.tracker.model.Expense
import co.ke.xently.expense.tracker.resources.Res
import co.ke.xently.expense.tracker.resources.cancel
import co.ke.xently.expense.tracker.resources.currency_amount
import co.ke.xently.expense.tracker.resources.delete
import co.ke.xently.expense.tracker.resources.dialog_text_delete_expense
import co.ke.xently.expense.tracker.resources.dialog_title_delete_expense
import co.ke.xently.expense.tracker.resources.due_date
import co.ke.xently.expense.tracker.resources.expense_filter_all_count
import co.ke.xently.expense.tracker.resources.expense_filter_category_count
import co.ke.xently.expense.tracker.resources.expense_paid_by_date
import co.ke.xently.expense.tracker.resources.log_expense
import co.ke.xently.expense.tracker.resources.no_expenses
import co.ke.xently.expense.tracker.resources.no_matching_expenses
import co.ke.xently.expense.tracker.resources.partner_no_longer_listed
import co.ke.xently.expense.tracker.resources.split_details
import co.ke.xently.expense.tracker.resources.split_details_unavailable
import co.ke.xently.expense.tracker.theme.ExpenseTrackerTheme
import co.ke.xently.expense.tracker.theme.Icons
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceEvent
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ExpensesScreen(
    state: ProjectWorkspaceState,
    onEvent: (ProjectWorkspaceEvent) -> Unit
) {
    var selectedCategory by remember(state.selectedProjectId) {
        mutableStateOf<DisplayCategory?>(null)
    }
    val visibleExpenses = filterExpensesByCategoryKey(state.expenses, selectedCategory)
    val categories =
        expenseCategoryOptions(state.selectedProject?.expenseCategories.orEmpty(), state.expenses)
    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = {
                            Text(
                                stringResource(
                                    Res.string.expense_filter_all_count,
                                    state.expenses.size
                                )
                            )
                        }
                    )
                    categories.forEach { category ->
                        val categoryTitle = when (category) {
                            is DisplayCategory.Project -> category.name
                            is DisplayCategory.Legacy -> categoryLabel(category.category)
                        }
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = {
                                Text(
                                    stringResource(
                                        Res.string.expense_filter_category_count,
                                        categoryTitle,
                                        state.expenses.count { expenseCategoryKey(it) == category }
                                    )
                                )
                            }
                        )
                    }
                }
            }
            if (visibleExpenses.isEmpty()) {
                item {
                    Text(
                        stringResource(
                            if (state.expenses.isEmpty()) Res.string.no_expenses
                            else Res.string.no_matching_expenses
                        )
                    )
                }
            }
            items(visibleExpenses, key = { it.id }) { expense ->
                ExpenseDetailCard(expense, state, onEvent)
            }
        }
        FloatingActionButton(
            onClick = { onEvent(ProjectWorkspaceEvent.OpenExpenseForm) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Text(
                stringResource(Res.string.log_expense),
                modifier = Modifier.padding(horizontal = 16.dp),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun ExpenseDetailCard(
    expense: Expense,
    state: ProjectWorkspaceState,
    onEvent: (ProjectWorkspaceEvent) -> Unit
) {
    val expensePaymentDescription = stringResource(
        Res.string.expense_paid_by_date,
        partnerName(state, expense.payingPartnerId),
        expense.transactionDate.toString()
    )
    var showDeletionConfirmationDialog by rememberSaveable {
        mutableStateOf(false)
    }
    if (showDeletionConfirmationDialog) {
        DeleteExpenseConfirmationDialog(
            expensePaymentDescription = "${expense.title} - $expensePaymentDescription",
            onDismissRequest = { showDeletionConfirmationDialog = false },
            onConfirmation = { onEvent(ProjectWorkspaceEvent.DeleteExpense(expenseId = expense.id)) },
        )
    }
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            expense.categoryName ?: categoryLabel(expense.category),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        expense.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    stringResource(
                        Res.string.currency_amount,
                        expense.totalAmount.toLong().toString()
                    ),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = expensePaymentDescription,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            AnimatedVisibility(expense.notes.isNotBlank()) {
                Text(expense.notes)
            }
            HorizontalDivider()
            Text(
                stringResource(Res.string.split_details),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            if (expense.splits.isEmpty()) {
                Text(
                    stringResource(Res.string.split_details_unavailable),
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                expense.splits.forEach { split ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = partnerName(state, split.partnerId))
                        Text(
                            stringResource(
                                Res.string.currency_amount,
                                split.shareAmount.toLong().toString()
                            )
                        )
                    }
                    Text(
                        stringResource(Res.string.due_date, split.dueDate.toString()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small.copy(CornerSize(25.dp)),
                contentPadding = PaddingValues(0.dp),
                onClick = { showDeletionConfirmationDialog = true },
            ) {
                Text(
                    text = stringResource(Res.string.delete).toUpperCase(Locale.current),
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun partnerName(
    state: ProjectWorkspaceState,
    payingPartnerId: String
): String = remember(state.partners) {
    state.partners.firstOrNull { it.id == payingPartnerId }?.name
} ?: stringResource(Res.string.partner_no_longer_listed)

@Composable
private fun DeleteExpenseConfirmationDialog(
    expensePaymentDescription: String,
    onDismissRequest: () -> Unit,
    onConfirmation: () -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(imageVector = Icons.delete, contentDescription = null)
        },
        title = {
            Text(text = stringResource(Res.string.dialog_title_delete_expense))
        },
        text = {
            Text(
                text = stringResource(
                    Res.string.dialog_text_delete_expense,
                    expensePaymentDescription
                )
            )
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = onConfirmation) {
                Text(stringResource(Res.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}

@PreviewLightDark
@PreviewFontScale
@PreviewDynamicColors
@PreviewScreenSizes
@Composable
private fun ExpensesScreenPreview() {
    ExpenseTrackerTheme { ExpensesScreen(previewWorkspaceState) {} }
}

@Preview(name = "No expenses")
@PreviewLightDark
@Composable
private fun EmptyExpensesScreenPreview() {
    ExpenseTrackerTheme { ExpensesScreen(previewEmptyWorkspaceState) {} }
}