package co.ke.xently.expense.tracker

import androidx.compose.ui.window.ComposeUIViewController
import co.ke.xently.expense.tracker.data.initializeClientStorage

fun ViewController() = run {
    initializeClientStorage()
    ComposeUIViewController { App() }
}
