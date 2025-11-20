package student.projects.jetpackpam.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import student.projects.jetpackpam.data.local.OfflineRepository

@Composable
fun SettingsMotorSpeedScreen(
    navController: NavController,
    offlineRepo: OfflineRepository,
    uid: String
) {

    val PinkAccent = Color(0xFFE34FF2)
    val coroutineScope = rememberCoroutineScope()

    // Load saved speed from offline DB
    var speed by remember { mutableFloatStateOf(MotorSpeedState.speed) }
    LaunchedEffect(Unit) {
        offlineRepo.getUnsynced(uid).forEach { item ->
            if (item.dataKey == "motor_speed") {
                speed = item.jsonData.toFloatOrNull() ?: speed
                MotorSpeedState.speed = speed
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .background(Color.White)
    ) {

        Text(
            "Motor Speed",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Text("Set motor speed:")

        Spacer(Modifier.height(26.dp))

        Slider(
            value = speed,
            onValueChange = {
                speed = it
                MotorSpeedState.speed = it
                // Save offline whenever slider changes
                coroutineScope.launch(Dispatchers.IO) {
                    offlineRepo.saveOffline(uid, "motor_speed", it)
                }
            },
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                activeTrackColor = PinkAccent,
                thumbColor = PinkAccent
            )
        )

        Spacer(Modifier.height(10.dp))

        Text("Speed: ${speed.toInt()}%")
    }
}

// Global state to persist motor speed across screens
object MotorSpeedState {
    var speed by mutableFloatStateOf(50f)
}
