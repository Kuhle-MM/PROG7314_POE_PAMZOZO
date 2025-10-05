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
import kotlinx.coroutines.launch
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.screens.ProfileScreen
import student.projects.jetpackpam.screens.accounthandler.LoginScreen
import student.projects.jetpackpam.screens.accounthandler.SignUpScreen
import student.projects.jetpackpam.screens.mainapp.MainScreen
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient

private const val TAG = "AppNavGraph"

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    googleAuthClient: GoogleAuthClient,
    authViewModel: AuthorizationModelViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // --- Collect user data from ViewModel (null means not signed in) ---
    val userData by authViewModel.userData.collectAsStateWithLifecycle()

    // --- Google One Tap Launcher setup ---
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult(),
        onResult = { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                coroutineScope.launch {
                    try {
                        val signInResult =
                            googleAuthClient.signInWithIntent(result.data ?: return@launch)
                        authViewModel.handleGoogleSignInResult(signInResult)
                    } catch (e: Exception) {
                        Log.e(TAG, "Error processing Google One Tap result", e)
                        authViewModel.handleGoogleSignInError(e.localizedMessage)
                        Toast.makeText(context, "Google sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Log.d(TAG, "Google One Tap cancelled or failed")
                Toast.makeText(context, "Google sign-in cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    )

    // --- Navigation control based on authentication state ---
    LaunchedEffect(userData) {
        // If user logs in successfully → move to MainScreen
        if (userData != null && navController.currentDestination?.route != "main") {
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // --- Navigation graph definition ---
    NavHost(
        navController = navController,
        startDestination = if (userData == null) "login" else "main"
    ) {

        // --- Login screen (Email + Google SSO) ---
        composable("login") {
            LoginScreen(
                navController = navController,
                authViewModel = authViewModel,
                googleAuthClient = googleAuthClient,
                googleSignInLauncher = googleSignInLauncher
            )
        }

        // --- Sign Up screen ---
        composable("signUp") {
            SignUpScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // --- Main screen (post-login home) ---
        composable("main") {
            MainScreen(
                authViewModel = authViewModel,
                rootNavController = navController,
                googleAuthClient = googleAuthClient
            )

        }



        // --- Profile screen (with manual sign-out) ---
        composable("profile") {
            ProfileScreen(
                userData = userData,
                onSignOut = {
                    try {
                        authViewModel.signOut()
                        Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error signing out", e)
                        Toast.makeText(context, "Sign-out failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }
}
