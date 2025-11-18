package student.projects.jetpackpam

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.messaging.FirebaseMessaging
import student.projects.jetpackpam.ui.theme.JetPackPamTheme
import student.projects.jetpackpam.screens.accounthandler.authorization.BiometricPromptActivity
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient
import student.projects.jetpackpam.screens.accounthandler.authorization.AuthorizationModelViewModelFactory
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.models.LanguageViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import student.projects.jetpackpam.appNavigation.AppNavGraph

class MainActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthorizationModelViewModel
    private lateinit var googleAuthClient: GoogleAuthClient

    // Launcher used to start the transient BiometricPromptActivity
    private lateinit var biometricLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ------------------------------
        // Register result launcher(s)
        // ------------------------------
        biometricLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    // Biometric activity indicated success — inform the shared ViewModel.
                    try {
                        authViewModel.onBiometricAuthenticated()
                        Toast.makeText(this, "Biometric unlocked", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.w("MainActivity", "Failed to notify ViewModel after biometric OK", e)
                    }
                }
                else -> {
                    Toast.makeText(this, "Biometric cancelled or failed", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // ------------------------------
        // Permissions (microphone + notifications on Android 13+)
        // ------------------------------
        val micPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (!granted) {
                    Toast.makeText(this, "Microphone permission is required.", Toast.LENGTH_LONG).show()
                }
            }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val notifPermissionLauncher =
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                    if (!granted) {
                        Toast.makeText(this, "Notification permission required.", Toast.LENGTH_LONG).show()
                    }
                }

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        // -----------------------------------------
        // GOOGLE AUTH CLIENT & VIEWMODEL (non-compose)
        // -----------------------------------------
        googleAuthClient = GoogleAuthClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )

        val factory = AuthorizationModelViewModelFactory(googleAuthClient)
        authViewModel = ViewModelProvider(this, factory)[AuthorizationModelViewModel::class.java]

        // -----------------------------------------
        // FIREBASE PUSH TOKEN (optional logging)
        // -----------------------------------------
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) android.util.Log.d("FCM", "Token: ${task.result}")
            else android.util.Log.e("FCM", "Failed to get token", task.exception)
        }

        // -----------------------------------------
        // COMPOSE UI LAYER
        // -----------------------------------------
        setContent {
            val languageViewModel: LanguageViewModel = viewModel()
            val navController = rememberNavController()
            val scope = rememberCoroutineScope()

            // load language once
            LaunchedEffect(Unit) { languageViewModel.loadLanguage() }

            JetPackPamTheme {
                AppNavGraph(
                    navController = navController,
                    googleAuthClient = googleAuthClient,
                    authViewModel = authViewModel,
                    languageViewModel = languageViewModel
                )
            }
        }
    }

    /**
     * Public helper to launch the BiometricPromptActivity.
     *
     * Usage from Compose:
     *   val ctx = LocalContext.current
     *   (ctx as? MainActivity)?.launchBiometricPrompt("Unlock P.A.M", "Authenticate to continue")
     *
     * This method checks resolveActivity() to avoid ActivityNotFoundException.
     */
    fun launchBiometricPrompt(title: String = "Unlock App", subtitle: String = "Authenticate to continue") {
        try {
            val intent = BiometricPromptActivity.createIntent(this, title, subtitle)

            if (intent.resolveActivity(packageManager) != null) {
                biometricLauncher.launch(intent)
            } else {
                // Defensive: activity missing — inform user and log
                Toast.makeText(this, "Biometric prompt not available", Toast.LENGTH_SHORT).show()
                Log.w("MainActivity", "BiometricPromptActivity not resolved by package manager")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to launch biometric activity", e)
            Toast.makeText(this, "Unable to start biometric prompt", Toast.LENGTH_SHORT).show()
        }
    }
}
