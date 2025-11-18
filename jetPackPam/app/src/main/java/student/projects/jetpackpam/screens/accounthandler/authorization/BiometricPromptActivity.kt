package student.projects.jetpackpam.screens.accounthandler.authorization


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity


/**
 * Lightweight activity that hosts BiometricPrompt (uses FragmentActivity).
 * Launch it with startActivityForResult / ActivityResultLauncher.
 *
 * Result:
 *  - RESULT_OK => authentication succeeded
 *  - RESULT_CANCELED => authentication cancelled/failed
 *
 * Usage:
 *  val intent = BiometricPromptActivity.createIntent(context, title = "Unlock", subtitle = "Confirm to continue")
 *  launcher.launch(intent)
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


        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Authenticate"
        val subtitle = intent.getStringExtra(EXTRA_SUBTITLE)


        val biometricManager = BiometricManager.from(this)
        if (biometricManager.canAuthenticate(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            ) != BiometricManager.BIOMETRIC_SUCCESS
        ) {
            // Not available -> cancel
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }


        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                setResult(Activity.RESULT_OK)
                finish()
            }


            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                // treat as cancel / fail
                setResult(Activity.RESULT_CANCELED)
                finish()
            }


            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // keep UX minimal: send CANCEL and finish (app can show message)
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        })


        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply { subtitle?.let { setSubtitle(it) } }
            // allow device credential as fallback (PIN/pattern) if device supports it
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .setNegativeButtonText("Cancel")
            .build()


        prompt.authenticate(promptInfo)
    }
}
