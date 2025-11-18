package student.projects.jetpackpam.screens.accounthandler.authorization


import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import java.lang.Exception


/**
 * Small helper to persist biometric-related preferences securely.
 * Uses EncryptedSharedPreferences when available; falls back to regular SharedPreferences on error.
 */
object BiometricPrefs {
    private const val FILE_NAME = "jetpackpam_secure_prefs"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    private const val KEY_LAST_SAVED_EMAIL = "last_saved_email"


    private fun getPrefs(context: Context): SharedPreferences {
        return try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                FILE_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // If EncryptedSharedPreferences fails (older devices / missing dependency) fall back.
            context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE)
        }
    }


    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }


    fun isBiometricEnabled(context: Context): Boolean =
        getPrefs(context).getBoolean(KEY_BIOMETRIC_ENABLED, false)


    fun saveLastEmail(context: Context, email: String) {
        getPrefs(context).edit().putString(KEY_LAST_SAVED_EMAIL, email).apply()
    }


    fun getLastEmail(context: Context): String? =
        getPrefs(context).getString(KEY_LAST_SAVED_EMAIL, null)


    fun clearAll(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
