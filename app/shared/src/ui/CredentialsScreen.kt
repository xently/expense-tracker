package co.ke.xently.expense.tracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import co.ke.xently.expense.tracker.data.DEMO_ACCESS_TOKEN
import co.ke.xently.expense.tracker.data.defaultDemoServiceUrl
import co.ke.xently.expense.tracker.resources.Res
import co.ke.xently.expense.tracker.resources.app_name
import co.ke.xently.expense.tracker.resources.demo_mode_notice
import co.ke.xently.expense.tracker.resources.demo_service_url
import co.ke.xently.expense.tracker.resources.loading
import co.ke.xently.expense.tracker.resources.start_demo
import co.ke.xently.expense.tracker.theme.ExpenseTrackerTheme
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceEvent
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun CredentialsScreen(
    state: ProjectWorkspaceState,
    onEvent: (ProjectWorkspaceEvent) -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineMedium)
        Text(
            stringResource(Res.string.demo_mode_notice),
            modifier = Modifier.padding(vertical = 12.dp)
        )
        Text(
            stringResource(Res.string.demo_service_url, defaultDemoServiceUrl()),
            style = MaterialTheme.typography.bodySmall
        )
        Button(
            onClick = {
                onEvent(ProjectWorkspaceEvent.Connect(defaultDemoServiceUrl(), DEMO_ACCESS_TOKEN))
            },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(if (state.isLoading) Res.string.loading else Res.string.start_demo))
        }
    }
}

@PreviewLightDark
@PreviewDynamicColors
@PreviewScreenSizes
@Composable
private fun CredentialsScreenPreview() {
    ExpenseTrackerTheme { CredentialsScreen(ProjectWorkspaceState(), {}) }
}

@Preview(name = "Connecting")
@PreviewLightDark
@Composable
private fun ConnectingCredentialsScreenPreview() {
    ExpenseTrackerTheme { CredentialsScreen(ProjectWorkspaceState(isLoading = true), {}) }
}