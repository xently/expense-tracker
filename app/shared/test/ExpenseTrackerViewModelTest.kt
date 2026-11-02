package co.ke.xently.expense.tracker

import co.ke.xently.expense.tracker.model.ComplianceStatus
import co.ke.xently.expense.tracker.model.ExpenseCategory
import co.ke.xently.expense.tracker.model.PartnerId
import co.ke.xently.expense.tracker.viewmodel.ExpenseTrackerViewModel
import co.ke.xently.expense.tracker.viewmodel.NavigationTab
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class ExpenseTrackerViewModelTest {

    @Test
    fun testInitialState() {
        val vm = ExpenseTrackerViewModel()
        val state = vm.uiState.value

        assertEquals(NavigationTab.DASHBOARD, state.selectedTab)
        assertEquals(3, state.partners.size)
        assertTrue(state.expenses.isNotEmpty())
        assertTrue(state.tranches.isNotEmpty())
        assertNotNull(state.calculatedRevenueSplit)
        assertEquals(3, state.partnerSummaries.size)
    }

    @Test
    fun testNavigationAndThemeToggle() {
        val vm = ExpenseTrackerViewModel()
        assertEquals(NavigationTab.DASHBOARD, vm.uiState.value.selectedTab)

        vm.selectTab(NavigationTab.EXPENSES)
        assertEquals(NavigationTab.EXPENSES, vm.uiState.value.selectedTab)

        val initialTheme = vm.uiState.value.isDarkTheme
        vm.toggleTheme()
        assertEquals(!initialTheme, vm.uiState.value.isDarkTheme)
    }

    @Test
    fun testAddExpenseAndRecalculate() {
        val vm = ExpenseTrackerViewModel()
        val initialCount = vm.uiState.value.expenses.size
        val initialTotalExp = vm.uiState.value.dashboardSummary.totalExpenses

        vm.addExpense(
            title = "Weeding Contract 2",
            category = ExpenseCategory.WEEDING,
            totalAmount = 15000.0,
            transactionDate = LocalDate(2026, 8, 1),
            payingPartnerId = PartnerId.HARRISON.name,
            notes = "Second round manual weeding"
        )

        val state = vm.uiState.value
        assertEquals(initialCount + 1, state.expenses.size)
        assertEquals(initialTotalExp + 15000.0, state.dashboardSummary.totalExpenses)
        assertNotNull(state.infoMessage)
    }

    @Test
    fun testAddTrancheAndRecalculate() {
        val vm = ExpenseTrackerViewModel()
        val initialTranchesCount = vm.uiState.value.tranches.size

        vm.addTranche(
            partnerId = PartnerId.VIN.name,
            amount = 18000.0,
            paymentDate = LocalDate(2026, 8, 10),
            referenceNumber = "MPESA-TEST123",
            notes = "Tranche payment for harvesting prep"
        )

        val state = vm.uiState.value
        assertEquals(initialTranchesCount + 1, state.tranches.size)
        assertNotNull(state.infoMessage)
    }

    @Test
    fun testUpdateRevenueInputs() {
        val vm = ExpenseTrackerViewModel()
        vm.updateRevenueInputs("600000", "80000")

        val state = vm.uiState.value
        val rev = state.calculatedRevenueSplit
        assertNotNull(rev)
        assertEquals(600000.0, rev.grossFactoryPayout)
        assertEquals(80000.0, rev.harvestOperationsExpenses)
        assertEquals(520000.0, rev.netFactoryPayout)
    }

    @Test
    fun testTriggerOverdueCheck() {
        val vm = ExpenseTrackerViewModel()
        vm.triggerOverdueCheck()

        val state = vm.uiState.value
        assertNotNull(state.infoMessage)
        assertTrue(state.notifications.isNotEmpty())
    }
}
