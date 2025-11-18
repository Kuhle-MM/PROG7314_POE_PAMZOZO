package student.projects.jetpackpam.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun SettingsMotorSpeedScreen(navController: NavController) {

    val PinkAccent = Color(0xFFE34FF2)
    var speed by remember { mutableFloatStateOf(50f) }

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
            onValueChange = { speed = it },
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
