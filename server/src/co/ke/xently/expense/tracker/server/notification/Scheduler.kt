package co.ke.xently.expense.tracker.server.notification

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.slf4j.LoggerFactory
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class BackgroundScheduler(
    private val overdueNotificationManager: OverdueNotificationManager,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob()),
    private val interval: Duration = 12.hours
) {
    private val logger = LoggerFactory.getLogger(BackgroundScheduler::class.java)
    private var job: Job? = null

    fun start() {
        job?.cancel()
        job = scope.launch {
            logger.info("Starting background overdue notification scheduler (interval: $interval)")
            while (isActive) {
                try {
                    val now =
                        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
                    logger.info("Running scheduled overdue check for date: $now")
                    overdueNotificationManager.checkAndDispatchOverdueNotifications(now)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.error("Error during scheduled overdue check: ${e.message}", e)
                }
                delay(interval)
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
        logger.info("Stopped background overdue notification scheduler")
    }
}
