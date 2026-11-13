package co.ke.xently.expense.tracker.data

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.cio.CIO

actual fun defaultHttpClientEngine(): HttpClientEngineFactory<*> = CIO