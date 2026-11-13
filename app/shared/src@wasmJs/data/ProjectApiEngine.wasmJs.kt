package co.ke.xently.expense.tracker.data

import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.js.Js

actual fun defaultHttpClientEngine(): HttpClientEngineFactory<*> = Js