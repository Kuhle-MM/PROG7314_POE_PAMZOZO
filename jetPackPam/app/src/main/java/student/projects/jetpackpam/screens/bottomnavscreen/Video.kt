import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Refresh
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

    // Refresh live feed every 1 second
    LaunchedEffect(Unit) {
        while (true) {
            imageUrl = "http://10.0.2.2:5000/api/camera/stream?ts=${System.currentTimeMillis()}"
            delay(1000)
        }
    }

    // Landscape layout
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left - Joystick control for motors
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
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                onStop = {
                    coroutineScope.launch {
                        try {
                            PiRetrofitInstance.api.moveMotors(MotorRequest(0, 0, 0, 0))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            )
        }

        // Center - Live video feed
        Box(
            modifier = Modifier
                .weight(3f)
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = rememberAsyncImagePainter(imageUrl),
                contentDescription = "Camera Feed",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Right - Camera control for pan/tilt
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
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                onReset = {
                    coroutineScope.launch {
                        try {
                            PiRetrofitInstance.api.moveCamera(CameraRequest(pan = 0, tilt = 0))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            )
        }
    }
}

// -------------------- JOYSTICK CONTROL --------------------

@Composable
fun JoystickControl(
    modifier: Modifier = Modifier,
    onMove: (Int, Int) -> Unit,
    onStop: () -> Unit
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    Box(
        modifier = modifier
            .size(150.dp)
            .clip(CircleShape)
            .background(Color.DarkGray.copy(alpha = 0.4f))
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        offsetX = 0f
                        offsetY = 0f
                        onStop()
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount.x).coerceIn(-60f, 60f)
                        offsetY = (offsetY + dragAmount.y).coerceIn(-60f, 60f)
                        onMove(offsetX.toInt(), offsetY.toInt())
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .offset(x = offsetX.dp, y = offsetY.dp)
                .clip(CircleShape)
                .background(Color.White)
        )
    }
}

// -------------------- CAMERA CONTROL --------------------

@Composable
fun CameraControl(
    modifier: Modifier = Modifier,
    onMove: (Int, Int) -> Unit,
    onReset: () -> Unit
) {
    var pan by remember { mutableStateOf(0) }
    var tilt by remember { mutableStateOf(0) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = {
                tilt = (tilt + 10).coerceIn(-90, 90)
                onMove(pan, tilt)
            }
        ) {
            Icon(Icons.Default.CenterFocusStrong, contentDescription = "Tilt Up", tint = Color.White)
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            IconButton(onClick = {
                pan = (pan - 10).coerceIn(-90, 90)
                onMove(pan, tilt)
            }) {
                Text("◀", color = Color.White)
            }

            IconButton(onClick = onReset) {
                Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.Red)
            }

            IconButton(onClick = {
                pan = (pan + 10).coerceIn(-90, 90)
                onMove(pan, tilt)
            }) {
                Text("▶", color = Color.White)
            }
        }

        IconButton(
            onClick = {
                tilt = (tilt - 10).coerceIn(-90, 90)
                onMove(pan, tilt)
            }
        ) {
            Text("▼", color = Color.White)
        }
    }
}

// -------------------- PREVIEW --------------------

@Preview(showBackground = true)
@Composable
fun VideoScreenPreview() {
    VideoScreen()
}