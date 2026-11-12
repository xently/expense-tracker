package co.ke.xently.expense.tracker

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable

actual fun getPlatform(): Platform = Platform("Android")

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled, onBack)
}