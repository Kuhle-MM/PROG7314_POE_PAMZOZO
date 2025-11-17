package student.projects.jetpackpam

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.widget.Toast
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider
import kotlinx.coroutines.launch
import com.google.android.gms.auth.api.identity.Identity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.StartIntentSenderForResult
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import student.projects.jetpackpam.appNavigation.AppNavGraph
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.screens.accounthandler.LoginScreen
import student.projects.jetpackpam.screens.accounthandler.SignUpScreen
import student.projects.jetpackpam.screens.accounthandler.authorization.AuthorizationModelViewModelFactory
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient
import student.projects.jetpackpam.screens.charades.CategorySelectionScreen
import student.projects.jetpackpam.screens.charades.GameOverScreen
import student.projects.jetpackpam.screens.charades.PlayingGameScreen
import student.projects.jetpackpam.screens.charades.StartUpScreen
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // -----------------------------------------
        // 1. MICROPHONE PERMISSIONS
        // -----------------------------------------
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (!isGranted) {
                    Toast.makeText(this, "Microphone permission is required.", Toast.LENGTH_LONG)
                        .show()
                }
            }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        // -----------------------------------------
        // 2. GOOGLE AUTH CLIENT & VIEWMODEL
        // -----------------------------------------
        googleAuthClient = GoogleAuthClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )

        val factory = AuthorizationModelViewModelFactory(googleAuthClient)
        authViewModel =
            ViewModelProvider(this, factory)[AuthorizationModelViewModel::class.java]

        // -----------------------------------------
        // 3. COMPOSE UI
        // -----------------------------------------
        setContent {
            val languageViewModel: LanguageViewModel = viewModel()
            val navController = rememberNavController()
            val context = LocalContext.current
            val scope = rememberCoroutineScope()
            val userData by authViewModel.userData.collectAsStateWithLifecycle()
            // Load saved language
            LaunchedEffect(Unit) {
                languageViewModel.loadLanguage()
            }

            // -----------------------------------------
            // 4. GOOGLE SIGN-IN LAUNCHER
            // -----------------------------------------
            val googleSignInLauncher =
                rememberLauncherForActivityResult(StartIntentSenderForResult()) { result: ActivityResult ->
                    if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                        scope.launch {
                            val signInResult = googleAuthClient.signInWithIntent(result.data!!)
                            authViewModel.handleGoogleSignInResult(signInResult)

                            // Navigate to main after successful login
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
                        composable("signup") {
                            SignUpScreen(
                                navController = navController,
                                authViewModel = authViewModel,
                                googleSignInLauncher = googleSignInLauncher,
                                languageViewModel = languageViewModel
                            )
                        }
                        // MAIN APP (after successful login)
                        composable("main") {
                            MainScreen(
                                googleAuthClient = googleAuthClient,
                                authViewModel = authViewModel,
                                languageViewModel = languageViewModel,
                                //navController = navController
                                rootNavController = navController
                            )
                        }

                        composable("profile") {
                            ProfileScreen(
                                userData = userData,
                                languageViewModel = languageViewModel,
                                onSignOut = {
                                    authViewModel.signOut()
                                    navController.navigate("login") {
                                        popUpTo("main") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("startup") { StartUpScreen(navController) }
                        composable("category") { CategorySelectionScreen(navController) }
                        composable("liveLogs") { LiveLogsScreen(navController) }
                        composable("settings") { SettingsScreen(navController) }
                        composable("playing/{sessionId}/{category}") { backStackEntry ->
                            PlayingGameScreen(
                                navController = navController,
                                sessionId = backStackEntry.arguments?.getString("sessionId") ?: "",
                                category = backStackEntry.arguments?.getString("category") ?: ""
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
}
