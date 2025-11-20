package student.projects.jetpackpam.screens.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import student.projects.jetpackpam.models.LogsViewModel
import student.projects.jetpackpam.screens.settings.ControllerSizeState.selectedSize

val paleCard = Color(0xFFFFFFFF)

@Composable
fun SettingsScreen(navController: NavController,
                   logsViewModel: LogsViewModel
) {
    // Expansion states
    var logsExpanded by remember { mutableStateOf(false) }
    var motorPositionExpanded by remember { mutableStateOf(false) }
    var motorSpeedExpanded by remember { mutableStateOf(false) }
    var controllerSizeExpanded by remember { mutableStateOf(false) }
    var biometricsExpanded by remember { mutableStateOf(false) }
    var followMeExpanded by remember { mutableStateOf(false) }
    val intervals = listOf(2, 5, 10, 15, 20, 30, 45, 60)
    var expanded by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // -----------------------
        // Canvas background circles
        // -----------------------
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 15f)

            // Top-right circle
            drawCircle(
                color = Color(0xFFF0A1F8),
                radius = 125f,

                center = Offset(x = size.width - 50f, y = 50f),
                style = stroke
            )

            // Bottom-left circle
            drawCircle(
                color = Color(0xFFFF9BC9),
                radius = 320f,
                center = Offset(x = 50f, y = size.height - 50f),
                style = stroke
            )
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // -------------------------
        // Logs
        // -------------------------
        SettingsCategory("Logs")

        ExpandableSettingsItem(
            expanded = expanded,
            onExpandToggle = { expanded = !expanded },
            icon = Icons.Default.ListAlt,
            title = "Change Log Interval",
            subtitle = "Adjust how frequently logs are filtered."
        ) {
            Column(Modifier.padding(start = 56.dp, bottom = 12.dp)) {

                Text("Select Interval:", fontWeight = FontWeight.Bold)

                intervals.forEach { minutes ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { logsViewModel.updateInterval(minutes) }
                    ) {
                        RadioButton(
                            selected = logsViewModel.selectedInterval == minutes,
                            onClick = { logsViewModel.updateInterval(minutes) }
                        )
                        Text("$minutes minutes", Modifier.padding(start = 8.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // -------------------------
        // Motors
        // -------------------------
        SettingsCategory("Motors")

        // Gear Shift Position
        ExpandableSettingsItem(
            expanded = motorPositionExpanded,
            onExpandToggle = { motorPositionExpanded = !motorPositionExpanded },
            icon = Icons.Default.SettingsRemote,
            title = "Gear Shift Position",
            subtitle = "Adjust the position of your gear-shift controller."
        ) {
            Column(Modifier.padding(start = 56.dp, bottom = 12.dp)) {

                val positions = listOf("Top", "Left", "Right", "Bottom")
                var selected by remember { mutableStateOf(GearShiftPositionState.selectedPosition) }

                positions.forEach { pos ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable {
                                selected = pos
                                GearShiftPositionState.selectedPosition = pos
                            }
                    ) {
                        RadioButton(
                            selected = selected == pos,
                            onClick = {
                                selected = pos
                                GearShiftPositionState.selectedPosition = pos
                            }
                        )
                        Text(pos, Modifier.padding(start = 8.dp))
                    }
                }
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
                Text("Select Joystick Size:", fontWeight = FontWeight.Bold)

                val sizes = listOf(0.8f, 1f, 1.2f) // small, medium, large
                val labels = listOf("Small", "Medium", "Large")
                var selectedSize by remember { mutableStateOf(1f) }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    sizes.forEachIndexed { index, sizeValue ->
                        Box(
                            modifier = Modifier
                                .size((60 * sizeValue).dp)
                                .background(
                                    color = if (selectedSize == sizeValue) Color(0xFFE34FF2) else Color.Gray,
                                    shape = CircleShape
                                )
                                .clickable { selectedSize = sizeValue },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                labels[index],
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Expose the selected size to the ControlsScreen
            ControllerSizeState.selectedSize = selectedSize
        }


        Spacer(Modifier.height(24.dp))
        // -------------------------
        // BLE features
        // -------------------------
        SettingsCategory("Accessibility")
        // Follow me
        ExpandableSettingsItem(
            expanded = followMeExpanded,
            onExpandToggle = { followMeExpanded = !followMeExpanded },
            icon = Icons.Default.People,
            title = "Follow me feature",
            subtitle = "Allows me to follow you using bluetooth, without controlling me until you deactivate this feature."
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
fun generateTimeBlocks(interval: Int): List<Pair<String, Int>> {

    val blocks = mutableListOf<Pair<String, Int>>()

    // For example: if interval = 10 → 0-9, 10-19, 20-29 ...
    for (i in 0 until (60 / interval)) {
        val start = i * interval
        val end = start + interval - 1
        blocks.add("${start}–${end} min" to i)
    }

    return blocks
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
object ControllerSizeState {
    var selectedSize by mutableStateOf(1f) // default medium
}

object GearShiftPositionState {
    var selectedPosition by mutableStateOf("Right")
}