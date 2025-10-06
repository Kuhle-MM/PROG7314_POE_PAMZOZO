package student.projects.jetpackpam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetPackPamTheme(){

                //MainScreen()
                //LoginScreen()
                LanguageSelectionScreen()
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
            }
        }

    }
}
