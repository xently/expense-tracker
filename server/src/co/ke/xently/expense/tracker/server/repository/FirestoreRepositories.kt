package co.ke.xently.expense.tracker.server.repository

import co.ke.xently.expense.tracker.model.AppNotification
import co.ke.xently.expense.tracker.model.Expense
import co.ke.xently.expense.tracker.model.ExpenseCategory
import co.ke.xently.expense.tracker.model.Partner
import co.ke.xently.expense.tracker.model.TranchePayment
import com.google.cloud.firestore.Firestore
import com.google.firebase.cloud.FirestoreClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

/**
 * Firestore implementation of RepositoryProvider using Firebase Admin Java SDK.
 * Can be initialized with a Firestore instance or lazily from FirestoreClient.getFirestore().
 */
class FirestoreExpenseRepository(
    private val firestoreProvider: () -> Firestore? = {
        runCatching { FirestoreClient.getFirestore() }.getOrNull()
    }
) : ExpenseRepository {

    private val fallback = InMemoryExpenseRepository()

    override suspend fun getAll(): List<Expense> = withContext(Dispatchers.IO) {
        val db = firestoreProvider() ?: return@withContext fallback.getAll()
        val snapshot = db.collection("expenses").get().get()
        snapshot.documents.mapNotNull { doc ->
            runCatching {
                Expense(
                    id = doc.id,
                    title = doc.getString("title") ?: "",
                    category = ExpenseCategory.fromString(doc.getString("category") ?: ""),
                    totalAmount = doc.getDouble("totalAmount") ?: 0.0,
                    currency = doc.getString("currency") ?: "KES",
                    transactionDate = LocalDate.parse(
                        doc.getString("transactionDate") ?: "2026-01-01"
                    ),
                    payingPartnerId = doc.getString("payingPartnerId") ?: "",
                    notes = doc.getString("notes") ?: "",
                    createdAtEpochMs = doc.getLong("createdAtEpochMs") ?: 0L
                )
            }.getOrNull()
        }
    }

    override suspend fun getById(id: String): Expense? = withContext(Dispatchers.IO) {
        val db = firestoreProvider() ?: return@withContext fallback.getById(id)
        val doc = db.collection("expenses").document(id).get().get()
        if (!doc.exists()) return@withContext null
        Expense(
            id = doc.id,
            title = doc.getString("title") ?: "",
            category = ExpenseCategory.fromString(doc.getString("category") ?: ""),
            totalAmount = doc.getDouble("totalAmount") ?: 0.0,
            currency = doc.getString("currency") ?: "KES",
            transactionDate = LocalDate.parse(doc.getString("transactionDate") ?: "2026-01-01"),
            payingPartnerId = doc.getString("payingPartnerId") ?: "",
            notes = doc.getString("notes") ?: "",
            createdAtEpochMs = doc.getLong("createdAtEpochMs") ?: 0L
        )
    }

    override suspend fun create(expense: Expense): Expense = withContext(Dispatchers.IO) {
        val db = firestoreProvider() ?: return@withContext fallback.create(expense)
        val data = mapOf(
            "title" to expense.title,
            "category" to expense.category.name,
            "totalAmount" to expense.totalAmount,
            "currency" to expense.currency,
            "transactionDate" to expense.transactionDate.toString(),
            "payingPartnerId" to expense.payingPartnerId,
            "notes" to expense.notes,
            "createdAtEpochMs" to expense.createdAtEpochMs
        )
        db.collection("expenses").document(expense.id).set(data).get()
        expense
    }

    override suspend fun update(expense: Expense): Expense? = withContext(Dispatchers.IO) {
        val db = firestoreProvider() ?: return@withContext fallback.update(expense)
        val docRef = db.collection("expenses").document(expense.id)
        if (!docRef.get().get().exists()) return@withContext null
        val data = mapOf(
            "title" to expense.title,
            "category" to expense.category.name,
            "totalAmount" to expense.totalAmount,
            "currency" to expense.currency,
            "transactionDate" to expense.transactionDate.toString(),
            "payingPartnerId" to expense.payingPartnerId,
            "notes" to expense.notes,
            "createdAtEpochMs" to expense.createdAtEpochMs
        )
        docRef.set(data).get()
        expense
    }

    override suspend fun delete(id: String): Boolean = withContext(Dispatchers.IO) {
        val db = firestoreProvider() ?: return@withContext fallback.delete(id)
        db.collection("expenses").document(id).delete().get()
        true
    }
}

