package co.ke.xently.expense.tracker.util

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual fun getCurrentEpochMs(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()
