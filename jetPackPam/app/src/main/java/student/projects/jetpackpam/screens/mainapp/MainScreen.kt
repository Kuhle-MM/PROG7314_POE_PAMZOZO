package student.projects.jetpackpam.screens.mainapp

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import student.projects.jetpackpam.appNavigation.BottomBarScreen
import student.projects.jetpackpam.appNavigation.BottomNavGraph
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun MainScreen(
    authViewModel: AuthorizationModelViewModel,
    rootNavController: NavHostController,
    googleAuthClient: GoogleAuthClient
) {
    val bottomNavController = rememberNavController()
    val activity = LocalContext.current as Activity
    val windowSizeClass: WindowSizeClass = calculateWindowSizeClass(activity)
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded

    if (isExpanded) {
        // Tablet / Large Screen Layout → Side Navigation
        Row(modifier = Modifier.fillMaxSize()) {
            SideBar(navController = bottomNavController)
            Box(modifier = Modifier.weight(1f)) {
                BottomNavGraph(
                    navController = bottomNavController,
                    paddingValues = PaddingValues(16.dp),
                    googleAuthClient = googleAuthClient,
                    authViewModel = authViewModel
                )
            }
        }
    } else {
        // Phone / Portrait → Bottom Navigation
        Scaffold(
            bottomBar = { BottomBar(navController = bottomNavController) }
        ) { innerPadding ->
            BottomNavGraph(
                navController = bottomNavController,
                paddingValues = innerPadding,
                googleAuthClient = googleAuthClient,
                authViewModel = authViewModel
            )
        }
    }
}

@Composable
fun BottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.Video,
        BottomBarScreen.Games
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
        screens.forEach { screen ->
            AddItem(screen, currentDestination, navController)
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomBarScreen,
    currentDestination: NavDestination?,
    navController: NavHostController
) {
    val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
    val animatedColor by animateColorAsState(
        targetValue = if (selected) Color(0xFFB48CFF) else Color.Gray
    )

    NavigationBarItem(
        label = { Text(screen.title, color = animatedColor) },
        icon = { Icon(screen.icon, contentDescription = screen.title, tint = animatedColor) },
        selected = selected,
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        }
    )
}

@Composable
fun SideBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreen.Home,
        BottomBarScreen.Video,
        BottomBarScreen.Games
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationRail(containerColor = MaterialTheme.colorScheme.background) {
        screens.forEach { screen ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            val animatedColor by animateColorAsState(
                targetValue = if (selected) Color(0xFFB48CFF) else Color.Gray
            )

            NavigationRailItem(
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id)
                        launchSingleTop = true
                    }
                },
                icon = { Icon(screen.icon, contentDescription = screen.title, tint = animatedColor) },
                label = { Text(screen.title, color = animatedColor) }
            )
        }
    }
}