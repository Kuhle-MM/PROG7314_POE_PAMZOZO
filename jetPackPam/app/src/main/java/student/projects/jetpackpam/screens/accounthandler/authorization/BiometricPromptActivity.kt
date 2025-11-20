// package student.projects.jetpackpam.screens.accounthandler.authorization

package student.projects.jetpackpam.screens.accounthandler.authorization

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Hosts the BiometricPrompt. Returns RESULT_OK on authentication success,
 * RESULT_CANCELED otherwise.
 *
 * This version requests BIOMETRIC_STRONG (fingerprint) only and removes activity
 * enter/exit animations and uses a transparent background to avoid the blank page flicker.
 */
class BiometricPromptActivity : FragmentActivity() {

    companion object {
        private const val EXTRA_TITLE = "extra_title"
        private const val EXTRA_SUBTITLE = "extra_subtitle"

        fun createIntent(context: Context, title: String, subtitle: String? = null): Intent {
            return Intent(context, BiometricPromptActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                subtitle?.let { putExtra(EXTRA_SUBTITLE, it) }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Immediately remove enter animation so the system won't flash the activity
        overridePendingTransition(0, 0)

        // Make window background transparent to keep underlying UI visible while biometric sheet shows
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Authenticate"
        val subtitle = intent.getStringExtra(EXTRA_SUBTITLE)

        val biometricManager = BiometricManager.from(this)
        // Request fingerprint (BIOMETRIC_STRONG) only
        if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            != BiometricManager.BIOMETRIC_SUCCESS
        ) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            // ensure no exit animation
            overridePendingTransition(0, 0)
            return
        }

        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                setResult(Activity.RESULT_OK)
                finish()
                overridePendingTransition(0, 0)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                setResult(Activity.RESULT_CANCELED)
                finish()
                overridePendingTransition(0, 0)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                setResult(Activity.RESULT_CANCELED)
                finish()
                overridePendingTransition(0, 0)
            }
        })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply { subtitle?.let { setSubtitle(it) } }
            // fingerprint-only
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
            // negative button (some API levels require it)
            .setNegativeButtonText("Cancel")
            .build()

        prompt.authenticate(promptInfo)
    }
}