class FirestoreTrancheRepository(
    private val firestoreProvider: () -> Firestore? = {
        runCatching { FirestoreClient.getFirestore() }.getOrNull()
    }
) : TranchePaymentRepository {

    private val fallback = InMemoryTrancheRepository()

    override suspend fun getAll(): List<TranchePayment> = withContext(Dispatchers.IO) {
        val db = firestoreProvider() ?: return@withContext fallback.getAll()
        val snapshot = db.collection("tranches").get().get()
        snapshot.documents.mapNotNull { doc ->
            runCatching {
                TranchePayment(
                    id = doc.id,
                    partnerId = doc.getString("partnerId") ?: "",
                    amount = doc.getDouble("amount") ?: 0.0,
                    paymentDate = LocalDate.parse(doc.getString("paymentDate") ?: "2026-01-01"),
                    notes = doc.getString("notes") ?: "",
                    referenceNumber = doc.getString("referenceNumber") ?: "",
                    createdAtEpochMs = doc.getLong("createdAtEpochMs") ?: 0L
                )
            }.getOrNull()
        }
    }

    override suspend fun getById(id: String): TranchePayment? = withContext(Dispatchers.IO) {
        val db = firestoreProvider() ?: return@withContext fallback.getById(id)
        val doc = db.collection("tranches").document(id).get().get()
        if (!doc.exists()) return@withContext null
        TranchePayment(
            id = doc.id,
            partnerId = doc.getString("partnerId") ?: "",
            amount = doc.getDouble("amount") ?: 0.0,
            paymentDate = LocalDate.parse(doc.getString("paymentDate") ?: "2026-01-01"),
            notes = doc.getString("notes") ?: "",
            referenceNumber = doc.getString("referenceNumber") ?: "",
            createdAtEpochMs = doc.getLong("createdAtEpochMs") ?: 0L
        )
    }

    override suspend fun create(payment: TranchePayment): TranchePayment =
        withContext(Dispatchers.IO) {
            val db = firestoreProvider() ?: return@withContext fallback.create(payment)
            val data = mapOf(
                "partnerId" to payment.partnerId,
                "amount" to payment.amount,
                "paymentDate" to payment.paymentDate.toString(),
                "notes" to payment.notes,
                "referenceNumber" to payment.referenceNumber,
                "createdAtEpochMs" to payment.createdAtEpochMs
            )
            db.collection("tranches").document(payment.id).set(data).get()
            payment
        }

    override suspend fun getByPartnerId(partnerId: String): List<TranchePayment> =
        withContext(Dispatchers.IO) {
            val db = firestoreProvider() ?: return@withContext fallback.getByPartnerId(partnerId)
            val snapshot =
                db.collection("tranches").whereEqualTo("partnerId", partnerId).get().get()
            snapshot.documents.mapNotNull { doc ->
                runCatching {
                    TranchePayment(
                        id = doc.id,
                        partnerId = doc.getString("partnerId") ?: "",
                        amount = doc.getDouble("amount") ?: 0.0,
                        paymentDate = LocalDate.parse(doc.getString("paymentDate") ?: "2026-01-01"),
                        notes = doc.getString("notes") ?: "",
                        referenceNumber = doc.getString("referenceNumber") ?: "",
                        createdAtEpochMs = doc.getLong("createdAtEpochMs") ?: 0L
                    )
                }.getOrNull()
            }
        }

    override suspend fun delete(id: String): Boolean = withContext(Dispatchers.IO) {
        val db = firestoreProvider() ?: return@withContext fallback.delete(id)
        db.collection("tranches").document(id).delete().get()
        true
    }
}

