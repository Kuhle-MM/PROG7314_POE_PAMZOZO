package student.projects.jetpackpam

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Build
import android.util.Log
import android.view.ViewGroup
import android.webkit.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import kotlin.math.hypot

// ==========================================
// 1. MASTER SETTINGS
// ==========================================
const val MASTER_IP = "192.168.137.250" // <--- CHECK THIS IP
const val TOKEN = "mySuperSecretRobotKey123"

const val VIDEO_URL = "http://$MASTER_IP:8000/index.html?token=$TOKEN"
const val MOTOR_URL = "http://$MASTER_IP:5000/"

// ==========================================
// 2. NETWORK LOGIC
// ==========================================
data class MotorRequest(val x: Double, val y: Double, val speed: Int = 50)
data class CameraRequest(val pan: Float, val tilt: Float)

interface InternalRobotApi {
    @POST("/api/joystick")
    suspend fun moveMotors(@Body request: MotorRequest)
    @POST("/api/command")
    suspend fun stopMotors(@Body request: Map<String, String>)
    @POST("/camera/move")
    suspend fun moveCamera(@Body request: CameraRequest)
    @POST("/camera/reset")
    suspend fun resetCamera()
}

object RobotNetwork {
    val api: InternalRobotApi by lazy {
        Retrofit.Builder()
            .baseUrl(MOTOR_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(InternalRobotApi::class.java)
    }
}

// ==========================================
// 3. THE PRETTY UI
// ==========================================
@Composable
fun VideoScreen() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // ==========================================
    // FORCE LANDSCAPE & FULL SCREEN (IMMERSIVE)
    // ==========================================
    DisposableEffect(Unit) {
        val activity = context as? Activity ?: return@DisposableEffect onDispose {}
        val window = activity.window

        // 1. Remember original orientation
        val originalOrientation = activity.requestedOrientation

        // 2. Force Landscape
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        // 3. Hide System Bars (The Magic Part)
        // This uses the modern WindowInsetsController API
        val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, window.decorView)

        insetsController.apply {
            hide(androidx.core.view.WindowInsetsCompat.Type.systemBars()) // Hide Status & Nav bars
            systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            // RESET EVERYTHING WHEN LEAVING THE SCREEN
            activity.requestedOrientation = originalOrientation
            insetsController.show(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        }
    }

    // Video WebView
    val webView = remember {
        WebView(context).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            settings.apply {
                javaScriptEnabled = true
                loadWithOverviewMode = true
                useWideViewPort = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_NO_CACHE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) safeBrowsingEnabled = false
            }
            setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
            setBackgroundColor(0x00000000)
            loadUrl(VIDEO_URL)
        }
    }
    DisposableEffect(Unit) { onDispose { webView.destroy() } }

    fun sendCommand(action: suspend () -> Unit) {
        coroutineScope.launch { try { action() } catch (e: Exception) { Log.e("PAM", "Cmd Fail: ${e.message}") } }
    }

    // MAIN LAYOUT: STACK (Video at back, Controls on top)
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // LAYER 1: Full Screen Video
        AndroidView(factory = { webView }, modifier = Modifier.fillMaxSize())

        // LAYER 2: Controls Overlay
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp), // Keep controls away from screen edges
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {

            // LEFT: JOYSTICK PANEL
            GlassPanel {
                JoystickControl(
                    onMove = { x, y ->
                        Log.d("PAM", "Joy: $x, $y")
                        sendCommand { RobotNetwork.api.moveMotors(MotorRequest(-    x.toDouble(), -y.toDouble())) }
                    },
                    onStop = {
                        sendCommand { RobotNetwork.api.stopMotors(mapOf("cmd" to "stop")) }
                    }
                )
            }

            // RIGHT: CAMERA D-PAD PANEL
            GlassPanel {
                CameraDPad(
                    onMove = { pan, tilt ->
                        sendCommand { RobotNetwork.api.moveCamera(CameraRequest(pan.toFloat(), tilt.toFloat())) }
                    },
                    onReset = {
                        sendCommand { RobotNetwork.api.resetCamera() }
                    }
                )
            }
        }
    }
}

// ==========================================
// 4. CUSTOM COMPONENTS
// ==========================================

// A reusable semi-transparent container
@Composable
fun GlassPanel(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.5f)) // See-through dark background
            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(24.dp)) // Subtle border
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun JoystickControl(onMove: (x: Float, y: Float) -> Unit, onStop: () -> Unit) {
    var handlePos by remember { mutableStateOf(Offset.Zero) }
    val radius = 80f
    val stickColor = Color(0xFFE91E63) // Hot Pink/Red
    val baseColor = Color.DarkGray

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Text Label
        Text("MOTORS", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(8.dp))

        // The Joystick
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(baseColor, CircleShape)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = { handlePos = Offset.Zero; onStop() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val newOffset = handlePos + dragAmount
                            val dist = hypot(newOffset.x, newOffset.y)
                            handlePos = if (dist < radius) newOffset else Offset(
                                newOffset.x / dist * radius,
                                newOffset.y / dist * radius
                            )
                            onMove((handlePos.x / radius).coerceIn(-1f, 1f), (handlePos.y / radius).coerceIn(-1f, 1f))
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // Base Grid Lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(Color.Gray, Offset(size.width/2, 0f), Offset(size.width/2, size.height), 2f)
                drawLine(Color.Gray, Offset(0f, size.height/2), Offset(size.width, size.height/2), 2f)
            }
            // The Stick Handle
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawCircle(stickColor, radius = 40f, center = Offset(size.width / 2 + handlePos.x, size.height / 2 + handlePos.y))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // The Requested RESET / STOP Button
        Button(
            onClick = { handlePos = Offset.Zero; onStop() },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.8f)),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            Icon(Icons.Rounded.StopCircle, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("STOP")
        }
    }
}

@Composable
fun CameraDPad(onMove: (pan: Int, tilt: Int) -> Unit, onReset: () -> Unit) {
    var pan by remember { mutableIntStateOf(90) }
    var tilt by remember { mutableIntStateOf(45) }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("CAMERA", color = Color.White.copy(alpha = 0.7f), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
        Spacer(modifier = Modifier.height(12.dp))

        // TOP BUTTON
        ArrowButton(Icons.Default.KeyboardArrowUp) { tilt = (tilt - 10).coerceAtLeast(0); onMove(pan, tilt) }

        Row(verticalAlignment = Alignment.CenterVertically) {
            // LEFT BUTTON
            ArrowButton(Icons.Default.ChevronLeft) { pan = (pan + 10).coerceAtMost(180); onMove(pan, tilt) }

            // CENTER RESET BUTTON
            FilledIconButton(
                onClick = { pan = 90; tilt = 45; onReset() },
                modifier = Modifier.size(56.dp).padding(4.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = Color(0xFF2196F3)) // Blue
            ) {
                Icon(Icons.Rounded.CenterFocusStrong, contentDescription = "Reset", tint = Color.White)
            }

            // RIGHT BUTTON
            ArrowButton(Icons.Default.ChevronRight) { pan = (pan - 10).coerceAtLeast(0); onMove(pan, tilt) }
        }

        // BOTTOM BUTTON
        ArrowButton(Icons.Default.KeyboardArrowDown) { tilt = (tilt + 10).coerceAtMost(90); onMove(pan, tilt) }
    }
}

@Composable
fun ArrowButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    FilledTonalIconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp).padding(2.dp),
        colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Color.White.copy(alpha = 0.2f))
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
    }
}