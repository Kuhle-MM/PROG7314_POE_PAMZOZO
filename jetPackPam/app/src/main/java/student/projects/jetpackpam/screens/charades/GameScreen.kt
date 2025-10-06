package student.projects.jetpackpam.screens.charades

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import student.projects.jetpackpam.R
import student.projects.jetpackpam.data.CharadesRequest
import student.projects.jetpackpam.retrofit.CharadesRetrofitInstance

//@Composable
//fun CharadesNavGraph() {
//    val navController = rememberNavController()
//
//    NavHost(navController = navController, startDestination = "start") {
//        composable("start") { StartUpScreen(navController) }
//        composable("category") { CategorySelectionScreen(navController) }
//        composable("playing/{sessionId}/{category}") { backStackEntry ->
//            val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
//            val category = backStackEntry.arguments?.getString("category") ?: ""
//            PlayingGameScreen(navController, sessionId, category)
//        }
//        composable("gameover") { GameOverScreen(navController) }
//    }
//}

@Composable
fun StartUpScreen(navController: NavController) {
    // Force landscape orientation
    val context = LocalContext.current
    DisposableEffect(Unit) {
        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            // Optional: restore portrait on exit
            (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.drawable.baseline_play_circle_outline_24), 
                contentDescription = "Play Button",
                modifier = Modifier
                    .size(120.dp)
                    .clickable {
                        navController.navigate("category") // Go to CategorySelectionScreen
                    }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Tap to Play",
                fontSize = 24.sp
            )
        }
    }
}
@Composable
fun CategorySelectionScreen(navController: NavController) {
    // Force landscape orientation
    val context = LocalContext.current
    DisposableEffect(Unit) {
        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    // Fetch categories from API
    LaunchedEffect(Unit) {
        try {
            isLoading = true
            errorMessage = null
            categories = CharadesRetrofitInstance.api.getCategories()
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Failed to load categories"
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Select a Category",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        when {
            isLoading -> CircularProgressIndicator()

            errorMessage != null -> Text(
                "⚠️ $errorMessage",
                color = MaterialTheme.colorScheme.error
            )

            categories.isNotEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(22.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(categories) { category ->
                            val isSelected = selectedCategory == category

                            OutlinedCard(
                                onClick = {
                                    coroutineScope.launch {
                                        selectedCategory = category
                                        try {
                                            val response = CharadesRetrofitInstance.api.startGame(
                                                CharadesRequest(
                                                    category = category,
                                                    roundSeconds = 60
                                                )
                                            )
                                            navController.navigate(
                                                "playing/${response.sessionId}/${response.category}"
                                            )
                                        } catch (e: Exception) {
                                            errorMessage = e.localizedMessage ?: "Could not start game"
                                        }
                                    }
                                },
                                modifier = Modifier.size(150.dp),
                                shape = CircleShape,
                                border = BorderStroke(
                                    3.dp,
                                    if (isSelected) Color(0xFFB48CFF) else Color(0xFFF0A1F8)
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0x22B48CFF) else Color.Transparent
                                )
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = category.replaceFirstChar { it.uppercase() },
                                        textAlign = TextAlign.Center,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            else -> Text("No categories found.")
        }
    }
}

@Composable
fun PlayingGameScreen(navController: NavController, sessionId: String, category: String) {
    // Force landscape orientation
    val context = LocalContext.current
    DisposableEffect(Unit) {
        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            // Optional: restore portrait on exit
            (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    Text("Session: $sessionId\nCategory: $category\nGame is starting...")
}

@Composable
fun GameOverScreen(navController: NavController) {
    // Force landscape orientation
    val context = LocalContext.current
    DisposableEffect(Unit) {
        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            // Optional: restore portrait on exit
            (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    Text("Game Over!")
    Button(onClick = { navController.navigate("startup") }) {
        Text("Restart")
    }
}