package co.ke.xently.expense.tracker

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import co.ke.xently.expense.tracker.data.initializeClientStorage

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    initializeClientStorage()
    ComposeViewport {
        App()
    }
}
