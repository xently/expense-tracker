package co.ke.xently.expense.tracker.util

@OptIn(ExperimentalWasmJsInterop::class)
private fun dateNow(): Double = js("Date.now()")

actual fun getCurrentEpochMs(): Long = dateNow().toLong()
