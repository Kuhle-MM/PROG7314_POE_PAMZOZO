package student.projects.jetpackpam.screens.mainapp

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.*
import androidx.compose.runtime.*
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
import kotlinx.coroutines.launch
import student.projects.jetpackpam.appNavigation.BottomBarScreen
import student.projects.jetpackpam.appNavigation.BottomNavGraph
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient
import student.projects.jetpackpam.screens.sidenavscreen.SideNav

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    authViewModel: AuthorizationModelViewModel,
    rootNavController: NavHostController,
    googleAuthClient: GoogleAuthClient,
    languageViewModel: LanguageViewModel
) {
    val bottomNavController = rememberNavController()
    val activity = LocalContext.current as Activity
    val windowSizeClass: WindowSizeClass = calculateWindowSizeClass(activity)
    val isExpanded = windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Observe UI texts
    val uiTexts by remember { derivedStateOf { languageViewModel.uiTexts } }

    if (isExpanded) {
        // Tablet / Large Screen Layout → Side Navigation
        Row(modifier = Modifier.fillMaxSize()) {
            SideBar(navController = bottomNavController, uiTexts = uiTexts)
            Box(modifier = Modifier.weight(1f)) {
                BottomNavGraph(
                    navController = bottomNavController,
                    paddingValues = PaddingValues(16.dp),
                    googleAuthClient = googleAuthClient,
                    authViewModel = authViewModel,
                    languageViewModel
                )
            }
        }
    } else {
        // Phone / Portrait → Bottom Navigation + Drawer on Home tab
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                SideNav(
                    currentRoute = getCurrentRoute(bottomNavController),
                    onItemSelected = { route ->
                        bottomNavController.navigate(route) { launchSingleTop = true }
                        scope.launch { drawerState.close() }
                    },
                    onLogout = {
                        try {
                            authViewModel.signOut()
                            Toast.makeText(
                                context,
                                uiTexts["signOutSuccess"] ?: "Signed out successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            rootNavController.navigate("login") {
                                popUpTo("main") { inclusive = true }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                context,
                                "${uiTexts["signOutFailed"] ?: "Sign-out failed"}: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        scope.launch { drawerState.close() }
                    }
                )
            }
        ) {
            Scaffold(
                topBar = {
                    val currentRoute = getCurrentRoute(bottomNavController)
                    val title = when (currentRoute) {
                        "home" -> uiTexts["home"] ?: "Home"
                        "video" -> uiTexts["video"] ?: "Video"
                        "games" -> uiTexts["games"] ?: "Games"
                        "language" -> uiTexts["language"] ?: "Language"
                        "fontSize" -> uiTexts["fontSize"] ?: "Font Size"
                        "pamTheme" -> uiTexts["pamTheme"] ?: "Pam Theme"
                        "personality" -> uiTexts["personality"] ?: "Personality"
                        "chat" -> uiTexts["chat"] ?: "Chat"
                        else -> uiTexts["profile"] ?: "Profile"
                    }

                    TopAppBar(
                        title = { Text(title) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(
                                    Icons.Default.Settings,
                                    contentDescription = uiTexts["settings"] ?: "Settings"
                                )
                            }
                        }
                    )
                },
                bottomBar = { BottomBar(navController = bottomNavController, uiTexts = uiTexts) }
            ) { innerPadding ->
                BottomNavGraph(
                    navController = bottomNavController,
                    paddingValues = innerPadding,
                    googleAuthClient = googleAuthClient,
                    authViewModel = authViewModel,
                    languageViewModel
                )
            }
        }
    }
}

/** 🔹 Helper to get current route **/
@Composable
private fun getCurrentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

/** 🔹 Bottom Navigation for Phones **/
@Composable
fun BottomBar(navController: NavHostController, uiTexts: Map<String, String>) {
    val screens = listOf(
        BottomBarScreen.Home to (uiTexts["home"] ?: "Home"),
        BottomBarScreen.Video to (uiTexts["video"] ?: "Video"),
        BottomBarScreen.Games to (uiTexts["games"] ?: "Games")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
        screens.forEach { (screen, title) ->
            val selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
            val animatedColor by animateColorAsState(
                targetValue = if (selected) Color(0xFFB48CFF) else Color.Gray
            )

            NavigationBarItem(
                label = { Text(title, color = animatedColor) },
                icon = { Icon(screen.icon, contentDescription = title, tint = animatedColor) },
                selected = selected,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.findStartDestination().id)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

/** 🔹 Side Navigation for Tablets **/
@Composable
fun SideBar(navController: NavHostController, uiTexts: Map<String, String>) {
    val screens = listOf(
        BottomBarScreen.Home to (uiTexts["home"] ?: "Home"),
        BottomBarScreen.Video to (uiTexts["video"] ?: "Video"),
        BottomBarScreen.Games to (uiTexts["games"] ?: "Games")
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationRail(containerColor = MaterialTheme.colorScheme.background) {
        screens.forEach { (screen, title) ->
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
                icon = { Icon(screen.icon, contentDescription = title, tint = animatedColor) },
                label = { Text(title, color = animatedColor) }
            )
        }
    }
}
