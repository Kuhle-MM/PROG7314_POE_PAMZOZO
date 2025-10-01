package student.projects.jetpackpam.screens.mainapp

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.wear.compose.material.ContentAlpha
import student.projects.jetpackpam.appNavigation.AuthNavGraph
import student.projects.jetpackpam.appNavigation.BottomBarScreen
import student.projects.jetpackpam.appNavigation.BottomNavGraph
import student.projects.jetpackpam.appNavigation.RootNavGraph
import student.projects.jetpackpam.models.AuthorizationModelViewModel


@Composable
fun MainScreen(authViewModel: AuthorizationModelViewModel) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = { BottomBar(navController = navController) }
    ) { innerPadding ->
        BottomNavGraph(
            navController = navController,
            paddingValues = innerPadding
        )
    }
}

@Composable
fun BottomBar(navController: androidx.navigation.NavHostController) {
    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.Video,
        BottomBarScreen.Games,
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
        screens.forEach { screen ->
            AddItem(
                screen = screen,
                currentDestination = currentDestination,
                navController = navController
            )
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomBarScreen,
    currentDestination: NavDestination?,
    navController: androidx.navigation.NavHostController
) {
    NavigationBarItem(
        label = { Text(text = screen.title) },
        icon = {
            Icon(
                imageVector = screen.icon,
                contentDescription = "Navigation Icon"
            )
        },
        selected = currentDestination?.hierarchy?.any {
            it.route == screen.route
        } == true,
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        }
    )
}