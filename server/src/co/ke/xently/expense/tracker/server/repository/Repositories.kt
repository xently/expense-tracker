package co.ke.xently.expense.tracker.server.repository

import co.ke.xently.expense.tracker.model.AppNotification
import co.ke.xently.expense.tracker.model.Expense
import co.ke.xently.expense.tracker.model.Partner
import co.ke.xently.expense.tracker.model.TranchePayment

interface ExpenseRepository {
    suspend fun getAll(): List<Expense>
    suspend fun getById(id: String): Expense?
    suspend fun create(expense: Expense): Expense
    suspend fun update(expense: Expense): Expense?
    suspend fun delete(id: String): Boolean
}

interface TranchePaymentRepository {
    suspend fun getAll(): List<TranchePayment>
    suspend fun getById(id: String): TranchePayment?
    suspend fun create(payment: TranchePayment): TranchePayment
    suspend fun getByPartnerId(partnerId: String): List<TranchePayment>
    suspend fun delete(id: String): Boolean
}

interface PartnerRepository {
    suspend fun getAll(): List<Partner>
    suspend fun getById(id: String): Partner?
    suspend fun save(partner: Partner): Partner
}

interface NotificationRepository {
    suspend fun getAll(): List<AppNotification>
    suspend fun create(notification: AppNotification): AppNotification
    suspend fun getByPartnerId(partnerId: String): List<AppNotification>
}

interface RepositoryProvider {
    val expenseRepository: ExpenseRepository
    val trancheRepository: TranchePaymentRepository
    val partnerRepository: PartnerRepository
    val notificationRepository: NotificationRepository
}
