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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import student.projects.jetpackpam.appNavigation.AppNavGraph
import student.projects.jetpackpam.data.local.SettingsManager
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.models.LogsViewModel
import student.projects.jetpackpam.screens.accounthandler.LoginScreen
import student.projects.jetpackpam.screens.accounthandler.SignUpScreen
import student.projects.jetpackpam.screens.accounthandler.authorization.AuthorizationModelViewModelFactory
import student.projects.jetpackpam.screens.accounthandler.authorization.BiometricPromptActivity
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient
import student.projects.jetpackpam.screens.charades.CategorySelectionScreen
import student.projects.jetpackpam.screens.charades.GameOverScreen
import student.projects.jetpackpam.screens.charades.PlayingGameScreen
import student.projects.jetpackpam.screens.charades.StartUpScreen
import student.projects.jetpackpam.screens.controls.Controls
import student.projects.jetpackpam.screens.livelogs.LiveLogsScreen
import student.projects.jetpackpam.screens.mainapp.MainScreen
import student.projects.jetpackpam.screens.settings.SettingsScreen
import student.projects.jetpackpam.screens.sidenavscreen.ProfileScreen
import student.projects.jetpackpam.screens.splash.SplashScreen
import student.projects.jetpackpam.screens.splash.WelcomeScreen
import student.projects.jetpackpam.ui.theme.JetPackPamTheme

class MainActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthorizationModelViewModel
    private lateinit var googleAuthClient: GoogleAuthClient
    private lateinit var biometricLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // -----------------------------------------
        // ⭐ INITIALIZE OFFLINE ROOM + DATASTORE HERE
        // -----------------------------------------
        SettingsManager.init(applicationContext)

        // -----------------------------------------
        // PERMISSIONS (MIC + NOTIFICATIONS)
        // We'll create launchers and request if needed
        // -----------------------------------------
        val micPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
                if (!granted) {
                    Toast.makeText(this, "Microphone permission is required.", Toast.LENGTH_LONG).show()
                }
            }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

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
            ) {
                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        // -----------------------------------------
        // GOOGLE AUTH CLIENT & VIEWMODEL
        // -----------------------------------------
        googleAuthClient = GoogleAuthClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )

        val factory = AuthorizationModelViewModelFactory(googleAuthClient)
        authViewModel = ViewModelProvider(this, factory)[AuthorizationModelViewModel::class.java]

        // -----------------------------------------
        // BIOMETRIC RESULT LAUNCHER
        // (registered after viewModel so we can call it safely)
        // -----------------------------------------
        biometricLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result: ActivityResult ->
            when (result.resultCode) {
                Activity.RESULT_OK -> {
                    try {
                        authViewModel.onBiometricAuthenticated()
                        Toast.makeText(this, "Biometric unlocked", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.w("MainActivity", "Failed to notify ViewModel", e)
                    }
                }
                else -> Toast.makeText(this, "Biometric cancelled or failed", Toast.LENGTH_SHORT).show()
            }
        }

        // -----------------------------------------
        // FIREBASE TOKEN (logs)
        // -----------------------------------------
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) Log.d("FCM", "Token: ${task.result}")
            else Log.e("FCM", "Failed to get token", task.exception)
        }

        // -----------------------------------------
        // COMPOSE UI
        // -----------------------------------------
        setContent {
            // ViewModels for Compose layer
            val languageViewModel: LanguageViewModel = viewModel()
            val logsViewModel: LogsViewModel = viewModel()
            val navController = rememberNavController()
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val userData by authViewModel.userData.collectAsStateWithLifecycle()

            // load language preferences once
            LaunchedEffect(Unit) { languageViewModel.loadLanguage() }

            // -----------------------------------------
            // GOOGLE SIGN-IN LAUNCHER (one-tap)
            // -----------------------------------------
            val googleSignInLauncher =
                rememberLauncherForActivityResult(StartIntentSenderForResult()) { result: ActivityResult ->
                    if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                        scope.launch {
                            val signInResult = googleAuthClient.signInWithIntent(result.data!!)
                            authViewModel.handleGoogleSignInResult(signInResult)

                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    } else {
                        Toast.makeText(context, "Sign-in cancelled", Toast.LENGTH_SHORT).show()
                    }
                }

            JetPackPamTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    // Explicit NavHost copied from your first file so login/sign-up flows
                    // keep access to the googleSignInLauncher and viewModels as before.
                    NavHost(navController = navController, startDestination = "splash") {

                        composable("splash") { SplashScreen(navController) }

                        composable("welcome") { WelcomeScreen(navController) }

                        composable("login") {
                            LoginScreen(
                                navController = navController,
                                googleAuthClient = googleAuthClient,
                                authViewModel = authViewModel,
                                languageViewModel = languageViewModel,
                                googleSignInLauncher = googleSignInLauncher
                            )
                        }

                        composable("signUp") {
                            SignUpScreen(
                                navController = navController,
                                authViewModel = authViewModel,
                                googleSignInLauncher = googleSignInLauncher,
                                languageViewModel = languageViewModel
                            )
                        }

                        // 🌟 MAIN APP
                        composable("main") {
                            MainScreen(
                                googleAuthClient = googleAuthClient,
                                authViewModel = authViewModel,
                                languageViewModel = languageViewModel,
                                rootNavController = navController
                            )
                        }

                        composable("profile") {
                            ProfileScreen(
                                userData = userData,
                                languageViewModel = languageViewModel
                            )
                        }

                        composable("controls") { Controls(navController) }

                        // ------------------ GAME SCREENS ------------------
                        composable("startup") { StartUpScreen(navController) }
                        composable("category") { CategorySelectionScreen(navController) }
                        composable("liveLogs") { LiveLogsScreen(navController, logsViewModel) }

                        composable("settings") { SettingsScreen(navController, logsViewModel) }

                        composable("playing/{sessionId}/{category") { backStackEntry ->
                            PlayingGameScreen(
                                navController = navController,
                                sessionId = backStackEntry.arguments?.getString("sessionId")!!,
                                category = backStackEntry.arguments?.getString("category")!!
                            )
                        }

                        composable("gameover") {
                            GameOverScreen(navController, null, null)
                        }
                    }
                }
            }
        }
    }

    /**
     * Launches the biometric host activity to authenticate.
     * The activity will return a result consumed by biometricLauncher.
     */
    fun launchBiometricPrompt(
        title: String = "Unlock App",
        subtitle: String = "Authenticate to continue"
    ) {
        try {
            val intent = BiometricPromptActivity.createIntent(this, title, subtitle)
            if (intent.resolveActivity(packageManager) != null) {
                biometricLauncher.launch(intent)
            } else {
                Toast.makeText(this, "Biometric prompt not available", Toast.LENGTH_SHORT).show()
                Log.w("MainActivity", "BiometricPromptActivity not resolved")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to launch biometric activity", e)
            Toast.makeText(this, "Unable to start biometric prompt", Toast.LENGTH_SHORT).show()
        }
    }
}
