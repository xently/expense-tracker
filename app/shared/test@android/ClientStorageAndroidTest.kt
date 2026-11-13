package co.ke.xently.expense.tracker.data

import kotlin.test.Test
import kotlin.test.assertEquals

class ClientStorageAndroidTest {
    @Test
    fun configuredServiceUrlIsUsed() {
        assertEquals(
            "http://192.168.1.25:8081",
            resolveDemoServiceUrl("http://192.168.1.25:8081")
        )
    }

    @Test
    fun missingServiceUrlUsesAndroidEmulatorDefault() {
        assertEquals("http://10.0.2.2:8081", resolveDemoServiceUrl(null))
    }

    @Test
    fun blankServiceUrlUsesAndroidEmulatorDefault() {
        assertEquals("http://10.0.2.2:8081", resolveDemoServiceUrl("  \t "))
    }
}