package co.ke.xently.expense.tracker

import androidx.compose.ui.unit.dp
import co.ke.xently.expense.tracker.ui.destination
import co.ke.xently.expense.tracker.ui.drawerOnlyNavigationTabs
import co.ke.xently.expense.tracker.ui.primaryNavigationTabs
import co.ke.xently.expense.tracker.ui.usesPermanentNavigationDrawer
import co.ke.xently.expense.tracker.ui.workspaceNavigationTabs
import co.ke.xently.expense.tracker.viewmodel.NavigationTab
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class WorkspaceNavigationTest {
    @Test
    fun notificationsAndInvitationsRemainSeparateDestinations() {
        assertEquals(WorkspaceDestination.Notifications, NavigationTab.NOTIFICATIONS.destination())
        assertEquals(WorkspaceDestination.Invitations, NavigationTab.INVITATIONS.destination())
        assertNotEquals(
            NavigationTab.NOTIFICATIONS.destination(),
            NavigationTab.INVITATIONS.destination()
        )
    }

    @Test
    fun bottomNavigationHasAtMostFiveItemsAndOtherDestinationsRemainInDrawer() {
        assertEquals(4, primaryNavigationTabs.size)
        assertEquals(
            listOf(NavigationTab.NOTIFICATIONS, NavigationTab.INVITATIONS),
            drawerOnlyNavigationTabs
        )
        assertEquals(6, workspaceNavigationTabs.size)
        assertEquals(NavigationTab.entries.toSet(), workspaceNavigationTabs.toSet())
    }

    @Test
    fun allNavigationTabsMapToTheirNavigation3Destinations() {
        assertEquals(
            listOf(
                WorkspaceDestination.Dashboard,
                WorkspaceDestination.Expenses,
                WorkspaceDestination.Partners,
                WorkspaceDestination.Settlement,
                WorkspaceDestination.Notifications,
                WorkspaceDestination.Invitations
            ),
            workspaceNavigationTabs.map { it.destination() }
        )
    }

    @Test
    fun permanentDrawerUsesExpandedWidthBreakpoint() {
        assertFalse(usesPermanentNavigationDrawer(839.dp))
        assertTrue(usesPermanentNavigationDrawer(840.dp))
    }
}