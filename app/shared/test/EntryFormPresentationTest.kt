package co.ke.xently.expense.tracker

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import co.ke.xently.expense.tracker.ui.EntryFormPresentation
import co.ke.xently.expense.tracker.ui.entryFormPresentation
import kotlin.test.Test
import kotlin.test.assertEquals

class EntryFormPresentationTest {
    @Test
    fun compactPortraitUsesBottomSheetWhileWideOrShortWindowsUseDialogs() {
        assertEquals(
            EntryFormPresentation.MODAL_BOTTOM_SHEET,
            entryFormPresentation(windowSizeClass(400, 800))
        )
        assertEquals(
            EntryFormPresentation.DIALOG,
            entryFormPresentation(windowSizeClass(800, 800))
        )
        assertEquals(
            EntryFormPresentation.DIALOG,
            entryFormPresentation(windowSizeClass(400, 400))
        )
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    private fun windowSizeClass(width: Int, height: Int): WindowSizeClass =
        WindowSizeClass.calculateFromSize(
            DpSize(width.dp, height.dp),
            supportedWidthSizeClasses = setOf(
                WindowWidthSizeClass.Compact,
                WindowWidthSizeClass.Medium,
                WindowWidthSizeClass.Expanded
            ),
            supportedHeightSizeClasses = setOf(
                WindowHeightSizeClass.Compact,
                WindowHeightSizeClass.Medium,
                WindowHeightSizeClass.Expanded
            )
        )
}