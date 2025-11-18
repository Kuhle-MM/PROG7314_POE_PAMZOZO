package student.projects.jetpackpam

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import student.projects.jetpackpam.data.CameraRequest
import student.projects.jetpackpam.data.MotorRequest
import student.projects.jetpackpam.retrofit.PiRetrofitInstance
import kotlin.math.hypot


// CONFIGURATION
const val ROBOT_IP = "192.168.137.250" // Ensure this is the Pi's IP for Video
const val TOKEN = "mySuperSecretRobotKey123"
const val STREAM_URL = "http://$ROBOT_IP:8000/index.html?token=$TOKEN"


@Composable
fun VideoScreen() {
    val context = LocalContext.current


    // Force Landscape Mode
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }


    val coroutineScope = rememberCoroutineScope()


    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // -------- Left: Joystick --------
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
                            // FIXED: Inverted Y to match standard cartesian coordinates
                            // Using Double to match C# precision
                            val request = MotorRequest(x.toDouble(), -y.toDouble())
                            PiRetrofitInstance.api.moveMotors(request)
                        } catch (e: Exception) {
                            println("Error sending move command: ${e.message}")
                        }
                    }
                },
                onStop = {
                    coroutineScope.launch {
                        try {
                            PiRetrofitInstance.api.stopMotors(mapOf("cmd" to "stop"))
                        } catch (e: Exception) {
                            println("Error sending stop command: ${e.message}")
                        }
                    }
                }
            )
        }


        // -------- Center: MJPEG Video Stream (WebView) --------
        Box(
            modifier = Modifier
                .weight(3f)
                .fillMaxHeight()
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.loadWithOverviewMode = true
                        settings.useWideViewPort = true
                        settings.javaScriptEnabled = true
                        setBackgroundColor(0x00000000) // Transparent
                        webViewClient = WebViewClient()
                        loadUrl(STREAM_URL)
                    }
                },
                update = { webView ->
                    if (webView.url != STREAM_URL) {
                        webView.loadUrl(STREAM_URL)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }


        // -------- Right: Camera Controls --------
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
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                },
                onReset = {
                    coroutineScope.launch {
                        try {
                            PiRetrofitInstance.api.resetCamera()
                        } catch (e: Exception) {
                            e.printStackTrace()
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


    // --- CRITICAL FIX: Throttling ---
    // We only allow a network request every 100ms
    var lastSendTime by remember { mutableStateOf(0L) }


    Box(
        modifier = Modifier
            .size(200.dp)
            .background(Color.DarkGray, CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragEnd = {
                        handlePosition = Offset.Zero
                        onStop() // Stop always fires immediately
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
                        val normalizedY = (handlePosition.y / radius).coerceIn(-1f, 1f)


                        // Only trigger callback if 100ms has passed
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastSendTime > 100) {
                            onMove(normalizedX, normalizedY)
                            lastSendTime = currentTime
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(Color.Gray)
            drawCircle(
                Color.Red,
                radius = 30f,
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
    var pan by remember { mutableStateOf(90) }
    var tilt by remember { mutableStateOf(45) }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.DarkGray.copy(alpha = 0.6f))
            .padding(16.dp)
            .width(150.dp)
    ) {
        Text("Camera", color = Color.White)
        Spacer(modifier = Modifier.height(8.dp))


        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { tilt = (tilt - 5).coerceAtLeast(0); onMove(pan, tilt) }) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Up")
            }
            Button(onClick = { tilt = (tilt + 5).coerceAtMost(90); onMove(pan, tilt) }) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Down")
            }
        }


        Spacer(modifier = Modifier.height(8.dp))


        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { pan = (pan + 5).coerceAtMost(180); onMove(pan, tilt) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Left")
            }
            Button(onClick = { pan = (pan - 5).coerceAtLeast(0); onMove(pan, tilt) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Right")
            }
        }


        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { pan = 90; tilt = 45; onReset() }) {
            Icon(Icons.Default.Refresh, contentDescription = "Reset")
        }
    }
}
