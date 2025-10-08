import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import student.projects.jetpackpam.data.CameraRequest
import student.projects.jetpackpam.data.MotorRequest
import student.projects.jetpackpam.retrofit.PiRetrofitInstance
import kotlin.math.hypot

@Composable
fun VideoScreen() {
    val context = LocalContext.current
    DisposableEffect(Unit) {
        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    val coroutineScope = rememberCoroutineScope()
    var imageUrl by remember { mutableStateOf("http://192.168.137.1:7298/api/camera/stream") }
    var hasFeed by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Refresh live feed every 1 second
    LaunchedEffect(Unit) {
        while (true) {
            imageUrl = "http://192.168.137.1:7298/api/camera/stream?ts=${System.currentTimeMillis()}"
            //delay(1000)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ---------- Left: Motor Joystick ----------
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            JoystickControl(
                onMove = { x, y ->
                    coroutineScope.launch {
                        try {
                            PiRetrofitInstance.api.moveMotors(MotorRequest(x, y, speed = 50))
                            errorMessage = null
                        } catch (e: Exception) {
                            errorMessage = "⚠️ Could not connect to motor API."
                        }
                    }
                },
                onStop = {
                    coroutineScope.launch {
                        try {
                            PiRetrofitInstance.api.stopMotors()
                        } catch (e: Exception) {
                            errorMessage = "⚠️ Failed to stop motors."
                        }
                    }
                }
            )
        }

        // ---------- Center: Video Feed ----------
        Box(
            modifier = Modifier
                .weight(3f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            if (hasFeed) {
                Image(
                    painter = rememberAsyncImagePainter(
                        model = imageUrl,
                        onError = { hasFeed = false; errorMessage = "🚫 No live feed detected." }
                    ),
                    contentDescription = "Camera Feed",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = errorMessage ?: "🚫 No live feed detected.",
                        color = Color.White,
                        modifier = Modifier.padding(16.dp)
                    )
                    IconButton(onClick = {
                        coroutineScope.launch {
                            imageUrl = "http://192.168.137.1:7298/api/camera/stream?ts=${System.currentTimeMillis()}"
                            hasFeed = true
                            errorMessage = null
                        }
                    }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Retry", tint = Color.White)
                    }
                }
            }
        }

        // ---------- Right: Camera Controls ----------
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            CameraControl(
                onMove = { pan, tilt ->
                    coroutineScope.launch {
                        try {
                            PiRetrofitInstance.api.moveCamera(CameraRequest(pan.toFloat(), tilt.toFloat()))
                            errorMessage = null
                        } catch (e: Exception) {
                            errorMessage = "⚠️ Could not connect to camera API."
                        }
                    }
                },
                onReset = {
                    coroutineScope.launch {
                        try {
                            PiRetrofitInstance.api.resetCamera()
                            errorMessage = null
                        } catch (e: Exception) {
                            errorMessage = "⚠️ Reset failed. Camera API unreachable."
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun JoystickControl(
    onMove: (x: Float, y: Float) -> Unit,
    onStop: () -> Unit
) {
    var handlePosition by remember { mutableStateOf(Offset.Zero) }
    val radius = 100f

    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color.DarkGray, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        handlePosition = Offset.Zero
                        onStop()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = handlePosition + dragAmount
                        val distance = hypot(newOffset.x, newOffset.y)
                        handlePosition = if (distance < radius) newOffset
                        else Offset(
                            x = newOffset.x / distance * radius,
                            y = newOffset.y / distance * radius
                        )

                        val normalizedX = (handlePosition.x / radius).coerceIn(-1f, 1f)
                        val normalizedY = (-handlePosition.y / radius).coerceIn(-1f, 1f)
                        onMove(normalizedX, normalizedY)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color.Gray)
            drawCircle(
                Color.Red,
                radius = 20f,
                center = Offset(size.width / 2 + handlePosition.x, size.height / 2 + handlePosition.y)
            )
        }
    }
}

@Composable
fun CameraControl(
    onMove: (pan: Int, tilt: Int) -> Unit,
    onReset: () -> Unit
) {
    var pan by remember { mutableStateOf(0) }
    var tilt by remember { mutableStateOf(0) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.DarkGray.copy(alpha = 0.6f))
            .padding(16.dp)
            .width(150.dp)
    ) {
        Text("Camera Control", color = Color.White)

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { tilt = (tilt + 10).coerceAtMost(90); onMove(pan, tilt) }) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up")
            }
            Button(onClick = { tilt = (tilt - 10).coerceAtLeast(-90); onMove(pan, tilt) }) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { pan = (pan - 10).coerceAtLeast(-90); onMove(pan, tilt) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Left")
            }
            Button(onClick = { pan = (pan + 10).coerceAtMost(90); onMove(pan, tilt) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Right")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = {
            pan = 0
            tilt = 0
            onReset()
        }) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun VideoScreenPreview() {
    VideoScreen()
}
