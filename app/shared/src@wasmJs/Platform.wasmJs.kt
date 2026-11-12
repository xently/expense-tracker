package co.ke.xently.expense.tracker

import androidx.compose.runtime.Composable

actual fun getPlatform(): Platform = Platform("Web")

@Composable
actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) = Unit