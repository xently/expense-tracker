package co.ke.xently.expense.tracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import co.ke.xently.expense.tracker.InvitationCard
import co.ke.xently.expense.tracker.model.ProjectInvitationStatus
import co.ke.xently.expense.tracker.resources.Res
import co.ke.xently.expense.tracker.resources.add_category
import co.ke.xently.expense.tracker.resources.add_category_hint
import co.ke.xently.expense.tracker.resources.create_project
import co.ke.xently.expense.tracker.resources.expense_categories
import co.ke.xently.expense.tracker.resources.expense_category
import co.ke.xently.expense.tracker.resources.invitations
import co.ke.xently.expense.tracker.resources.no_invitations
import co.ke.xently.expense.tracker.resources.no_projects
import co.ke.xently.expense.tracker.resources.project_description
import co.ke.xently.expense.tracker.resources.project_name
import co.ke.xently.expense.tracker.resources.remove_category
import co.ke.xently.expense.tracker.resources.select_project
import co.ke.xently.expense.tracker.theme.ExpenseTrackerTheme
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceEvent
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ProjectsScreen(
    state: ProjectWorkspaceState,
    onEvent: (ProjectWorkspaceEvent) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var categoryDraft by rememberSaveable { mutableStateOf("") }
    var expenseCategories by rememberSaveable { mutableStateOf(emptyList<String>()) }
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                stringResource(Res.string.select_project),
                style = MaterialTheme.typography.headlineSmall
            )
        }
        if (state.projects.isEmpty()) item { Text(stringResource(Res.string.no_projects)) }
        items(state.projects, key = { it.id }) { project ->
            Card(
                onClick = { onEvent(ProjectWorkspaceEvent.SelectProject(project.id)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(project.name, style = MaterialTheme.typography.titleMedium)
                    if (project.description.isNotBlank()) Text(project.description)
                    if (project.expenseCategories.isNotEmpty()) {
                        Text(
                            project.expenseCategories.joinToString(" · "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        item {
            Text(
                stringResource(Res.string.create_project),
                style = MaterialTheme.typography.titleLarge
            )
        }
        item {
            OutlinedTextField(
                singleLine = true,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next,
                ),
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(Res.string.project_name)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                maxLines = 3,
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(Res.string.project_description)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Text(
                stringResource(Res.string.expense_categories),
                style = MaterialTheme.typography.titleMedium
            )
        }
        item {
            OutlinedTextField(
                value = categoryDraft,
                onValueChange = { categoryDraft = it },
                label = { Text(stringResource(Res.string.expense_category)) },
                supportingText = { Text(stringResource(Res.string.add_category_hint)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Button(
                onClick = {
                    val category = categoryDraft.trim()
                    if (category.isNotEmpty() && expenseCategories.none {
                            it.equals(
                                category,
                                true
                            )
                        }) {
                        expenseCategories = expenseCategories + category
                        categoryDraft = ""
                    }
                },
                enabled = categoryDraft.isNotBlank() && expenseCategories.none {
                    it.equals(categoryDraft.trim(), true)
                }
            ) { Text(stringResource(Res.string.add_category)) }
        }
        if (expenseCategories.isNotEmpty()) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(expenseCategories, key = { it }) { category ->
                        val removeDescription = stringResource(Res.string.remove_category, category)
                        InputChip(
                            selected = false,
                            onClick = { expenseCategories = expenseCategories - category },
                            modifier = Modifier.semantics {
                                contentDescription = removeDescription
                            },
                            label = { Text(category) },
                            trailingIcon = { Text("×") }
                        )
                    }
                }
            }
        }
        item {
            Button(
                onClick = {
                    onEvent(
                        ProjectWorkspaceEvent.CreateProject(
                            name,
                            description,
                            expenseCategories
                        )
                    )
                },
                enabled = name.isNotBlank() && expenseCategories.isNotEmpty() &&
                        categoryDraft.isBlank() && !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(Res.string.create_project)) }
        }
        item {
            Text(
                stringResource(Res.string.invitations),
                style = MaterialTheme.typography.titleLarge
            )
        }
        val pending = state.invitations.filter { it.status == ProjectInvitationStatus.PENDING }
        if (pending.isEmpty()) item { Text(stringResource(Res.string.no_invitations)) }
        items(pending, key = { it.id }) { InvitationCard(it, onEvent) }
    }
}

@PreviewLightDark
@PreviewFontScale
@PreviewDynamicColors
@PreviewScreenSizes
@Composable
private fun ProjectsScreenPreview() {
    ExpenseTrackerTheme { ProjectsScreen(previewWorkspaceState.copy(projects = emptyList())) {} }
}

@Preview(name = "Projects available")
@PreviewLightDark
@Composable
private fun ProjectsWithInvitationsScreenPreview() {
    ExpenseTrackerTheme { ProjectsScreen(previewWorkspaceState.copy(selectedProjectId = null)) {} }
}