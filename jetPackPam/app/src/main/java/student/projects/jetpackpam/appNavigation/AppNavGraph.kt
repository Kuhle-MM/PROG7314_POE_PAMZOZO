package student.projects.jetpackpam.appNavigation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import student.projects.jetpackpam.localization.LocalLanguageViewModel
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.models.LogsViewModel
import student.projects.jetpackpam.screens.sidenavscreen.ProfileScreen
import student.projects.jetpackpam.screens.accounthandler.LoginScreen
import student.projects.jetpackpam.screens.accounthandler.SignUpScreen
import student.projects.jetpackpam.screens.mainapp.MainScreen
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient
import student.projects.jetpackpam.screens.charades.CategorySelectionScreen
import student.projects.jetpackpam.screens.charades.GameOverScreen
import student.projects.jetpackpam.screens.charades.PlayingGameScreen
import student.projects.jetpackpam.screens.charades.StartUpScreen
import student.projects.jetpackpam.screens.livelogs.LiveLogsScreen
import student.projects.jetpackpam.screens.splash.SplashScreen
import student.projects.jetpackpam.screens.splash.WelcomeScreen

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    googleAuthClient: GoogleAuthClient,
    authViewModel: AuthorizationModelViewModel,
    languageViewModel: LanguageViewModel
) {
    val userData by authViewModel.userData.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val logsViewModel: LogsViewModel = viewModel()

    // Google One Tap launcher (handles StartIntentSenderForResult)
    val googleSignInLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                scope.launch {
                    try {
                        // Convert returned intent to a SignInResult and let ViewModel handle firebase sign-in
                        val signInResult = googleAuthClient.signInWithIntent(result.data!!)
                        authViewModel.handleGoogleSignInResult(signInResult)

                        // Safe navigation after successful sign in
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    } catch (e: Exception) {
                        // Surface the error and let ViewModel record it if needed
                        authViewModel.handleGoogleSignInError(e.localizedMessage)
                        Toast.makeText(context, "Google sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(context, "Sign-in cancelled", Toast.LENGTH_SHORT).show()
            }
        }

    CompositionLocalProvider(LocalLanguageViewModel provides languageViewModel) {
        // app starts at splash -> welcome -> login/signup -> main
        NavHost(
            navController = navController,
            startDestination = "splash"
        ) {
            // Splash & Welcome
            composable("splash") {
                SplashScreen(navController = navController)
            }

            composable("welcome") {
                WelcomeScreen(navController = navController)
            }

            // LOGIN
            composable("login") {
                LoginScreen(
                    navController = navController,
                    googleAuthClient = googleAuthClient,
                    authViewModel = authViewModel,
                    languageViewModel = languageViewModel,
                    googleSignInLauncher = googleSignInLauncher
                )
            }

            // SIGNUP (route name matches LoginScreen's navigate("signUp"))
            composable("signUp") {
                SignUpScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    googleSignInLauncher = googleSignInLauncher,
                    languageViewModel = languageViewModel
                )
            }

            // MAIN APP
            composable("main") {
                MainScreen(
                    googleAuthClient = googleAuthClient,
                    authViewModel = authViewModel,
                    languageViewModel = languageViewModel,
                    rootNavController = navController
                )
            }

            // PROFILE (uses a coroutine for sign-out to avoid calling suspend directly)
            composable("profile") {
                ProfileScreen(
                    userData = userData,
                    languageViewModel = languageViewModel
                )
            }

            // Charades / Game / Live logs routes
            composable("startup") { StartUpScreen(navController) }
            composable("category") { CategorySelectionScreen(navController) }
            composable("liveLogs") { LiveLogsScreen(navController, logsViewModel) }

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
