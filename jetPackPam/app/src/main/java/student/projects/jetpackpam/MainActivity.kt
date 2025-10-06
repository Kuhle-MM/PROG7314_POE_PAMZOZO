package student.projects.jetpackpam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.identity.Identity
import student.projects.jetpackpam.appNavigation.AppNavGraph
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.screens.accounthandler.authorization.AuthorizationModelViewModelFactory
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import student.projects.jetpackpam.screens.accounthandler.LoginScreen
import student.projects.jetpackpam.screens.charades.CategorySelectionScreen
import student.projects.jetpackpam.screens.charades.CharadesNavGraph
import student.projects.jetpackpam.screens.charades.GameOverScreen
import student.projects.jetpackpam.screens.charades.PlayingGameScreen
import student.projects.jetpackpam.screens.charades.StartUpScreen
import student.projects.jetpackpam.screens.firsttimecustom.FontSelectionScreen
import student.projects.jetpackpam.screens.firsttimecustom.LanguageSelectionScreen
import student.projects.jetpackpam.screens.firsttimecustom.PamThemeSelectionScreen
import student.projects.jetpackpam.screens.firsttimecustom.PersonalitySelectionScreen
import student.projects.jetpackpam.screens.firsttimecustom.PersonalitySelectionScreen2
import student.projects.jetpackpam.screens.mainapp.MainScreen
import student.projects.jetpackpam.ui.theme.JetPackPamTheme
import student.projects.jetpackpam.ui.theme.Surface

class MainActivity : ComponentActivity() {

    // --- Dependencies used throughout the app ---
    private lateinit var authViewModel: AuthorizationModelViewModel
    private lateinit var googleAuthClient: GoogleAuthClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Initialize the GoogleAuthClient ---
        // This manages all Google One Tap / Sign-In operations
        googleAuthClient = GoogleAuthClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )

        // --- ViewModel setup using a Factory ---
        // The factory injects our googleAuthClient into the ViewModel
        val factory = AuthorizationModelViewModelFactory(googleAuthClient)
        authViewModel = ViewModelProvider(this, factory)[AuthorizationModelViewModel::class.java]

        // --- Compose UI setup ---
        setContent {
            JetPackPamTheme(){

                //MainScreen()
                //LoginScreen()
               // LanguageSelectionScreen()
                //PersonalitySelectionScreen()
                //PersonalitySelectionScreen2()
                //FontSelectionScreen()
                //PamThemeSelectionScreen()

                //Gaming section
//                Surface {
//                    val navController = rememberNavController()
//                    NavHost(
//                        navController = navController,
//                        startDestination = "startup"
//                    ) {
//                        composable("startup") { StartUpScreen(navController) }
//                        composable("category") { CategorySelectionScreen(navController) }
//                        composable("playing/{sessionId}/{category}") { backStackEntry ->
//                            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
//                            val category = backStackEntry.arguments?.getString("category") ?: ""
//                            PlayingGameScreen(navController, sessionId, category)
//                        }
//                        composable("gameover") { GameOverScreen(navController) }
//                    }
//                }
            JetPackPamTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    // AppNavGraph controls all navigation & session flow
                    AppNavGraph(
                        googleAuthClient = googleAuthClient,
                        authViewModel = authViewModel
                    )
                }
            }
        }

    }
}
