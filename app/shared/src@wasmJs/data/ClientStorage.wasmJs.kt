package co.ke.xently.expense.tracker.data

import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.core.okio.WebLocalStorage
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferencesSerializer

private var preferences: DataStore<Preferences>? = null

actual fun defaultDemoServiceUrl(): String = "http://localhost:8081"

actual fun initializeClientStorage(platformContext: Any?) {
    preferences = DataStoreFactory.create(
        storage = WebLocalStorage(
            serializer = PreferencesSerializer,
            name = "project-ledger.preferences"
        )
    )
}

actual fun createWorkspaceCache(): WorkspaceCache =
    DataStoreWorkspaceCache(requireNotNull(preferences) { "Client storage was not initialized" })

actual fun createThemePreferenceStore(): ThemePreferenceStore =
    ThemePreferenceStore(requireNotNull(preferences) { "Client storage was not initialized" })