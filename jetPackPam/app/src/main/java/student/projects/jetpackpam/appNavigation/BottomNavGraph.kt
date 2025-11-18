package student.projects.jetpackpam.appNavigation

import VideoScreen
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.screens.ChatScreen
import student.projects.jetpackpam.screens.sidenavscreen.ProfileScreen
import student.projects.jetpackpam.screens.bottomnavscreen.HomeScreen
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient
import student.projects.jetpackpam.screens.charades.CategorySelectionScreen
import student.projects.jetpackpam.screens.charades.GameOverScreen
import student.projects.jetpackpam.screens.charades.PlayingGameScreen
import student.projects.jetpackpam.screens.charades.StartUpScreen
import student.projects.jetpackpam.screens.firsttimecustom.FontSelectionScreen
import student.projects.jetpackpam.screens.firsttimecustom.LanguageSelectionScreen
import student.projects.jetpackpam.screens.firsttimecustom.PamThemeSelectionScreen
import student.projects.jetpackpam.screens.firsttimecustom.PersonalitySelectionScreen2
import student.projects.jetpackpam.screens.livelogs.LiveLogsScreen
import student.projects.jetpackpam.util.DeviceConfiguration

@Composable
fun BottomNavGraph(
    navController: NavHostController,
    paddingValues: PaddingValues,
    googleAuthClient: GoogleAuthClient,
    authViewModel: AuthorizationModelViewModel,
    languageViewModel: LanguageViewModel
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val userData by authViewModel.userData.collectAsStateWithLifecycle()

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(paddingValues)
    ) {

        composable("home") {
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel,
                languageViewModel = languageViewModel,
                uiTexts = languageViewModel.uiTexts
            )

        }



        composable("chat") {
            ChatScreen(languageViewModel= languageViewModel)
        }

        composable("profile") {
            ProfileScreen(
                userData = userData,
                languageViewModel= languageViewModel,
                onSignOut = {
                    authViewModel.signOutSafely(context, navController,)
                }
            )
        }

        composable("video") {
            VideoScreen(languageViewModel= languageViewModel)
        }

        composable("games") { StartUpScreen(navController) }
        composable("start") { StartUpScreen(navController) }
        composable("category") { CategorySelectionScreen(navController) }
        composable("liveLogs") { LiveLogsScreen(navController) }
        composable("playing/{sessionId}/{category}") { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
            val category = backStackEntry.arguments?.getString("category") ?: ""
            PlayingGameScreen(navController, sessionId, category)
        }

        composable(
            route = "gameover?correct={correct}&skipped={skipped}",
            arguments = listOf(
                navArgument("correct") { type = NavType.StringType; defaultValue = "" },
                navArgument("skipped") { type = NavType.StringType; defaultValue = "" }
            )
        ) { backStackEntry ->
            val correct = backStackEntry.arguments?.getString("correct")
            val skipped = backStackEntry.arguments?.getString("skipped")
            GameOverScreen(navController = navController, correct = correct, skipped = skipped)
        }

        composable("language") {
            LanguageSelectionScreen(languageViewModel = languageViewModel)
        }

        composable("fontSize") { FontSelectionScreen(languageViewModel= languageViewModel) }
        composable("pamTheme") { PamThemeSelectionScreen(languageViewModel= languageViewModel) }
        composable("personality") { PersonalitySelectionScreen2(languageViewModel= languageViewModel) }
    }
}
