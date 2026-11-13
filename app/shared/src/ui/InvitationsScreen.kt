package co.ke.xently.expense.tracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewDynamicColors
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import co.ke.xently.expense.tracker.InvitationCard
import co.ke.xently.expense.tracker.InvitePartnerForm
import co.ke.xently.expense.tracker.model.ProjectInvitationStatus
import co.ke.xently.expense.tracker.resources.Res
import co.ke.xently.expense.tracker.resources.invitations
import co.ke.xently.expense.tracker.resources.no_invitations
import co.ke.xently.expense.tracker.theme.ExpenseTrackerTheme
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceEvent
import co.ke.xently.expense.tracker.viewmodel.ProjectWorkspaceState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun InvitationsScreen(
    state: ProjectWorkspaceState,
    onEvent: (ProjectWorkspaceEvent) -> Unit
) {
    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text(stringResource(Res.string.invitations)) }
        val pending = state.invitations.filter { it.status == ProjectInvitationStatus.PENDING }
        if (pending.isEmpty()) item { Text(stringResource(Res.string.no_invitations)) }
        items(pending, key = { it.id }) { InvitationCard(it, onEvent) }
        if (state.isProjectOwner) item { InvitePartnerForm(onEvent) }
    }
}

@PreviewLightDark
@PreviewFontScale
@PreviewDynamicColors
@PreviewScreenSizes
@Composable
private fun InvitationsScreenPreview() {
    ExpenseTrackerTheme { InvitationsScreen(previewWorkspaceState) {} }
}

@Preview(name = "No invitations")
@PreviewLightDark
@Composable
private fun EmptyInvitationsScreenPreview() {
    ExpenseTrackerTheme { InvitationsScreen(previewEmptyWorkspaceState) {} }
}