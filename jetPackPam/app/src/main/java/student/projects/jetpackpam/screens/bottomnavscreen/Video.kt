import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    var imageUrl by remember { mutableStateOf("http://10.0.2.2:5000/api/camera/stream") }
    var hasFeed by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Refresh live feed every 1 second
    LaunchedEffect(Unit) {
        while (true) {
            try {
                imageUrl = "http://10.0.2.2:5000/api/camera/stream?ts=${System.currentTimeMillis()}"
                hasFeed = true
                errorMessage = null
            } catch (e: Exception) {
                hasFeed = false
                errorMessage = "⚠️ Unable to refresh video feed. Check connection."
            }
            delay(1000)
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
                            val leftSpeed = (y + x).coerceIn(-100, 100)
                            val rightSpeed = (y - x).coerceIn(-100, 100)
                            PiRetrofitInstance.api.moveMotors(
                                MotorRequest(
                                    x = x,
                                    y = y,
                                    leftMotorSpeed = leftSpeed,
                                    rightMotorSpeed = rightSpeed
                                )
                            )
                            errorMessage = null
                        } catch (e: Exception) {
                            errorMessage = "⚠️ Could not connect to motor API."
                        }
                    }
                },
                onStop = {
                    coroutineScope.launch {
                        try {
                            PiRetrofitInstance.api.moveMotors(MotorRequest(0, 0, 0, 0))
                        } catch (e: Exception) {
                            errorMessage = "⚠️ Failed to stop motors. API not responding."
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
                // Fallback when no live feed detected
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
                            try {
                                imageUrl = "http://10.0.2.2:5000/api/camera/stream?ts=${System.currentTimeMillis()}"
                                hasFeed = true
                                errorMessage = null
                            } catch (e: Exception) {
                                errorMessage = "⚠️ Failed to reconnect to feed."
                            }
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
                            PiRetrofitInstance.api.moveCamera(CameraRequest(pan, tilt))
                            errorMessage = null
                        } catch (e: Exception) {
                            errorMessage = "⚠️ Could not connect to camera API."
                        }
                    }
                },
                onReset = {
                    coroutineScope.launch {
                        try {
                            PiRetrofitInstance.api.moveCamera(CameraRequest(pan = 0, tilt = 0))
                            errorMessage = null
                        } catch (e: Exception) {
                            errorMessage = "⚠️ Reset failed. Camera API unreachable."
                        }
                    }
                }
            )
        }
    }

    // ---------- Overlay Error Message ----------
//    errorMessage?.let {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(top = 8.dp)
//                .background(Color(0xAA000000)),
//            contentAlignment = Alignment.Center
//        ) {
//            Text(text = it, color = Color.Red, modifier = Modifier.padding(8.dp))
//        }
//    }
}

@Composable
fun JoystickControl(
    onMove: (x: Int, y: Int) -> Unit,
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
                        if (distance < radius) {
                            handlePosition = newOffset
                        }
                        val normalizedX = (newOffset.x / radius * 100).toInt()
                        val normalizedY = (-newOffset.y / radius * 100).toInt()
                        onMove(normalizedX, normalizedY)
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color.Gray)
            drawCircle(Color.Red, radius = 20f, center = Offset(
                size.width / 2 + handlePosition.x,
                size.height / 2 + handlePosition.y
            ))
        }
    }
}

//Camera control with two sliders (Pan and Tilt) and reset button.
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
                //Text("🔼")
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up")
            }
            Button(onClick = { tilt = (tilt - 10).coerceAtLeast(-90); onMove(pan, tilt) }) {
                //Text("🔽")
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { pan = (pan - 10).coerceAtLeast(-90); onMove(pan, tilt) }) {
                //Text("◀️")
                Icon(Icons.Default.ChevronLeft, contentDescription = "Left")
            }
            Button(onClick = { pan = (pan + 10).coerceAtMost(90); onMove(pan, tilt) }) {
                //Text("▶️")
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
// -------------------- PREVIEW --------------------

@Preview(showBackground = true)
@Composable
fun VideoScreenPreview() {
    VideoScreen()
}