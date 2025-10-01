package student.projects.jetpackpam.appNavigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.screens.accounthandler.LoginScreen
import student.projects.jetpackpam.screens.accounthandler.SignUpScreen
import student.projects.jetpackpam.screens.mainapp.MainScreen

@Composable
fun RootNavGraph(authViewModel: AuthorizationModelViewModel) {
    val navController = rememberNavController() // must be at the top

    NavHost(navController = navController, startDestination = "login") {
        // Auth screens
        composable("login") { LoginScreen(navController, authViewModel) }
        composable("signup") { SignUpScreen(navController, authViewModel) }

        // Main app screen
        composable("main") { MainScreen(authViewModel) }
    }
}

