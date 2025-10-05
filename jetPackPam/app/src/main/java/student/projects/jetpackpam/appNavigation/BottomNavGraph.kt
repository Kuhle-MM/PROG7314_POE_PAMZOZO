package student.projects.jetpackpam.appNavigation

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.screens.ChatScreen
import student.projects.jetpackpam.screens.ProfileScreen
import student.projects.jetpackpam.screens.bottomnavscreen.GamesScreen
import student.projects.jetpackpam.screens.bottomnavscreen.HomeScreen
import student.projects.jetpackpam.screens.bottomnavscreen.VideoScreen
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient

@Composable
fun BottomNavGraph(navController: NavHostController, paddingValues: PaddingValues,
                   googleAuthClient: GoogleAuthClient,
                   authViewModel: AuthorizationModelViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userData by authViewModel.userData.collectAsStateWithLifecycle()
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(paddingValues)
    ) {



        composable("home") { HomeScreen(navController = navController) }
        composable("chat") { ChatScreen() }
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
        composable("video") { VideoScreen() }
        composable("games") { GamesScreen() }
    }
}