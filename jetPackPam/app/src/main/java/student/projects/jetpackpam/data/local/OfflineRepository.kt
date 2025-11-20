package student.projects.jetpackpam.data.local

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow

class OfflineRepository(private val dao: OfflineDao) {

    suspend fun saveOffline(uid: String, key: String, data: Any) {
        val json = Gson().toJson(data)
        dao.insertData(
            OfflineData(
                uid = uid,
                dataKey = key,
                jsonData = json
            )
        )
    }

    suspend fun getUnsynced(uid: String) = dao.getUnsynced(uid)

    suspend fun markSynced(id: Int) = dao.markSynced(id)
}

class SettingsRepository(private val dao: SettingsDao) {

    val settings: Flow<SettingsEntity> = dao.getSettings()

    suspend fun saveFontSize(value: Float) {
        dao.updateFontSize(value)
    }
    suspend fun saveMotorSpeed(value: Float) = dao.updateMotorSpeed(value)
    suspend fun saveMotorPosition(value: String) = dao.updateMotorPosition(value)
    suspend fun saveControllerSize(value: Float) = dao.updateControllerSize(value)
    suspend fun saveLogInterval(value: Int) = dao.updateLogInterval(value)

    // NEW:
    suspend fun saveLanguage(name: String, code: String) = dao.updateLanguage(name, code)
    suspend fun saveTheme(mode: String) = dao.updateTheme(mode)


    suspend fun initializeDefaultSettings() {
        dao.insert(SettingsEntity())
    }
}
