package co.ke.xently.expense.tracker.server.notification

import co.ke.xently.expense.tracker.calculator.LedgerCalculator
import co.ke.xently.expense.tracker.model.AppNotification
import co.ke.xently.expense.tracker.model.ComplianceStatus
import co.ke.xently.expense.tracker.model.ServerEvent
import co.ke.xently.expense.tracker.server.repository.RepositoryProvider
import co.ke.xently.expense.tracker.server.sse.EventBroadcaster
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.Message
import com.google.firebase.messaging.Notification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate
import org.slf4j.LoggerFactory
import java.util.UUID

interface PushNotificationService {
    suspend fun sendPushNotification(
        fcmToken: String,
        title: String,
        body: String,
        data: Map<String, String> = emptyMap()
    ): String?
}

class FcmPushNotificationService(
    private val messagingProvider: () -> FirebaseMessaging? = {
        runCatching { FirebaseMessaging.getInstance() }.getOrNull()
    }
) : PushNotificationService {

    private val logger = LoggerFactory.getLogger(FcmPushNotificationService::class.java)

    override suspend fun sendPushNotification(
        fcmToken: String,
        title: String,
        body: String,
        data: Map<String, String>
    ): String? = withContext(Dispatchers.IO) {
        val fcm = messagingProvider()
        if (fcm == null) {
            logger.info("[MOCK FCM PUSH] Sent to token '$fcmToken': '$title' - '$body'")
            return@withContext "mock-msg-${UUID.randomUUID()}"
        }

        try {
            val messageBuilder = Message.builder()
                .setToken(fcmToken)
                .setNotification(
                    Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build()
                )
            data.forEach { (k, v) -> messageBuilder.putData(k, v) }
            val response = fcm.send(messageBuilder.build())
            logger.info("Successfully sent FCM message: $response to $fcmToken")
            response
        } catch (e: Exception) {
            logger.error("Error sending FCM notification to $fcmToken: ${e.message}", e)
            null
        }
    }
}

class OverdueNotificationManager(
    private val repositoryProvider: RepositoryProvider,
    private val pushNotificationService: PushNotificationService,
    private val eventBroadcaster: EventBroadcaster
) {
    private val logger = LoggerFactory.getLogger(OverdueNotificationManager::class.java)

    /**
     * Checks monthly due dates (the 10th day of the month following transaction date)
     * and flags outstanding balances past due date.
     * Dispatches FCM push notifications for overdue balances:
     * "Reminder: Your farming expense share of KES {amount} for {Month} is past due"
     */
    suspend fun checkAndDispatchOverdueNotifications(
        asOfDate: LocalDate = LocalDate(2026, 11, 2)
    ): List<AppNotification> {
        val expenses = repositoryProvider.expenseRepository.getAll()
        val tranches = repositoryProvider.trancheRepository.getAll()
        val partners = repositoryProvider.partnerRepository.getAll()

        val summaries = LedgerCalculator.computePartnerSummaries(
            expenses = expenses,
            tranches = tranches,
            partners = partners,
            asOfDate = asOfDate
        )

        val dispatched = mutableListOf<AppNotification>()
        val monthName = asOfDate.month.name.lowercase().replaceFirstChar { it.uppercase() }

        for (summary in summaries) {
            if (summary.complianceStatus == ComplianceStatus.OVERDUE && summary.overdueAmount > 0.0) {
                val partner = partners.firstOrNull { it.id == summary.partnerId }
                val amountFormatted = summary.overdueAmount.toLong()
                val title = "Overdue Farming Expense Share"
                val message =
                    "Reminder: Your farming expense share of KES $amountFormatted for $monthName is past due"

                val notification = AppNotification(
                    id = "notif-${UUID.randomUUID()}",
                    partnerId = summary.partnerId,
                    partnerName = summary.partnerName,
                    title = title,
                    message = message,
                    amountOverdue = summary.overdueAmount,
                    monthYear = "$monthName ${asOfDate.year}",
                    timestampEpochMs = System.currentTimeMillis(),
                    isSent = true
                )

                // Dispatch push notification to partner device token if present
                val token = partner?.fcmToken ?: "token-${partner?.id}"
                val fcmMessageId = pushNotificationService.sendPushNotification(
                    fcmToken = token,
                    title = title,
                    body = message,
                    data = mapOf(
                        "partnerId" to summary.partnerId,
                        "overdueAmount" to summary.overdueAmount.toString(),
                        "monthYear" to "$monthName ${asOfDate.year}"
                    )
                )

                val savedNotif = repositoryProvider.notificationRepository.create(
                    notification.copy(fcmMessageId = fcmMessageId)
                )
                dispatched.add(savedNotif)

                // Broadcast real-time SSE event
                eventBroadcaster.broadcast(ServerEvent.NotificationDispatched(savedNotif))
                logger.info("Dispatched overdue reminder to ${summary.partnerName}: $message")
            }
        }

        return dispatched
    }
}
