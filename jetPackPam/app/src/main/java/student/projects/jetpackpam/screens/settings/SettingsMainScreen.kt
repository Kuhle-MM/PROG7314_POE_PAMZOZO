package student.projects.jetpackpam.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

// Keep theme consistent
val paleBackground = Color(0xFFF4F4F4)
val paleCard = Color(0xFFFFFFFF)

@Composable
fun SettingsScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            //.background(paleBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // CATEGORY: Logs
        SettingsCategory(title = "Logs")

        SettingsItem(
            icon = Icons.Default.ListAlt,
            title = "Change Log Timed Options",
            subtitle = "Adjust how frequently your logs can be filtered.",
            onClick = { navController.navigate("settingsLogs") }
        )

        Spacer(Modifier.height(28.dp))

        // CATEGORY: Motors
        SettingsCategory(title = "Motors")

        SettingsItem(
            icon = Icons.Default.DirectionsCar,
            title = "Motor Controller Position",
            subtitle = "Adjust the position of your motor controller.",
            onClick = { navController.navigate("settingsMotorPosition") }
        )

        SettingsItem(
            icon = Icons.Default.DirectionsCar,
            title = "Motor Speed Range",
            subtitle = "Control the speed of P.A.M",
            onClick = { navController.navigate("settingsMotorSpeed") }
        )

        SettingsItem(
            icon = Icons.Default.Build,
            title = "Controller Size",
            subtitle = "Change the size of your controller.",
            onClick = { navController.navigate("settingsControllerSize") }
        )

        Spacer(Modifier.height(28.dp))

        // CATEGORY: Biometrics
        SettingsCategory(title = "Biometrics")

        SettingsItem(
            icon = Icons.Default.Fingerprint,
            title = "Enable Biometrics",
            subtitle = "Enable or diable biometric authentication",
            onClick = { navController.navigate("settingsBiometrics") }
        )
    }
}

@Composable
fun SettingsCategory(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SettingsItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = paleCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )

            Spacer(Modifier.width(16.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )
            }
        }
    }
}
