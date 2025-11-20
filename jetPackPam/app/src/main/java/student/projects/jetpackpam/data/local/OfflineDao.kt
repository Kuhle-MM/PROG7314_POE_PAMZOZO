package student.projects.jetpackpam.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
abstract class OfflineDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertData(data: OfflineData)

    @Query("SELECT * FROM offline_storage WHERE uid = :uid AND synced = 0")
    abstract suspend fun getUnsynced(uid: String): List<OfflineData>

    @Query("UPDATE offline_storage SET synced = 1 WHERE id = :id")
    abstract suspend fun markSynced(id: Int)
}

@Dao
abstract class UserPreferencesDao {
    @Query("SELECT * FROM user_preferences WHERE id = 0")
    abstract suspend fun getPreferences(): UserPreferencesEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun savePreferences(prefs: UserPreferencesEntity)
}

@Dao
interface SettingsDao {

    @Query("UPDATE settings SET language = :name, languageCode = :code WHERE id = 0")
    suspend fun updateLanguage(name: String, code: String)
    @Query("UPDATE settings SET themeMode = :mode WHERE id = 0")
    suspend fun updateTheme(mode: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: SettingsEntity)

    @Query("SELECT * FROM settings WHERE id = 1 LIMIT 1")
    fun getSettings(): Flow<SettingsEntity>

    // ----- Individual updates -----
    @Query("UPDATE settings SET fontSize = :value WHERE id = 1")
    suspend fun updateFontSize(value: Float)

    @Query("UPDATE settings SET motorSpeed = :value WHERE id = 1")
    suspend fun updateMotorSpeed(value: Float)

    @Query("UPDATE settings SET motorPosition = :value WHERE id = 1")
    suspend fun updateMotorPosition(value: String)

    @Query("UPDATE settings SET controllerSize = :value WHERE id = 1")
    suspend fun updateControllerSize(value: Float)

    @Query("UPDATE settings SET logInterval = :value WHERE id = 1")
    suspend fun updateLogInterval(value: Int)

    // ----- NEW: bulk update -----
    @Query("""
        UPDATE settings SET 
            fontSize = :fontSize,
            motorSpeed = :motorSpeed,
            motorPosition = :motorPosition,
            controllerSize = :controllerSize,
            logInterval = :logInterval
        WHERE id = 1
    """)
    suspend fun updateAll(
        fontSize: Float,
        motorSpeed: Float,
        motorPosition: String,
        controllerSize: Float,
        logInterval: Int
    )
}