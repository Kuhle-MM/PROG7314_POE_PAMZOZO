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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
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
import student.projects.jetpackpam.localization.LocalLanguageViewModel
import student.projects.jetpackpam.localization.t
import student.projects.jetpackpam.retrofit.CharadesRetrofitInstance
import student.projects.jetpackpam.ui.theme.Shapes
import kotlin.collections.isNotEmpty



@Composable
fun StartUpScreen(navController: NavController) {
    val context = LocalContext.current
    val languageViewModel = LocalLanguageViewModel.current

    DisposableEffect(Unit) {
        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFFFFFFF)
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_play_circle_outline_24),
                    contentDescription = t("Play button"),
                    modifier = Modifier
                        .size(120.dp)
                        .clip(shape = Shapes.large)
                        .clickable { navController.navigate("category") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = t("Tap to play"),
                    fontSize = 24.sp,
                    color = Color.Black
                )
            }
        }
    }
}


@Composable
fun CategorySelectionScreen(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var categories by remember { mutableStateOf<List<String>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose { (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }

    LaunchedEffect(Unit) {
        try {
            isLoading = true
            categories = CharadesRetrofitInstance.api.getCategories()
        } catch (e: Exception) {
            errorMessage = e.localizedMessage ?: "Failed to load categories"
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFFFFFFF)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                t("Select a Category"),
                style = MaterialTheme.typography.titleLarge.copy(color = Color.Black),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            when {
                isLoading -> CircularProgressIndicator()
                errorMessage != null -> Text(errorMessage!!, color = Color.Red)
                categories.isNotEmpty() -> {
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
                                                CharadesRequest(category = category, roundSeconds = 60)
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
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                }
                else -> Text(t("No categories found"), color = Color.Gray)
            }
        }
    }
}


@SuppressLint("ServiceCast")
@Composable
fun PlayingGameScreen(navController: NavController, sessionId: String, category: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // --- Game state ---
    var guesses by remember { mutableStateOf<List<GuessItem>>(emptyList()) }
    var currentIndex by remember { mutableStateOf(0) }
    var currentGuess by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var timeLeft by remember { mutableStateOf(30) } // 30s per round
    var isGameOver by remember { mutableStateOf(false) }

    var correctGuesses by remember { mutableStateOf<List<String>>(emptyList()) }
    var skippedGuesses by remember { mutableStateOf<List<String>>(emptyList()) }

    // --- Orientation ---
    DisposableEffect(Unit) {
        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    // --- Suspend loader (no composable calls here) ---
    suspend fun loadGuesses(): List<GuessItem> =
        CharadesRetrofitInstance.api.getAllGuesses(category)

    // --- Load 12 guesses safely ---
    LaunchedEffect(category) {
        isLoading = true
        val result = runCatching { loadGuesses() }
        guesses = result.getOrElse {
            errorMessage = it.localizedMessage ?: "Failed to load"
            emptyList()
        }.shuffled().take(12)
        currentIndex = 0
        currentGuess = guesses.getOrNull(0)?.text ?: ""
        isLoading = false
    }

    // --- Timer (separate LaunchedEffect) ---
    LaunchedEffect(key1 = isGameOver, key2 = guesses) {
        // reset timer when new round loaded
        if (guesses.isNotEmpty()) {
            timeLeft = 30
            while (timeLeft > 0 && !isGameOver) {
                delay(1000)
                timeLeft--
            }
            if (timeLeft <= 0) isGameOver = true
        }
    }

    // --- Navigate to GameOver when finished ---
    LaunchedEffect(isGameOver) {
        if (isGameOver) {
            val correctParam = Uri.encode(correctGuesses.joinToString(","))
            val skippedParam = Uri.encode(skippedGuesses.joinToString(","))
            navController.navigate("gameover?correct=$correctParam&skipped=$skippedParam") {
                popUpTo("playing/$sessionId/$category") { inclusive = true }
            }
        }
    }

    // --- Haptic ---
    fun vibrateFeedback() {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        vibrator?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.vibrate(VibrationEffect.createOneShot(120, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                it.vibrate(120)
            }
        }
    }

    // --- Next guess (mark skipped when correct==false) ---
    fun nextGuess(isCorrect: Boolean) {
        if (guesses.isEmpty()) return
        if (isCorrect) correctGuesses = correctGuesses + currentGuess
        else skippedGuesses = skippedGuesses + currentGuess

        currentIndex++
        if (currentIndex < guesses.size) {
            currentGuess = guesses[currentIndex].text
        } else {
            isGameOver = true
        }
    }

    // --- Sensor setup (tilt to mark) ---
    val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    var lastActionTime by remember { mutableStateOf(0L) }

    val sensorEventListener = remember {
        object : SensorEventListener {
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
            override fun onSensorChanged(event: SensorEvent) {
                val now = System.currentTimeMillis()
                if (now - lastActionTime < 800) return

                val y = event.values[1]
                if (y < -5f) { // tilt down => correct
                    lastActionTime = now
                    scope.launch {
                        vibrateFeedback()
                        nextGuess(true)
                    }
                } else if (y > 5f) { // tilt up => skip
                    lastActionTime = now
                    scope.launch {
                        vibrateFeedback()
                        nextGuess(false)
                    }
                }
            }
        }
    }

    DisposableEffect(Unit) {
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI)
        onDispose { sensorManager.unregisterListener(sensorEventListener) }
    }

    // --- UI ---
    Scaffold(containerColor = Color(0xFFFFFFFF)) { padding ->
        // Use inner padding, then outer padding 24.dp to match earlier design
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween // top (timer), center (card), bottom (controls)
        ) {
            // TOP: Timer + progress bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "${t("Time")}: $timeLeft s",
                        style = MaterialTheme.typography.titleMedium.copy(color = Color.Black)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // small circular indicator
                    CircularProgressIndicator(
                        progress = (timeLeft / 30f).coerceIn(0f, 1f),
                        modifier = Modifier.size(36.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = Color.Gray,
                        strokeWidth = 4.dp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))


            }
            Spacer(modifier = Modifier.height(8.dp))
            // CENTER: Animated guess card (grows/shrinks as needed)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else if (errorMessage != null) {
                    Text(text = errorMessage ?: "", color = Color.Red)
                } else {
                    androidx.compose.animation.AnimatedContent(targetState = currentGuess) { guess ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .heightIn(min = 140.dp, max = 220.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize().padding(12.dp)) {
                                if (guess.isBlank()) {
                                    Text(t("No guesses found"), color = Color.Black, textAlign = TextAlign.Center)
                                } else {
                                    Text(
                                        text = guess,
                                        color = Color.Black,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            // BOTTOM: counts + actions
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    androidx.compose.animation.AnimatedContent(targetState = correctGuesses.size) { count ->
                        Text("${t("Correct")}: $count", color = Color.Black)
                    }
                    androidx.compose.animation.AnimatedContent(targetState = skippedGuesses.size) { count ->
                        Text("${t("Skipped")}: $count", color = Color.Red)
                    }
                }


                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}



@Composable
fun GameOverScreen(navController: NavController, correct: String?, skipped: String?) {
    val context = LocalContext.current
    val activity = context as? Activity

    DisposableEffect(Unit) {
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }

    val correctList = correct?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    val skippedList = skipped?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    val scrollState = rememberScrollState()

    Scaffold(containerColor = Color(0xFFFFFFFF)) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(t("Game Over"), style = MaterialTheme.typography.headlineMedium.copy(color = Color.White))
            Spacer(modifier = Modifier.height(24.dp))
            Text(t("Correct Words"), color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            correctList.forEach { Text("• $it", color = Color.White, fontSize = 18.sp) }

            Spacer(modifier = Modifier.height(16.dp))

            Text(t("Skipped Words"), color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            skippedList.forEach { Text("• $it", color = Color.White, fontSize = 18.sp) }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    activity?.finish()
                    activity?.let {
                        val intent = it.intent
                        intent.putExtra("navigateToCharades", true)
                        it.startActivity(intent)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8C77F7)),
                modifier = Modifier.fillMaxWidth(0.5f)
            ) {
                Text(t("Restart"), color = Color.White)
            }
        }
    }
}


