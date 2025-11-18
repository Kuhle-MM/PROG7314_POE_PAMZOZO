package student.projects.jetpackpam.screens.accounthandler.authorization


import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import student.projects.jetpackpam.models.AuthorizationModelViewModel


private const val TAG = "BiometricObserver"


/**
 * Compose helper that observes authViewModel.requestBiometricPrompt flow.
 * When the flow becomes true it will show the BiometricPrompt via BiometricPromptController.
 *
 * - fragmentActivity: the host FragmentActivity required by BiometricPrompt
 * - authViewModel: your AuthorizationModelViewModel
 * - onAuthenticatedRedirect: optional block to run (e.g., navigate to "main")
 *
 * Usage:
 *   BiometricPromptObserver(
 *     fragmentActivity = yourActivity,
 *     authViewModel = authViewModel,
 *     onAuthenticatedRedirect = { navController.navigate("main") }
 *   )
 */
@Composable
fun BiometricPromptObserver(
    fragmentActivity: FragmentActivity,
    authViewModel: AuthorizationModelViewModel,
    onAuthenticatedRedirect: (() -> Unit)? = null
) {
    val shouldShow = authViewModel.requestBiometricPrompt.collectAsStateWithLifecycle(initialValue = false)


    LaunchedEffect(shouldShow.value) {
        if (!shouldShow.value) return@LaunchedEffect


        try {
            val controller = BiometricPromptController(fragmentActivity)


            if (!controller.isBiometricReady()) {
                Log.w(TAG, "Device not biometric-ready")
                authViewModel.clearBiometricPromptRequest()
                return@LaunchedEffect
            }


            controller.authenticate(
                title = "Unlock ${fragmentActivity.title ?: ""}".trim(),
                subtitle = "Authenticate to continue",
                onSuccess = {
                    // inform ViewModel that biometric succeeded
                    authViewModel.onBiometricAuthenticated {
                        // optionally handle navigation on success
                        onAuthenticatedRedirect?.invoke()
                    }
                    authViewModel.clearBiometricPromptRequest()
                },
                onError = { error ->
                    Log.w(TAG, "Biometric error: $error")
                    // expose error to UI via ViewModel (ViewModel uses _errorMessage)
                    // If you want to show a toast from here, you can, but ViewModel will have an error message.
                    authViewModel.clearBiometricPromptRequest()
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show BiometricPrompt", e)
            authViewModel.clearBiometricPromptRequest()
        }
    }
}
