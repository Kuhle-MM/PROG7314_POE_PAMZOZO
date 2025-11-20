package student.projects.jetpackpam.data.local

import android.content.Context

object SettingsManager {

    private lateinit var repository: SettingsRepository

    fun init(context: Context) {
        val db = SettingsDatabase.getDatabase(context)
        repository = SettingsRepository(db.settingsDao())
    }

    fun repo(): SettingsRepository = repository
}
