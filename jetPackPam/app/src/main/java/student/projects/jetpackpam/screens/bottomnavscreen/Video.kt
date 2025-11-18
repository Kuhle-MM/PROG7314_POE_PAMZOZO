package student.projects.jetpackpam

import android.app.Activity
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Build
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import student.projects.jetpackpam.data.CameraRequest
import student.projects.jetpackpam.data.MotorRequest
import student.projects.jetpackpam.retrofit.PiRetrofitInstance
import kotlin.math.hypot

// ---------------------------------------------------------
// CONFIGURATION
// ---------------------------------------------------------
const val ROBOT_IP = "192.168.137.250"
const val TOKEN = "mySuperSecretRobotKey123"
const val STREAM_URL = "http://$ROBOT_IP:8000/index.html?token=$TOKEN"

@Composable
fun VideoScreen() {
    val context = LocalContext.current

    // Force Landscape
    DisposableEffect(Unit) {
        val activity = context as? Activity
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        onDispose { activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED }
    }

    val coroutineScope = rememberCoroutineScope()

    // ERROR STATE: Tracks if the robot is connected
    var isConnected by remember { mutableStateOf(true) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Helper function to safely execute API calls without crashing
    fun safeApiCall(action: suspend () -> Unit) {
        coroutineScope.launch {
            try {
                action()
                // If call succeeds, we assume we are connected
                if (!isConnected) isConnected = true
            } catch (e: Exception) {
                e.printStackTrace()
                // Only flag as disconnected if it's a network error
                isConnected = false
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ================= LEFT: JOYSTICK =================
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp),
            contentAlignment = Alignment.BottomStart
        ) {
            JoystickControl(
                onMove = { x, y ->
                    safeApiCall {
                        // UPDATED: Converts Float to Double and removes 'speed'
                        // We invert Y (-y) because joystick Up is negative in Compose
                        PiRetrofitInstance.api.moveMotors(MotorRequest(x.toDouble(), -y.toDouble()))
                    }
                },
                onStop = {
                    safeApiCall {
                        // Sends the specific command map required by Python
                        PiRetrofitInstance.api.stopMotors(mapOf("cmd" to "stop"))
                    }
                },
                enabled = isConnected
            )
        }

        // ================= CENTER: VIDEO FEED / ERROR SCREEN =================
        Box(
            modifier = Modifier
                .weight(3f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.DarkGray),
            contentAlignment = Alignment.Center
        ) {
            if (isConnected) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            settings.apply {
                                loadWithOverviewMode = true
                                useWideViewPort = true
                                javaScriptEnabled = true
                                cacheMode = WebSettings.LOAD_NO_CACHE
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    safeBrowsingEnabled = false // Prevents ANR
                                }
                            }
                            setBackgroundColor(0x00000000)

                            webViewClient = object : WebViewClient() {
                                override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                                    super.onReceivedError(view, request, error)
                                    isConnected = false
                                }
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    super.onPageStarted(view, url, favicon)
                                    isConnected = true
                                }
                            }
                            loadUrl(STREAM_URL)
                            webViewRef = this
                        }
                    },
                    update = { webView ->
                        if (webView.url != STREAM_URL) webView.loadUrl(STREAM_URL)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // ERROR SCREEN
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Offline",
                        tint = Color.Red,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Robot Disconnected",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isConnected = true
                            webViewRef?.reload()
                            webViewRef?.loadUrl(STREAM_URL)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Retry Connection")
                    }
                }
            }
        }

        // ================= RIGHT: CAMERA CONTROLS =================
        Box(
            modifier = Modifier.weight(1f).fillMaxHeight().padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            CameraControl(
                onMove = { pan, tilt ->
                    safeApiCall {
                        PiRetrofitInstance.api.moveCamera(CameraRequest(pan.toFloat(), tilt.toFloat()))
                    }
                },
                onReset = {
                    safeApiCall {
                        PiRetrofitInstance.api.resetCamera()
                    }
                },
                enabled = isConnected
            )
        }
    }
}

// ---------------------------------------------------------
// SUB-COMPONENTS
// ---------------------------------------------------------

@Composable
fun JoystickControl(
    onMove: (x: Float, y: Float) -> Unit,
    onStop: () -> Unit,
    enabled: Boolean
) {
    var handlePosition by remember { mutableStateOf(Offset.Zero) }
    val radius = 100f
    val baseColor = if (enabled) Color.DarkGray else Color.Gray
    val stickColor = if (enabled) Color.Red else Color.DarkGray

    Box(
        modifier = Modifier
            .size(200.dp)
            .background(baseColor, CircleShape)
            .pointerInput(enabled) {
                if (enabled) {
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
                            val normalizedY = (handlePosition.y / radius).coerceIn(-1f, 1f)
                            onMove(normalizedX, normalizedY)
                        }
                    )
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(baseColor)
            drawCircle(stickColor, radius = 35f, center = Offset(size.width / 2 + handlePosition.x, size.height / 2 + handlePosition.y))
        }
    }
}

@Composable
fun CameraControl(
    onMove: (pan: Int, tilt: Int) -> Unit,
    onReset: () -> Unit,
    enabled: Boolean
) {
    var pan by remember { mutableIntStateOf(90) }
    var tilt by remember { mutableIntStateOf(45) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.background(Color.DarkGray.copy(alpha = 0.8f)).padding(16.dp).width(150.dp)
    ) {
        Text("Camera", color = if (enabled) Color.White else Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { tilt = (tilt - 5).coerceAtLeast(0); onMove(pan, tilt) }, enabled = enabled) { Icon(Icons.Default.KeyboardArrowUp, "Up") }
            Button(onClick = { tilt = (tilt + 5).coerceAtMost(90); onMove(pan, tilt) }, enabled = enabled) { Icon(Icons.Default.KeyboardArrowDown, "Down") }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { pan = (pan + 5).coerceAtMost(180); onMove(pan, tilt) }, enabled = enabled) { Icon(Icons.Default.ChevronLeft, "Left") }
            Button(onClick = { pan = (pan - 5).coerceAtLeast(0); onMove(pan, tilt) }, enabled = enabled) { Icon(Icons.Default.ChevronRight, "Right") }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Button(onClick = { pan = 90; tilt = 45; onReset() }, enabled = enabled) { Icon(Icons.Default.Refresh, "Reset") }
    }
}

@Preview(showBackground = true, widthDp = 800, heightDp = 400)
@Composable
fun VideoScreenPreview() {
    VideoScreen()
}