class FirestorePartnerRepository(
    private val firestoreProvider: () -> Firestore? = {
        runCatching { FirestoreClient.getFirestore() }.getOrNull()
    }
) : PartnerRepository {

    private val fallback = InMemoryPartnerRepository()

    override suspend fun getAll(): List<Partner> = withContext(Dispatchers.IO) {
        val db = firestoreProvider() ?: return@withContext fallback.getAll()
        val snapshot = db.collection("partners").get().get()
        snapshot.documents.mapNotNull { doc ->
            runCatching {
                Partner(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    email = doc.getString("email") ?: "",
                    phone = doc.getString("phone") ?: "",
                    fcmToken = doc.getString("fcmToken")
                )
            }.getOrNull()
        }
    }

    override suspend fun getById(id: String): Partner? = withContext(Dispatchers.IO) {
        val db = firestoreProvider() ?: return@withContext fallback.getById(id)
        val doc = db.collection("partners").document(id).get().get()
        if (!doc.exists()) return@withContext null
        Partner(
            id = doc.id,
            name = doc.getString("name") ?: "",
            email = doc.getString("email") ?: "",
            phone = doc.getString("phone") ?: "",
            fcmToken = doc.getString("fcmToken")
        )
    }

    override suspend fun save(partner: Partner): Partner = withContext(Dispatchers.IO) {
        val db = firestoreProvider() ?: return@withContext fallback.save(partner)
        val data = mapOf(
            "name" to partner.name,
            "email" to partner.email,
            "phone" to partner.phone,
            "fcmToken" to partner.fcmToken
        )
        db.collection("partners").document(partner.id).set(data).get()
        partner
    }
}

class FirestoreNotificationRepository(
    private val firestoreProvider: () -> Firestore? = {
        runCatching { FirestoreClient.getFirestore() }.getOrNull()
    }
) : NotificationRepository {

    private val fallback = InMemoryNotificationRepository()

    override suspend fun getAll(): List<AppNotification> = withContext(Dispatchers.IO) {
        val db = firestoreProvider() ?: return@withContext fallback.getAll()
        val snapshot = db.collection("notifications").get().get()
        snapshot.documents.mapNotNull { doc ->
            runCatching {
                AppNotification(
                    id = doc.id,
                    partnerId = doc.getString("partnerId") ?: "",
                    partnerName = doc.getString("partnerName") ?: "",
                    title = doc.getString("title") ?: "",
                    message = doc.getString("message") ?: "",
                    amountOverdue = doc.getDouble("amountOverdue") ?: 0.0,
                    monthYear = doc.getString("monthYear") ?: "",
                    timestampEpochMs = doc.getLong("timestampEpochMs") ?: 0L,
                    isSent = doc.getBoolean("isSent") ?: true,
                    fcmMessageId = doc.getString("fcmMessageId")
                )
            }.getOrNull()
        }
    }

    override suspend fun create(notification: AppNotification): AppNotification =
        withContext(Dispatchers.IO) {
            val db = firestoreProvider() ?: return@withContext fallback.create(notification)
            val data = mapOf(
                "partnerId" to notification.partnerId,
                "partnerName" to notification.partnerName,
                "title" to notification.title,
                "message" to notification.message,
                "amountOverdue" to notification.amountOverdue,
                "monthYear" to notification.monthYear,
                "timestampEpochMs" to notification.timestampEpochMs,
                "isSent" to notification.isSent,
                "fcmMessageId" to notification.fcmMessageId
            )
            db.collection("notifications").document(notification.id).set(data).get()
            notification
        }

    override suspend fun getByPartnerId(partnerId: String): List<AppNotification> =
        withContext(Dispatchers.IO) {
            val db = firestoreProvider() ?: return@withContext fallback.getByPartnerId(partnerId)
            val snapshot =
                db.collection("notifications").whereEqualTo("partnerId", partnerId).get().get()
            snapshot.documents.mapNotNull { doc ->
                runCatching {
                    AppNotification(
                        id = doc.id,
                        partnerId = doc.getString("partnerId") ?: "",
                        partnerName = doc.getString("partnerName") ?: "",
                        title = doc.getString("title") ?: "",
                        message = doc.getString("message") ?: "",
                        amountOverdue = doc.getDouble("amountOverdue") ?: 0.0,
                        monthYear = doc.getString("monthYear") ?: "",
                        timestampEpochMs = doc.getLong("timestampEpochMs") ?: 0L,
                        isSent = doc.getBoolean("isSent") ?: true,
                        fcmMessageId = doc.getString("fcmMessageId")
                    )
                }.getOrNull()
            }
        }
}

class FirestoreRepositoryProvider(firestore: Firestore? = null) : RepositoryProvider {
    override val expenseRepository: ExpenseRepository = FirestoreExpenseRepository { firestore }
    override val trancheRepository: TranchePaymentRepository =
        FirestoreTrancheRepository { firestore }
    override val partnerRepository: PartnerRepository = FirestorePartnerRepository { firestore }
    override val notificationRepository: NotificationRepository =
        FirestoreNotificationRepository { firestore }
}
