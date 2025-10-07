package student.projects.jetpackpam.util


import android.content.Context
import android.content.SharedPreferences

object LanguagePrefs {
    private const val PREFS_NAME = "language_prefs"
    private const val KEY_LANGUAGE_CODE = "language_code"
    private const val KEY_LANGUAGE_NAME = "language_name"

    fun saveLanguage(context: Context, name: String, code: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_LANGUAGE_CODE, code)
            .putString(KEY_LANGUAGE_NAME, name)
            .apply()
    }

    fun getSavedLanguageCode(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE_CODE, null)
    }

    fun getSavedLanguageName(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_LANGUAGE_NAME, null)
    }
}
