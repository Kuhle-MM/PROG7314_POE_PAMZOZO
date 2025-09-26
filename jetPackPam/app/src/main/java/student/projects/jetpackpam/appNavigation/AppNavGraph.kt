package student.projects.jetpackpam.appNavigation

import student.projects.jetpackpam.screens.mainapp.MainScreen
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.screens.accounthandler.LoginScreen
import student.projects.jetpackpam.screens.accounthandler.SignUpScreen

@Composable
fun AppNavGraph(navController: NavHostController = rememberNavController()) {
    val authViewModel: AuthorizationModelViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "login" // Start with login
    ) {
        // Login Screen
        composable("login") {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }

        // Sign Up Screen
        composable("signUp") {
            SignUpScreen(navController = navController, authViewModel = authViewModel)
        }

        // Main App (contains bottom navigation)
        composable("main") { // 👈 renamed from "home" to avoid conflict
            MainScreen(authViewModel, rootNavController = navController)
        }
    }
}
