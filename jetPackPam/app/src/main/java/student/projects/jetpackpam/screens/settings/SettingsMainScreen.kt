package student.projects.jetpackpam.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

val paleCard = Color(0xFFFFFFFF)

@Composable
fun SettingsScreen(navController: NavController) {

    // Expansion states
    var logsExpanded by remember { mutableStateOf(false) }
    var motorPositionExpanded by remember { mutableStateOf(false) }
    var motorSpeedExpanded by remember { mutableStateOf(false) }
    var controllerSizeExpanded by remember { mutableStateOf(false) }
    var biometricsExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // -------------------------
        // Logs
        // -------------------------
        SettingsCategory("Logs")

        ExpandableSettingsItem(
            expanded = logsExpanded,
            onExpandToggle = { logsExpanded = !logsExpanded },
            icon = Icons.Default.ListAlt,
            title = "Change Log Timed Options",
            subtitle = "Adjust how frequently logs are filtered."
        ) {
            Column(Modifier.padding(start = 56.dp, bottom = 12.dp)) {
                Text("Select Interval:", fontWeight = FontWeight.Bold)

                val options = listOf("Every 10 min", "Every 30 min", "Hourly")
                var selected by remember { mutableStateOf(options.first()) }

                options.forEach { label ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { selected = label }
                    ) {
                        RadioButton(
                            selected = selected == label,
                            onClick = { selected = label }
                        )
                        Text(label, Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // -------------------------
        // Motors
        // -------------------------
        SettingsCategory("Motors")

        // Motor position
        ExpandableSettingsItem(
            expanded = motorPositionExpanded,
            onExpandToggle = { motorPositionExpanded = !motorPositionExpanded },
            icon = Icons.Default.SettingsRemote,
            title = "Motor Controller Position",
            subtitle = "Adjust the position of your motor controller."
        ) {
            Column(Modifier.padding(start = 56.dp, bottom = 12.dp)) {
                val positions = listOf("Front", "Middle", "Back")
                var selected by remember { mutableStateOf(positions.first()) }

                positions.forEach { pos ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { selected = pos }
                    ) {
                        RadioButton(
                            selected = selected == pos,
                            onClick = { selected = pos }
                        )
                        Text(pos, Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        // Motor speed
        ExpandableSettingsItem(
            expanded = motorSpeedExpanded,
            onExpandToggle = { motorSpeedExpanded = !motorSpeedExpanded },
            icon = Icons.Default.DirectionsCar,
            title = "Motor Speed Range",
            subtitle = "Control the speed of P.A.M."
        ) {
            Column(Modifier.padding(start = 56.dp, bottom = 12.dp)) {
                Text("Speed:", fontWeight = FontWeight.Bold)
                var speed by remember { mutableStateOf(50f) }

                Slider(
                    value = speed,
                    onValueChange = { speed = it },
                    valueRange = 0f..100f
                )
                Text("${speed.toInt()}%")
            }
        }

        // Controller size
        ExpandableSettingsItem(
            expanded = controllerSizeExpanded,
            onExpandToggle = { controllerSizeExpanded = !controllerSizeExpanded },
            icon = Icons.Default.Build,
            title = "Controller Size",
            subtitle = "Change the size of your controller."
        ) {
            Column(Modifier.padding(start = 56.dp, bottom = 12.dp)) {
                var size by remember { mutableStateOf(1f) }
                Text("Size:", fontWeight = FontWeight.Bold)

                Slider(
                    value = size,
                    onValueChange = { size = it },
                    valueRange = 0.5f..2f
                )
                Text(String.format("%.1fx", size))
            }
        }

        Spacer(Modifier.height(24.dp))

        // -------------------------
        // Biometrics
        // -------------------------
        SettingsCategory("Biometrics")

        ExpandableSettingsItem(
            expanded = biometricsExpanded,
            onExpandToggle = { biometricsExpanded = !biometricsExpanded },
            icon = Icons.Default.Fingerprint,
            title = "Enable Biometrics",
            subtitle = "Enable or disable biometric authentication."
        ) {
            Column(Modifier.padding(start = 56.dp, bottom = 12.dp)) {

                var enabled by remember { mutableStateOf(false) }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Biometrics Enabled")
                    Switch(
                        checked = enabled,
                        onCheckedChange = { enabled = it }
                    )
                }
            }
        }
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
fun ExpandableSettingsItem(
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onExpandToggle() }
            .padding(bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = paleCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
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

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                content()
            }
        }
    }
}
