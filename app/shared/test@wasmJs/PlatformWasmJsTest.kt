package co.ke.xently.expense.tracker

import kotlin.test.Test
import kotlin.test.assertTrue

class PlatformWasmJsTest {
    @Test
    fun `platform name is reported`() {
        assertTrue(getPlatform().name.isNotBlank())
    }
}
