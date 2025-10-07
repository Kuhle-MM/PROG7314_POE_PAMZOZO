package student.projects.jetpackpam.screens.charades

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import student.projects.jetpackpam.R
import student.projects.jetpackpam.data.CharadesRequest
import student.projects.jetpackpam.data.GuessItem
import student.projects.jetpackpam.data.GuessRequest
import student.projects.jetpackpam.retrofit.CharadesRetrofitInstance
import kotlin.collections.isNotEmpty

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
@SuppressLint("ServiceCast")
@Composable
fun PlayingGameScreen(navController: NavController, sessionId: String, category: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var guesses by remember { mutableStateOf<List<GuessItem>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var currentGuess by remember { mutableStateOf("Loading...") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var timeLeft by remember { mutableStateOf(60) }
    var isGameOver by remember { mutableStateOf(false) }

    // Track results
    var correctGuesses by remember { mutableStateOf<List<String>>(emptyList()) }
    var skippedGuesses by remember { mutableStateOf<List<String>>(emptyList()) }

    // --- Orientation ---
    DisposableEffect(Unit) {
        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // --- Load guesses ---
    LaunchedEffect(Unit) {
        try {
            guesses = CharadesRetrofitInstance.api.getAllGuesses(category)
            if (guesses.isNotEmpty()) {
                currentGuess = guesses[currentIndex].text
            } else {
                currentGuess = "No guesses found."
            }
        } catch (e: Exception) {
            errorMessage = e.localizedMessage
        }
    }

    // --- Timer ---
    LaunchedEffect(Unit) {
        while (timeLeft > 0 && !isGameOver) {
            delay(1000)
            timeLeft--
        }
        isGameOver = true
    }

    // --- Navigate when finished ---
    if (isGameOver) {
        LaunchedEffect(Unit) {
            val correctParam = Uri.encode(correctGuesses.joinToString(","))
            val skippedParam = Uri.encode(skippedGuesses.joinToString(","))
            navController.navigate("gameover?correct=$correctParam&skipped=$skippedParam") {
                popUpTo("playing/$sessionId/$category") { inclusive = true }
            }
        }
    }

    // --- Helpers ---
    fun vibrateFeedback() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(200)
            }
        }
    }

    fun nextGuess() {
        if (guesses.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % guesses.size
            currentGuess = guesses[currentIndex].text
        }
    }

    // --- Sensor Setup ---
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    var lastActionTime by remember { mutableStateOf(0L) }

    val sensorEventListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

        override fun onSensorChanged(event: SensorEvent) {
            val now = System.currentTimeMillis()
            // Prevent multiple triggers too fast
            if (now - lastActionTime < 800) return

            val y = event.values[1] // Landscape tilt detection uses Y axis

            if (y < -5f) { // Device tilted DOWN (correct)
                lastActionTime = now
                scope.launch {
                    vibrateFeedback()
                    correctGuesses = correctGuesses + currentGuess
                    nextGuess()
                }
            } else if (y > 5f) { // Device tilted UP (skip)
                lastActionTime = now
                scope.launch {
                    vibrateFeedback()
                    skippedGuesses = skippedGuesses + currentGuess
                    nextGuess()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(sensorEventListener) }
    }

    // --- UI ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = Color.White)
        } else if (errorMessage != null) {
            Text(errorMessage!!, color = Color.Red)
        } else {
            Text(
                text = "⏱ $timeLeft s left",
                style = MaterialTheme.typography.titleLarge.copy(color = Color.Yellow)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = currentGuess,
                style = MaterialTheme.typography.headlineLarge.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { nextGuess() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C77F7))
            ) {
                Text("Next Guess", color = Color.White)
            }
        }
    }
}

@Composable
fun GameOverScreen(navController: NavController, correct: String?, skipped: String?) {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    val correctList = correct?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    val skippedList = skipped?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

    // Scrollable for long lists
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF202020))
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Game Over!",
            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("Correct Words:", color = Color.Green, fontWeight = FontWeight.Bold)
        correctList.forEach { word ->
            Text("• $word", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text("Skipped Words:", color = Color.Red, fontWeight = FontWeight.Bold)
        skippedList.forEach { word ->
            Text("• $word", color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))

        val activity = (LocalContext.current as? Activity)

        Button(
            onClick = {
                activity?.finish()
                activity?.let {
                    val intent = it.intent
                    intent.putExtra("navigateToCharades", true)
                    it.startActivity(intent)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C77F7))
        ) {
            Text("Go home", color = Color.White)
        }

    }
}
