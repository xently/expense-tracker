package co.ke.xently.expense.tracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import co.ke.xently.expense.tracker.resources.Res
import co.ke.xently.expense.tracker.resources.display_name
import co.ke.xently.expense.tracker.resources.profile_setup
import co.ke.xently.expense.tracker.resources.save_profile
import co.ke.xently.expense.tracker.resources.username
import co.ke.xently.expense.tracker.theme.ExpenseTrackerTheme
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceEvent
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RegistrationScreen(
    state: ProjectWorkspaceState,
    onEvent: (ProjectWorkspaceEvent) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            stringResource(Res.string.profile_setup),
            style = MaterialTheme.typography.headlineSmall
        )
        OutlinedTextField(
            username,
            { username = it },
            label = { Text(stringResource(Res.string.username)) },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            displayName,
            { displayName = it },
            label = { Text(stringResource(Res.string.display_name)) },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { onEvent(ProjectWorkspaceEvent.Register(username, displayName)) },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.save_profile))
        }
    }
}

@PreviewLightDark
@PreviewFontScale
@PreviewDynamicColors
@PreviewScreenSizes
@Composable
private fun RegistrationScreenPreview() {
    ExpenseTrackerTheme { RegistrationScreen(ProjectWorkspaceState(), {}) }
}

@Preview(name = "Loading registration")
@PreviewLightDark
@Composable
private fun LoadingRegistrationScreenPreview() {
    ExpenseTrackerTheme { RegistrationScreen(ProjectWorkspaceState(isLoading = true), {}) }
}