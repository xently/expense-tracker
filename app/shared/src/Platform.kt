package co.ke.xently.expense.tracker

import androidx.compose.runtime.Composable

data class Platform(val name: String)

expect fun getPlatform(): Platform

@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)