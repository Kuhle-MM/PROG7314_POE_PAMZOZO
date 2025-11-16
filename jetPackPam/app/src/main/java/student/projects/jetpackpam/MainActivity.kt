package student.projects.jetpackpam

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.auth.api.identity.Identity
import student.projects.jetpackpam.appNavigation.AppNavGraph
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.screens.accounthandler.authorization.AuthorizationModelViewModelFactory
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient
import student.projects.jetpackpam.screens.splash.SplashScreen
import student.projects.jetpackpam.screens.splash.WelcomeScreen
import student.projects.jetpackpam.ui.theme.JetPackPamTheme

class MainActivity : ComponentActivity() {

    private lateinit var authViewModel: AuthorizationModelViewModel
    private lateinit var googleAuthClient: GoogleAuthClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request microphone permissions
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (!isGranted) {
                    Toast.makeText(this, "Microphone permission is required.", Toast.LENGTH_LONG)
                        .show()
                }
            }

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Already granted
            }

            else -> {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        // Initialize GoogleAuthClient
        googleAuthClient = GoogleAuthClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )

        // Initialize ViewModel using factory
        val factory = AuthorizationModelViewModelFactory(googleAuthClient)
        authViewModel =
            ViewModelProvider(this, factory)[AuthorizationModelViewModel::class.java]

        // Compose UI Setup
        setContent {
            val languageViewModel: LanguageViewModel = viewModel()

            // Load the saved language on startup
            LaunchedEffect(Unit) {
                languageViewModel.loadLanguage()
            }

            JetPackPamTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {

                        // SPLASH SCREEN
                        composable("splash") {
                            SplashScreen(navController = navController)
                        }

                        // WELCOME SCREEN
                        composable("welcome") {
                            WelcomeScreen(navController = navController)
                        }


                        // MAIN APP (YOUR EXISTING LOGIC)
                        composable("main") {
                            AppNavGraph(
                                googleAuthClient = googleAuthClient,
                                authViewModel = authViewModel,
                                languageViewModel = languageViewModel
                            )
                        }
                    }
                }
            }
        }
    }
}
