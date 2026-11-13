package co.ke.xently.expense.tracker.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.room3.ConstructedBy
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import co.ke.xently.expense.tracker.model.Expense
import co.ke.xently.expense.tracker.model.Project
import co.ke.xently.expense.tracker.model.ProjectUser
import co.ke.xently.expense.tracker.model.TranchePayment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CachedProjectMember(
    val membership: co.ke.xently.expense.tracker.model.ProjectMembership,
    val user: ProjectUser
)

@Serializable
data class CachedProjectSnapshot(
    val project: Project,
    val members: List<CachedProjectMember>,
    val expenses: List<Expense>,
    val tranches: List<TranchePayment>
)

@Entity(tableName = "project_snapshots")
data class ProjectSnapshotEntity(
    @PrimaryKey val projectId: String,
    val snapshotJson: String
)

@Dao
interface ProjectSnapshotDao {
    @Query("SELECT * FROM project_snapshots WHERE projectId = :projectId")
    suspend fun get(projectId: String): ProjectSnapshotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(snapshot: ProjectSnapshotEntity)

    @Query("DELETE FROM project_snapshots WHERE projectId = :projectId")
    suspend fun delete(projectId: String)
}

@Database(entities = [ProjectSnapshotEntity::class], version = 1, exportSchema = false)
@ConstructedBy(ProjectWorkspaceDatabaseConstructor::class)
abstract class ProjectWorkspaceDatabase : RoomDatabase() {
    abstract fun projectSnapshotDao(): ProjectSnapshotDao
}

expect object ProjectWorkspaceDatabaseConstructor :
    RoomDatabaseConstructor<ProjectWorkspaceDatabase>

interface WorkspaceCache {
    suspend fun read(projectId: String): CachedProjectSnapshot?
    suspend fun write(snapshot: CachedProjectSnapshot)
    suspend fun clear(projectId: String)
}

class RoomWorkspaceCache(
    private val dao: ProjectSnapshotDao,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : WorkspaceCache {
    override suspend fun read(projectId: String): CachedProjectSnapshot? =
        dao.get(projectId)?.let { json.decodeFromString<CachedProjectSnapshot>(it.snapshotJson) }

    override suspend fun write(snapshot: CachedProjectSnapshot) {
        dao.save(ProjectSnapshotEntity(snapshot.project.id, json.encodeToString(snapshot)))
    }

    override suspend fun clear(projectId: String) = dao.delete(projectId)
}

class DataStoreWorkspaceCache(
    private val dataStore: DataStore<Preferences>,
    private val json: Json = Json { ignoreUnknownKeys = true }
) : WorkspaceCache {
    override suspend fun read(projectId: String): CachedProjectSnapshot? {
        val key = stringPreferencesKey("project.snapshot.$projectId")
        return dataStore.data.first()[key]?.let { json.decodeFromString<CachedProjectSnapshot>(it) }
    }

    override suspend fun write(snapshot: CachedProjectSnapshot) {
        val key = stringPreferencesKey("project.snapshot.${snapshot.project.id}")
        dataStore.edit { it[key] = json.encodeToString(snapshot) }
    }

    override suspend fun clear(projectId: String) {
        val key = stringPreferencesKey("project.snapshot.$projectId")
        dataStore.edit { it.remove(key) }
    }
}

class InMemoryWorkspaceCache(initialSnapshots: List<CachedProjectSnapshot> = emptyList()) :
    WorkspaceCache {
    private val snapshots = initialSnapshots.associateBy { it.project.id }.toMutableMap()

    override suspend fun read(projectId: String): CachedProjectSnapshot? = snapshots[projectId]
    override suspend fun write(snapshot: CachedProjectSnapshot) {
        snapshots[snapshot.project.id] = snapshot
    }

    override suspend fun clear(projectId: String) {
        snapshots.remove(projectId)
    }
}

enum class SavedThemePreference {
    SYSTEM,
    LIGHT,
    DARK
}

class ThemePreferenceStore(private val dataStore: DataStore<Preferences>) {
    private val themeKey = stringPreferencesKey("theme.preference")

    val preference: Flow<SavedThemePreference> = dataStore.data
        .map { preferences ->
            preferences[themeKey]?.let { value ->
                SavedThemePreference.entries.firstOrNull { it.name == value }
                    ?: SavedThemePreference.SYSTEM
            } ?: SavedThemePreference.SYSTEM
        }
        .catch { emit(SavedThemePreference.SYSTEM) }

    suspend fun save(preference: SavedThemePreference) {
        dataStore.edit { it[themeKey] = preference.name }
    }
}

expect fun initializeClientStorage(platformContext: Any? = null)
expect fun createWorkspaceCache(): WorkspaceCache
expect fun createThemePreferenceStore(): ThemePreferenceStore
