package co.ke.xently.expense.tracker.server.repository

import co.ke.xently.expense.tracker.calculator.ExpenseCalculator
import co.ke.xently.expense.tracker.model.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDate

class InMemoryExpenseRepository(initial: List<Expense> = emptyList()) : ExpenseRepository {
    private val mutex = Mutex()
    private val storage = initial.associateBy { it.id }.toMutableMap()

    override suspend fun getAll(): List<Expense> = mutex.withLock {
        storage.values.toList()
    }

    override suspend fun getById(id: String): Expense? = mutex.withLock {
        storage[id]
    }

    override suspend fun create(expense: Expense): Expense = mutex.withLock {
        val splits = expense.splits.ifEmpty {
            ExpenseCalculator.calculateSplits(expense.totalAmount, expense.transactionDate)
        }
        val toSave = expense.copy(splits = splits)
        storage[toSave.id] = toSave
        toSave
    }

    override suspend fun update(expense: Expense): Expense? = mutex.withLock {
        if (!storage.containsKey(expense.id)) return null
        val splits = expense.splits.ifEmpty {
            ExpenseCalculator.calculateSplits(expense.totalAmount, expense.transactionDate)
        }
        val toSave = expense.copy(splits = splits)
        storage[toSave.id] = toSave
        toSave
    }

    override suspend fun delete(id: String): Boolean = mutex.withLock {
        storage.remove(id) != null
    }
}

class InMemoryTrancheRepository(initial: List<TranchePayment> = emptyList()) :
    TranchePaymentRepository {
    private val mutex = Mutex()
    private val storage = initial.associateBy { it.id }.toMutableMap()

    override suspend fun getAll(): List<TranchePayment> = mutex.withLock {
        storage.values.toList()
    }

    override suspend fun getById(id: String): TranchePayment? = mutex.withLock {
        storage[id]
    }

    override suspend fun create(payment: TranchePayment): TranchePayment = mutex.withLock {
        storage[payment.id] = payment
        payment
    }

    override suspend fun getByPartnerId(partnerId: String): List<TranchePayment> = mutex.withLock {
        storage.values.filter { it.partnerId.equals(partnerId, ignoreCase = true) }
    }

    override suspend fun delete(id: String): Boolean = mutex.withLock {
        storage.remove(id) != null
    }
}

class InMemoryPartnerRepository(initial: List<Partner> = emptyList()) : PartnerRepository {
    private val mutex = Mutex()
    private val storage = initial.associateBy { it.id }.toMutableMap()

    override suspend fun getAll(): List<Partner> = mutex.withLock {
        storage.values.toList()
    }

    override suspend fun getById(id: String): Partner? = mutex.withLock {
        storage[id]
    }

    override suspend fun save(partner: Partner): Partner = mutex.withLock {
        storage[partner.id] = partner
        partner
    }
}

class InMemoryNotificationRepository(initial: List<AppNotification> = emptyList()) :
    NotificationRepository {
    private val mutex = Mutex()
    private val storage = initial.associateBy { it.id }.toMutableMap()

    override suspend fun getAll(): List<AppNotification> = mutex.withLock {
        storage.values.toList().sortedByDescending { it.timestampEpochMs }
    }

    override suspend fun create(notification: AppNotification): AppNotification = mutex.withLock {
        storage[notification.id] = notification
        notification
    }

    override suspend fun getByPartnerId(partnerId: String): List<AppNotification> = mutex.withLock {
        storage.values.filter { it.partnerId.equals(partnerId, ignoreCase = true) }
    }
}

class InMemoryRepositoryProvider : RepositoryProvider {
    override val expenseRepository: ExpenseRepository
    override val trancheRepository: TranchePaymentRepository
    override val partnerRepository: PartnerRepository
    override val notificationRepository: NotificationRepository

    init {
        val initialPartners = listOf(
            Partner(
                id = PartnerId.HARRISON.name,
                name = PartnerId.HARRISON.displayName,
                email = PartnerId.HARRISON.email,
                phone = "+254700000001",
                fcmToken = "mock-fcm-token-harrison"
            ),
            Partner(
                id = PartnerId.HERMAN.name,
                name = PartnerId.HERMAN.displayName,
                email = PartnerId.HERMAN.email,
                phone = "+254700000002",
                fcmToken = "mock-fcm-token-herman"
            ),
            Partner(
                id = PartnerId.VIN.name,
                name = PartnerId.VIN.displayName,
                email = PartnerId.VIN.email,
                phone = "+254700000003",
                fcmToken = "mock-fcm-token-vin"
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

        partnerRepository = InMemoryPartnerRepository(initialPartners)
        expenseRepository = InMemoryExpenseRepository(listOf(exp1, exp2, exp3, exp4))
        trancheRepository = InMemoryTrancheRepository(listOf(tranche1, tranche2))
        notificationRepository = InMemoryNotificationRepository()
    }
}
