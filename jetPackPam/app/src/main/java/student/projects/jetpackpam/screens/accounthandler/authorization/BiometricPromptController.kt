package student.projects.jetpackpam.screens.accounthandler.authorization


import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor


/**
 * Small controller that shows the system BiometricPrompt for a FragmentActivity.
 *
 * Usage:
 *   val controller = BiometricPromptController(fragmentActivity)
 *   controller.authenticate(
 *       title = "Unlock App",
 *       subtitle = "Confirm to continue",
 *       onSuccess = { /* success */ },
 *       onError = { err -> /* error */ }
 *   )
 */
class BiometricPromptController(private val activity: FragmentActivity) {


    private val executor: Executor by lazy { ContextCompat.getMainExecutor(activity) }


    fun isBiometricReady(): Boolean {
        val manager = BiometricManager.from(activity)
        return manager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }


    fun authenticate(
        title: String = "Unlock with Biometrics",
        subtitle: String? = null,
        negativeButtonText: String = "Cancel",
        allowedAuthenticators: Int = BiometricManager.Authenticators.BIOMETRIC_STRONG
                or BiometricManager.Authenticators.DEVICE_CREDENTIAL,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .apply { subtitle?.let { setSubtitle(it) } }
            .setAllowedAuthenticators(allowedAuthenticators)
            // If you want a negative button text (for BIOMETRIC_STRONG + DEVICE_CREDENTIAL combos),
            // you can add setNegativeButtonText for backward compat; with setAllowedAuthenticators
            // device credential may be used instead of negative button on some devices.
            .setNegativeButtonText(negativeButtonText)
            .build()


        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }


                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }


                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Authentication failed")
                }
            }
        )


        biometricPrompt.authenticate(promptInfo)
    }
}
