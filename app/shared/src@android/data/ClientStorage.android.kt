package co.ke.xently.expense.tracker.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import okio.Path.Companion.toPath

private const val DEFAULT_DEMO_SERVICE_URL = "http://10.0.2.2:8081"
private const val DEMO_SERVICE_URL_RESOURCE_NAME = "demo_service_url"

private var database: ProjectWorkspaceDatabase? = null
private var preferences: DataStore<Preferences>? = null
private var demoServiceUrl: String = DEFAULT_DEMO_SERVICE_URL

actual fun defaultDemoServiceUrl(): String = demoServiceUrl

internal fun resolveDemoServiceUrl(configuredServiceUrl: String?): String =
    configuredServiceUrl?.trim()?.takeIf { it.isNotEmpty() } ?: DEFAULT_DEMO_SERVICE_URL

actual fun initializeClientStorage(platformContext: Any?) {
    if (database != null && preferences != null) return
    val appContext = (platformContext as? Context)?.applicationContext
        ?: error("Android application context is required")
    val demoServiceUrlResourceId = appContext.resources.getIdentifier(
        DEMO_SERVICE_URL_RESOURCE_NAME,
        "string",
        appContext.packageName
    )
    demoServiceUrl = resolveDemoServiceUrl(
        demoServiceUrlResourceId.takeIf { it != 0 }?.let { appContext.getString(it) }
    )
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