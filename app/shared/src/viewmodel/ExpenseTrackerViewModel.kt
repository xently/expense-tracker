package co.ke.xently.expense.tracker.viewmodel

import androidx.lifecycle.ViewModel
import co.ke.xently.expense.tracker.calculator.ExpenseCalculator
import co.ke.xently.expense.tracker.calculator.LedgerCalculator
import co.ke.xently.expense.tracker.calculator.RevenueSplitCalculator
import co.ke.xently.expense.tracker.model.AppNotification
import co.ke.xently.expense.tracker.model.ComplianceStatus
import co.ke.xently.expense.tracker.model.DashboardSummary
import co.ke.xently.expense.tracker.model.Expense
import co.ke.xently.expense.tracker.model.ExpenseCategory
import co.ke.xently.expense.tracker.model.Partner
import co.ke.xently.expense.tracker.model.PartnerId
import co.ke.xently.expense.tracker.model.PartnerLedgerSummary
import co.ke.xently.expense.tracker.model.SeasonRevenueCalculation
import co.ke.xently.expense.tracker.model.TranchePayment
import co.ke.xently.expense.tracker.util.getCurrentEpochMs
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.datetime.LocalDate

enum class NavigationTab(val title: String) {
    DASHBOARD("Dashboard"),
    EXPENSES("Expenses"),
    PARTNERS("Partner Ledgers"),
    REVENUE_SPLIT("Revenue Split"),
    NOTIFICATIONS("Alerts & FCM")
}

data class ExpenseTrackerUiState(
    val selectedTab: NavigationTab = NavigationTab.DASHBOARD,
    val isDarkTheme: Boolean = false,
    val asOfDate: LocalDate = LocalDate(2026, 11, 2),
    val partners: List<Partner> = ExpenseCalculator.DEFAULT_PARTNERS,
    val expenses: List<Expense> = emptyList(),
    val tranches: List<TranchePayment> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val selectedCategoryFilter: ExpenseCategory? = null,
    val revenueGrossPayoutInput: String = "450000",
    val revenueHarvestExpensesInput: String = "60000",
    val calculatedRevenueSplit: SeasonRevenueCalculation? = null,
    val showAddExpenseDialog: Boolean = false,
    val showAddTrancheDialog: Boolean = false,
    val infoMessage: String? = null
) {
    val partnerSummaries: List<PartnerLedgerSummary>
        get() = LedgerCalculator.computePartnerSummaries(expenses, tranches, partners, asOfDate)

    val dashboardSummary: DashboardSummary
        get() = LedgerCalculator.computeDashboardSummary(
            expenses,
            tranches,
            notifications,
            partners,
            asOfDate
        )

    val filteredExpenses: List<Expense>
        get() = if (selectedCategoryFilter != null) {
            expenses.filter { it.category == selectedCategoryFilter }
        } else {
            expenses
        }.sortedByDescending { it.transactionDate }
}

class ExpenseTrackerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseTrackerUiState())
    val uiState: StateFlow<ExpenseTrackerUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        val initialPartners = listOf(
            Partner(
                id = PartnerId.HARRISON.name,
                name = PartnerId.HARRISON.displayName,
                email = PartnerId.HARRISON.email,
                phone = "+254700000001",
                fcmToken = "mock-fcm-harrison"
            ),
            Partner(
                id = PartnerId.HERMAN.name,
                name = PartnerId.HERMAN.displayName,
                email = PartnerId.HERMAN.email,
                phone = "+254700000002",
                fcmToken = "mock-fcm-herman"
            ),
            Partner(
                id = PartnerId.VIN.name,
                name = PartnerId.VIN.displayName,
                email = PartnerId.VIN.email,
                phone = "+254700000003",
                fcmToken = "mock-fcm-vin"
            )
        )

        val expDate1 = LocalDate(2026, 3, 10)
        val exp1 = Expense(
            id = "exp-001",
            title = "Tractor Harrowing & Land Preparation (15 Acres)",
            category = ExpenseCategory.LAND_PREP,
            totalAmount = 75000.0,
            transactionDate = expDate1,
            payingPartnerId = PartnerId.HARRISON.name,
            notes = "First and second pass disc harrowing",
            splits = ExpenseCalculator.calculateSplits(75000.0, expDate1, initialPartners),
            createdAtEpochMs = 1773187200000L
        )

        val expDate2 = LocalDate(2026, 4, 5)
        val exp2 = Expense(
            id = "exp-002",
            title = "Certified Sugarcane Seedlings Delivery",
            category = ExpenseCategory.SEEDLINGS,
            totalAmount = 60000.0,
            transactionDate = expDate2,
            payingPartnerId = PartnerId.HERMAN.name,
            notes = "Early maturing cane variety stalks",
            splits = ExpenseCalculator.calculateSplits(60000.0, expDate2, initialPartners),
            createdAtEpochMs = 1775347200000L
        )

        val expDate3 = LocalDate(2026, 5, 20)
        val exp3 = Expense(
            id = "exp-003",
            title = "Basal Fertilizer Application (CAN & NPK)",
            category = ExpenseCategory.FERTILIZER,
            totalAmount = 45000.0,
            transactionDate = expDate3,
            payingPartnerId = PartnerId.HARRISON.name,
            notes = "30 bags applied during planting",
            splits = ExpenseCalculator.calculateSplits(45000.0, expDate3, initialPartners),
            createdAtEpochMs = 1779235200000L
        )

        val expDate4 = LocalDate(2026, 7, 12)
        val exp4 = Expense(
            id = "exp-004",
            title = "First Manual Weeding & Furrow Maintenance",
            category = ExpenseCategory.WEEDING,
            totalAmount = 30000.0,
            transactionDate = expDate4,
            payingPartnerId = PartnerId.VIN.name,
            notes = "Casual labor contract for 15 acres",
            splits = ExpenseCalculator.calculateSplits(30000.0, expDate4, initialPartners),
            createdAtEpochMs = 1783814400000L
        )

        val tranche1 = TranchePayment(
            id = "tr-001",
            partnerId = PartnerId.VIN.name,
            amount = 25000.0,
            paymentDate = LocalDate(2026, 4, 8),
            notes = "Tranche contribution for Land Prep and Seedlings share",
            referenceNumber = "MPESA-QAB12345",
            createdAtEpochMs = 1775606400000L
        )

        val tranche2 = TranchePayment(
            id = "tr-002",
            partnerId = PartnerId.HERMAN.name,
            amount = 20000.0,
            paymentDate = LocalDate(2026, 6, 2),
            notes = "Tranche contribution towards fertilizer cost",
            referenceNumber = "MPESA-QAC67890",
            createdAtEpochMs = 1780358400000L
        )

        val notif1 = AppNotification(
            id = "notif-seed-01",
            partnerId = PartnerId.VIN.name,
            partnerName = PartnerId.VIN.displayName,
            title = "Overdue Farming Expense Share",
            message = "Reminder: Your farming expense share of KES 45,000 for October is past due",
            amountOverdue = 45000.0,
            monthYear = "October 2026",
            timestampEpochMs = 1793740800000L,
            isSent = true,
            fcmMessageId = "fcm-msg-oct-vin"
        )

        val initialExpenses = listOf(exp1, exp2, exp3, exp4)
        val initialTranches = listOf(tranche1, tranche2)
        val initialNotifs = listOf(notif1)

        val summaries = LedgerCalculator.computePartnerSummaries(
            expenses = initialExpenses,
            tranches = initialTranches,
            partners = initialPartners,
            asOfDate = LocalDate(2026, 11, 2)
        )

        val initialRevenueSplit = RevenueSplitCalculator.calculate(
            grossFactoryPayout = 450000.0,
            harvestOperationsExpenses = 60000.0,
            partnerLedgerSummaries = summaries
        )

        _uiState.value = ExpenseTrackerUiState(
            partners = initialPartners,
            expenses = initialExpenses,
            tranches = initialTranches,
            notifications = initialNotifs,
            calculatedRevenueSplit = initialRevenueSplit
        )
    }

    fun selectTab(tab: NavigationTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun toggleTheme() {
        _uiState.update { it.copy(isDarkTheme = !it.isDarkTheme) }
    }

    fun setCategoryFilter(category: ExpenseCategory?) {
        _uiState.update { it.copy(selectedCategoryFilter = category) }
    }

    fun setShowAddExpenseDialog(show: Boolean) {
        _uiState.update { it.copy(showAddExpenseDialog = show) }
    }

    fun setShowAddTrancheDialog(show: Boolean) {
        _uiState.update { it.copy(showAddTrancheDialog = show) }
    }

    fun clearInfoMessage() {
        _uiState.update { it.copy(infoMessage = null) }
    }

    fun addExpense(
        title: String,
        category: ExpenseCategory,
        totalAmount: Double,
        transactionDate: LocalDate,
        payingPartnerId: String,
        notes: String
    ) {
        val splits =
            ExpenseCalculator.calculateSplits(totalAmount, transactionDate, _uiState.value.partners)
        val nowMs = getCurrentEpochMs()
        val newExpense = Expense(
            id = "exp-${nowMs.toString().takeLast(6)}",
            title = title,
            category = category,
            totalAmount = totalAmount,
            currency = "KES",
            transactionDate = transactionDate,
            payingPartnerId = payingPartnerId,
            notes = notes,
            splits = splits,
            createdAtEpochMs = nowMs
        )

        _uiState.update { state ->
            val updatedExpenses = state.expenses + newExpense
            val summaries = LedgerCalculator.computePartnerSummaries(
                expenses = updatedExpenses,
                tranches = state.tranches,
                partners = state.partners,
                asOfDate = state.asOfDate
            )
            val updatedRev = state.calculatedRevenueSplit?.let {
                RevenueSplitCalculator.calculate(
                    grossFactoryPayout = it.grossFactoryPayout,
                    harvestOperationsExpenses = it.harvestOperationsExpenses,
                    partnerLedgerSummaries = summaries
                )
            }

            state.copy(
                expenses = updatedExpenses,
                calculatedRevenueSplit = updatedRev,
                showAddExpenseDialog = false,
                infoMessage = "Expense of KES ${totalAmount.toLong()} added successfully!"
            )
        }
    }

    fun deleteExpense(id: String) {
        _uiState.update { state ->
            val updatedExpenses = state.expenses.filterNot { it.id == id }
            val summaries = LedgerCalculator.computePartnerSummaries(
                expenses = updatedExpenses,
                tranches = state.tranches,
                partners = state.partners,
                asOfDate = state.asOfDate
            )
            val updatedRev = state.calculatedRevenueSplit?.let {
                RevenueSplitCalculator.calculate(
                    grossFactoryPayout = it.grossFactoryPayout,
                    harvestOperationsExpenses = it.harvestOperationsExpenses,
                    partnerLedgerSummaries = summaries
                )
            }

            state.copy(
                expenses = updatedExpenses,
                calculatedRevenueSplit = updatedRev,
                infoMessage = "Expense deleted"
            )
        }
    }

    fun addTranche(
        partnerId: String,
        amount: Double,
        paymentDate: LocalDate,
        referenceNumber: String,
        notes: String
    ) {
        val nowMs = getCurrentEpochMs()
        val newTranche = TranchePayment(
            id = "tr-${nowMs.toString().takeLast(6)}",
            partnerId = partnerId,
            amount = amount,
            paymentDate = paymentDate,
            notes = notes,
            referenceNumber = referenceNumber,
            createdAtEpochMs = nowMs
        )

        _uiState.update { state ->
            val updatedTranches = state.tranches + newTranche
            val summaries = LedgerCalculator.computePartnerSummaries(
                expenses = state.expenses,
                tranches = updatedTranches,
                partners = state.partners,
                asOfDate = state.asOfDate
            )
            val updatedRev = state.calculatedRevenueSplit?.let {
                RevenueSplitCalculator.calculate(
                    grossFactoryPayout = it.grossFactoryPayout,
                    harvestOperationsExpenses = it.harvestOperationsExpenses,
                    partnerLedgerSummaries = summaries
                )
            }

            state.copy(
                tranches = updatedTranches,
                calculatedRevenueSplit = updatedRev,
                showAddTrancheDialog = false,
                infoMessage = "Tranche payment of KES ${amount.toLong()} recorded successfully!"
            )
        }
    }

    fun updateRevenueInputs(gross: String, harvestExpenses: String) {
        _uiState.update {
            it.copy(
                revenueGrossPayoutInput = gross,
                revenueHarvestExpensesInput = harvestExpenses
            )
        }
        val grossVal = gross.toDoubleOrNull() ?: 0.0
        val harvestVal = harvestExpenses.toDoubleOrNull() ?: 0.0
        val summaries = _uiState.value.partnerSummaries
        val calculation = RevenueSplitCalculator.calculate(grossVal, harvestVal, summaries)
        _uiState.update { it.copy(calculatedRevenueSplit = calculation) }
    }

    fun triggerOverdueCheck() {
        val state = _uiState.value
        val summaries = state.partnerSummaries
        val newNotifs = mutableListOf<AppNotification>()
        val monthName = state.asOfDate.month.name.lowercase().replaceFirstChar { it.uppercase() }

        val nowMs = getCurrentEpochMs()
        for ((partnerId, partnerName, _, _, _, _, _, overdueAmount, complianceStatus) in summaries) {
            if (complianceStatus == ComplianceStatus.OVERDUE && overdueAmount > 0.0) {
                val notif = AppNotification(
                    id = "notif-${nowMs.toString().takeLast(6)}-$partnerId",
                    partnerId = partnerId,
                    partnerName = partnerName,
                    title = "Overdue Farming Expense Share",
                    message = "Reminder: Your farming expense share of KES ${overdueAmount.toLong()} for $monthName is past due",
                    amountOverdue = overdueAmount,
                    monthYear = "$monthName ${state.asOfDate.year}",
                    timestampEpochMs = nowMs,
                    isSent = true,
                    fcmMessageId = "fcm-$nowMs"
                )
                newNotifs.add(notif)
            }
        }

        _uiState.update {
            it.copy(
                notifications = newNotifs + it.notifications,
                infoMessage = if (newNotifs.isNotEmpty()) {
                    "Dispatched ${newNotifs.size} overdue notification(s) via FCM push!"
                } else {
                    "All partner balances are currently compliant! No overdue notifications needed."
                }
            )
        }
    }
}
