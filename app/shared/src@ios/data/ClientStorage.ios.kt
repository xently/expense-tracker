package co.ke.xently.expense.tracker.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

private var database: ProjectWorkspaceDatabase? = null
private var preferences: DataStore<Preferences>? = null

actual fun defaultDemoServiceUrl(): String = "http://127.0.0.1:8081"

@OptIn(ExperimentalForeignApi::class)
actual fun initializeClientStorage(platformContext: Any?) {
    if (database != null && preferences != null) return
    val documentDirectory = NSFileManager.defaultManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null
    )
    val directoryPath = requireNotNull(documentDirectory?.path)
    database =
        Room.databaseBuilder<ProjectWorkspaceDatabase>(name = "$directoryPath/project-ledger.db")
            .setDriver(BundledSQLiteDriver())
            .build()
    preferences = PreferenceDataStoreFactory.createWithPath(
        produceFile = { "$directoryPath/project-ledger.preferences_pb".toPath() }
    )
}

actual fun createWorkspaceCache(): WorkspaceCache =
    RoomWorkspaceCache(requireNotNull(database) { "Client storage was not initialized" }.projectSnapshotDao())

actual fun createThemePreferenceStore(): ThemePreferenceStore =
    ThemePreferenceStore(requireNotNull(preferences) { "Client storage was not initialized" })