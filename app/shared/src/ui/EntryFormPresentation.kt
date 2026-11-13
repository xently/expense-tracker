package co.ke.xently.expense.tracker.ui

import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

internal enum class EntryFormPresentation {
    MODAL_BOTTOM_SHEET,
    DIALOG
}

internal fun entryFormPresentation(windowSizeClass: WindowSizeClass): EntryFormPresentation =
    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact &&
        windowSizeClass.heightSizeClass != WindowHeightSizeClass.Compact
    ) {
        EntryFormPresentation.MODAL_BOTTOM_SHEET
    } else {
        EntryFormPresentation.DIALOG
    }