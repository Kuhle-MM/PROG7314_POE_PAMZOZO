package student.projects.jetpackpam.appNavigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import student.projects.jetpackpam.screens.accounthandler.LoginScreen
import student.projects.jetpackpam.screens.bottomnavscreen.GamesScreen
import student.projects.jetpackpam.screens.bottomnavscreen.HomeScreen
import student.projects.jetpackpam.screens.bottomnavscreen.VideoScreen

@Composable
fun BottomNavGraph(navController: NavHostController, paddingValues: PaddingValues) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Home.route,
        modifier = Modifier.padding(paddingValues)
    ) {
        composable(BottomBarScreen.Home.route) { HomeScreen() }
        composable(BottomBarScreen.Video.route) { VideoScreen() }
        composable(BottomBarScreen.Games.route) { GamesScreen() }
    }
}