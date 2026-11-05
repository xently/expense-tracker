package co.ke.xently.expense.tracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import okio.Path.Companion.toPath

private var database: ProjectWorkspaceDatabase? = null
private var preferences: DataStore<Preferences>? = null

actual fun defaultDemoServiceUrl(): String = "http://192.168.100.13:8081"

actual fun initializeClientStorage(platformContext: Any?) {
    if (database != null && preferences != null) return
    val appContext = (platformContext as? Context)?.applicationContext
        ?: error("Android application context is required")
    database = Room.databaseBuilder<ProjectWorkspaceDatabase>(
        context = appContext,
        name = appContext.getDatabasePath("project-ledger.db").absolutePath
    ).setDriver(BundledSQLiteDriver()).build()
    preferences = PreferenceDataStoreFactory.createWithPath(
        produceFile = { appContext.filesDir.resolve("project-ledger.preferences_pb").absolutePath.toPath() }
    )
}

actual fun createWorkspaceCache(): WorkspaceCache =
    RoomWorkspaceCache(requireNotNull(database) { "Client storage was not initialized" }.projectSnapshotDao())

actual fun createThemePreferenceStore(): ThemePreferenceStore =
    ThemePreferenceStore(requireNotNull(preferences) { "Client storage was not initialized" })