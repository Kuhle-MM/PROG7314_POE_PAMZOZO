package student.projects.jetpackpam.appNavigation

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import student.projects.jetpackpam.localization.LocalLanguageViewModel
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.screens.ProfileScreen
import student.projects.jetpackpam.screens.accounthandler.LoginScreen
import student.projects.jetpackpam.screens.accounthandler.SignUpScreen
import student.projects.jetpackpam.screens.mainapp.MainScreen
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient
import student.projects.jetpackpam.screens.charades.*

private const val TAG = "AppNavGraph"

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    googleAuthClient: GoogleAuthClient,
    authViewModel: AuthorizationModelViewModel,
    languageViewModel: LanguageViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val userData by authViewModel.userData.collectAsStateWithLifecycle()

    // --- Google One Tap Launcher ---
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            coroutineScope.launch {
                try {
                    val signInResult = googleAuthClient.signInWithIntent(result.data ?: return@launch)
                    authViewModel.handleGoogleSignInResult(signInResult)

                    // Navigate safely to main
                    navController.navigate("main") {
                        launchSingleTop = true
                        navController.graph.startDestinationRoute?.let { start ->
                            popUpTo(start) { inclusive = true }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Google One Tap error", e)
                    Toast.makeText(context, "Google sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
                    authViewModel.handleGoogleSignInError(e.localizedMessage)
                }
            }
        } else {
            Toast.makeText(context, "Google sign-in cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    // --- Navigation Graph ---
    CompositionLocalProvider(LocalLanguageViewModel provides languageViewModel) {
        NavHost(
            navController = navController,
            startDestination = if (userData == null) "login" else "main"
        ) {
            composable("login") {
                LoginScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    googleAuthClient = googleAuthClient,
                    googleSignInLauncher = googleSignInLauncher
                )
            }
            composable("signUp") {
                SignUpScreen(
                    navController = navController,
                    authViewModel = authViewModel,
                    googleSignInLauncher = googleSignInLauncher
                )
            }
            composable("main") {
                MainScreen(
                    authViewModel = authViewModel,
                    rootNavController = navController,
                    googleAuthClient = googleAuthClient,
                    languageViewModel = languageViewModel
                )
            }
            composable("profile") {
                ProfileScreen(
                    userData = userData,
                    uiTexts = languageViewModel.uiTexts
                )
            }
            composable("startup") { StartUpScreen(navController) }
            composable("category") { CategorySelectionScreen(navController) }
            composable("playing/{sessionId}/{category}") { backStackEntry ->
                val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
                val category = backStackEntry.arguments?.getString("category") ?: ""
                PlayingGameScreen(navController, sessionId, category)
            }
            composable(
                route = "gameover?correct={correct}&skipped={skipped}",
                arguments = listOf(
                    navArgument("correct") { defaultValue = ""; type = androidx.navigation.NavType.StringType },
                    navArgument("skipped") { defaultValue = ""; type = androidx.navigation.NavType.StringType }
                )
            ) { backStackEntry ->
                val correct = backStackEntry.arguments?.getString("correct")
                val skipped = backStackEntry.arguments?.getString("skipped")
                GameOverScreen(navController, correct, skipped)
            }
        }
    }
}
