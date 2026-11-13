@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package co.ke.xently.expense.tracker.data

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ProjectApiWasmJsTest {
    @Test
    fun profileRequestUsesBrowserFetch() = runTest {
        installFetchMock()
        try {
            val profile = KtorProjectApi().getProfile("http://localhost:8081", "demo-token")

            assertEquals("browser-user", profile.id)
            assertEquals("http://localhost:8081/api/v1/me/profile", lastFetchUrl())
            assertEquals("Bearer demo-token", lastFetchAuthorization())
        } finally {
            restoreFetch()
        }
    }
}

private fun installFetchMock(): Unit = js("""{
    globalThis.__projectApiOriginalFetch = globalThis.fetch;
    globalThis.fetch = async (input, init = {}) => {
        globalThis.__projectApiFetchUrl = typeof input === "string" ? input : input.url;
        globalThis.__projectApiFetchAuthorization = new Headers(init.headers).get("Authorization");
        return new Response(
            JSON.stringify({
                id: "browser-user",
                username: "browser-user",
                displayName: "Browser User",
                email: "browser@example.test"
            }),
            { status: 200, headers: { "Content-Type": "application/json" } }
        );
    };
}""")

private fun lastFetchUrl(): String = js("globalThis.__projectApiFetchUrl")

private fun lastFetchAuthorization(): String = js("globalThis.__projectApiFetchAuthorization")

private fun restoreFetch(): Unit = js("{ globalThis.fetch = globalThis.__projectApiOriginalFetch; }")