package student.projects.jetpackpam.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offline_storage")
data class OfflineData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uid: String,          // Firebase user UID
    val dataKey: String,      // e.g. "pi", "mapping", "profile"
    val jsonData: String,     // Store the object as JSON
    val synced: Boolean = false
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val id: Int = 0,        // ALWAYS 0 → only ONE row
    val fontSize: Float = 20f,          // default
    val languageCode: String = "en",    // future proof
    val themeMode: String = "light"     // future proof
)

@Entity(tableName = "settings")
data class SettingsEntity(

    @PrimaryKey val id: Int = 1,  // Always one row

    val fontSize: Float = 20f,
    val motorSpeed: Float = 50f,

    val motorPosition: String = "Left",
    val controllerSize: Float = 1f,
    val themeMode: String = "Light", // "Light" or "Dark"
    val logInterval: Int = 10,
    // NEW:
    val language: String = "English",
    val languageCode: String = "en"
)