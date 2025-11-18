package student.projects.jetpackpam.screens.controls

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import student.projects.jetpackpam.screens.settings.ControllerSizeState
import student.projects.jetpackpam.screens.settings.GearShiftPositionState
import kotlin.math.hypot
import kotlin.math.min

// =========================================================================================
// MAIN SCREEN
// =========================================================================================
@Composable
fun Controls(navController: NavController) {

    val gearPosition by remember { derivedStateOf { GearShiftPositionState.selectedPosition } }

    var knobOffset by remember { mutableStateOf(Offset.Zero) }
    var joystickRadius by remember { mutableStateOf(0f) }
    var selectedGear by remember { mutableStateOf(1) }

    val sizeMultiplier by remember { derivedStateOf { ControllerSizeState.selectedSize } }

    val speed by remember {
        derivedStateOf {
            val distanceFactor =
                if (joystickRadius > 0)
                    min(1f, hypot(knobOffset.x, knobOffset.y) / joystickRadius)
                else 0f

            (selectedGear * distanceFactor * 100).toInt()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A1A1A))
    ) {

        when (gearPosition) {

            // -----------------------------------------------------------------------------------
            // TOP
            // -----------------------------------------------------------------------------------
            "Top" -> {
                Column(Modifier.fillMaxSize()) {

                    GearRowTop(
                        selected = selectedGear,
                        speed = speed,
                        onSelect = { selectedGear = it }
                    )

                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Joystick(
                            knobOffset = knobOffset,
                            sizeMultiplier = sizeMultiplier,
                            onDrag = { knobOffset = it },
                            onReset = { knobOffset = Offset.Zero },
                            radiusChanged = { joystickRadius = it }
                        )
                    }
                }
            }

            // -----------------------------------------------------------------------------------
            // BOTTOM
            // -----------------------------------------------------------------------------------
            "Bottom" -> {
                Column(Modifier.fillMaxSize()) {

                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Joystick(
                            knobOffset = knobOffset,
                            sizeMultiplier = sizeMultiplier,
                            onDrag = { knobOffset = it },
                            onReset = { knobOffset = Offset.Zero },
                            radiusChanged = { joystickRadius = it }
                        )
                    }

                    GearRowBottom(
                        selected = selectedGear,
                        speed = speed,
                        onSelect = { selectedGear = it }
                    )
                }
            }

            // -----------------------------------------------------------------------------------
            // LEFT
            // -----------------------------------------------------------------------------------
            "Left" -> {
                Row(Modifier.fillMaxSize()) {

                    GearColumnLeft(
                        selected = selectedGear,
                        speed = speed,
                        onSelect = { selectedGear = it }
                    )

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Joystick(
                            knobOffset = knobOffset,
                            sizeMultiplier = sizeMultiplier,
                            onDrag = { knobOffset = it },
                            onReset = { knobOffset = Offset.Zero },
                            radiusChanged = { joystickRadius = it }
                        )
                    }
                }
            }

            // -----------------------------------------------------------------------------------
            // RIGHT (DEFAULT)
            // -----------------------------------------------------------------------------------
            else -> {
                Row(Modifier.fillMaxSize()) {

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Joystick(
                            knobOffset = knobOffset,
                            sizeMultiplier = sizeMultiplier,
                            onDrag = { knobOffset = it },
                            onReset = { knobOffset = Offset.Zero },
                            radiusChanged = { joystickRadius = it }
                        )
                    }

                    GearColumnRight(
                        selected = selectedGear,
                        speed = speed,
                        onSelect = { selectedGear = it }
                    )
                }
            }
        }
    }
}

// =========================================================================================
// JOYSTICK
// =========================================================================================
@Composable
fun Joystick(
    knobOffset: Offset,
    sizeMultiplier: Float,
    onDrag: (Offset) -> Unit,
    onReset: () -> Unit,
    radiusChanged: (Float) -> Unit,
) {
    val baseSizeDp = 250.dp
    val canvasSizeDp = baseSizeDp * sizeMultiplier

    val baseColor = Brush.radialGradient(
        listOf(Color(0xFF3E3E3E), Color(0xFF1A1A1A))
    )
    val knobColor = Brush.radialGradient(
        listOf(Color(0xFFE34FF2), Color(0xFFA10DB0))
    )

    Canvas(
        modifier = Modifier
            .size(canvasSizeDp)
            .pointerInput(sizeMultiplier) {
                detectDragGestures(
                    onDragEnd = { onReset() },
                    onDrag = { _, dragAmount ->
                        val center = Offset(size.width / 2f, size.height / 2f)
                        val newOffset = Offset(
                            (knobOffset.x + dragAmount.x).coerceIn(-center.x, center.x),
                            (knobOffset.y + dragAmount.y).coerceIn(-center.y, center.y)
                        )
                        onDrag(newOffset)
                    }
                )
            }
    ) {
        val center = Offset(size.width / 2, size.height / 2)
        val radius = size.minDimension / 2
        radiusChanged(radius)

        drawCircle(
            brush = baseColor,
            radius = radius,
            center = center
        )

        drawCircle(
            brush = knobColor,
            radius = radius / 3f,
            center = center + knobOffset
        )
    }
}

// =========================================================================================
// GEAR SHIFT UI — TOP
// =========================================================================================
@Composable
fun GearRowTop(selected: Int, speed: Int, onSelect: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        Row(horizontalArrangement = Arrangement.Center) {
            (1..3).forEach { gear ->
                GearButton(gear, gear == selected) { onSelect(gear) }
                Spacer(Modifier.width(12.dp))
            }
        }

        Text("Speed: $speed%", color = Color.White, modifier = Modifier.padding(top = 8.dp))
    }
}

// =========================================================================================
// GEAR SHIFT UI — BOTTOM
// =========================================================================================
@Composable
fun GearRowBottom(selected: Int, speed: Int, onSelect: (Int) -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 24.dp)
    ) {

        Text("Speed: $speed%", color = Color.White, modifier = Modifier.padding(bottom = 8.dp))

        Row(horizontalArrangement = Arrangement.Center) {
            (1..3).forEach { gear ->
                GearButton(gear, gear == selected) { onSelect(gear) }
                Spacer(Modifier.width(12.dp))
            }
        }
    }
}

// =========================================================================================
// GEAR SHIFT — LEFT
// =========================================================================================
@Composable
fun GearColumnLeft(selected: Int, speed: Int, onSelect: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .padding(start = 16.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center
    ) {
        (1..3).forEach { gear ->
            GearButton(gear, gear == selected) { onSelect(gear) }
            Spacer(Modifier.height(12.dp))
        }

        Text("Speed: $speed%", color = Color.White, modifier = Modifier.padding(top = 16.dp))
    }
}

// =========================================================================================
// GEAR SHIFT — RIGHT
// =========================================================================================
@Composable
fun GearColumnRight(selected: Int, speed: Int, onSelect: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .padding(end = 16.dp)
            .fillMaxHeight(),
        verticalArrangement = Arrangement.Center
    ) {

        (1..3).forEach { gear ->
            GearButton(gear, gear == selected) { onSelect(gear) }
            Spacer(Modifier.height(12.dp))
        }

        Text("Speed: $speed%", color = Color.White)
    }
}

// =========================================================================================
// SHARED GEAR BUTTON
// =========================================================================================
@Composable
fun GearButton(gear: Int, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .size(70.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            if (isSelected) Color(0xFFE34FF2) else Color.Gray
        ),
        shape = RectangleShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                "Gear $gear",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
