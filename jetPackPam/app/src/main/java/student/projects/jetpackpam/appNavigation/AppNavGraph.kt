package student.projects.jetpackpam.appNavigation

import android.app.Activity
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

    val googleSignInLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                scope.launch {
                    val signInResult = googleAuthClient
                        .signInWithIntent(result.data!!)
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
                    googleSignInLauncher = googleSignInLauncher,
                    languageViewModel = languageViewModel
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
