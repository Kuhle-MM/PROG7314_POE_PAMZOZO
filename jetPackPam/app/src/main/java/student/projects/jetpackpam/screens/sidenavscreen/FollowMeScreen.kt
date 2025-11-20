package student.projects.jetpackpam.screens.sidenavscreen

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.AdvertiseCallback
import android.bluetooth.le.AdvertiseData
import android.bluetooth.le.AdvertiseSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.ParcelUuid
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import androidx.compose.animation.core.*
import androidx.compose.ui.text.style.TextAlign

@Composable
fun RadioWaveAnimation(
    isActive: Boolean,
    waveColor: Color = Color(0xFFA10DB0),
    pulseColor: Color = Color(0xFFEA80FC)
) {
    if (!isActive) return

    val transition = rememberInfiniteTransition(label = "waveTransition")

    // Each wave has different start offsets
    val waves = listOf(0, 600, 1200)

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(300.dp)) {
        waves.forEach { delay ->
            val scale by transition.animateFloat(
                initialValue = 0.6f,
                targetValue = 2.5f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = LinearEasing, delayMillis = delay),
                    repeatMode = RepeatMode.Restart
                ),
                label = "scale"
            )

            val alpha by transition.animateFloat(
                initialValue = 0.5f,
                targetValue = 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1800, easing = LinearEasing, delayMillis = delay),
                    repeatMode = RepeatMode.Restart
                ),
                label = "alpha"
            )

            Box(
                modifier = Modifier
                    .size(340.dp)
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        this.alpha = alpha
                    }
                    .background(waveColor.copy(alpha = 0.3f), CircleShape)
            )
        }

        // Central pulse
        val pulseScale by transition.animateFloat(
            initialValue = 1f,
            targetValue = 1.1f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse"
        )

        Box(
            modifier = Modifier
                .size(280.dp)
                .graphicsLayer {
                    scaleX = pulseScale
                    scaleY = pulseScale
                }
                .background(pulseColor.copy(alpha = 0.4f), CircleShape)
        )
    }
}

@Composable
fun FollowMeScreen(context: Context, navController: NavController) {
    val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter: BluetoothAdapter? = bluetoothManager.adapter
    var isAdvertising by remember { mutableStateOf(false) }

    val blePermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (!granted) Toast.makeText(context, "Permissions denied", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) { permissionLauncher.launch(blePermissions) }

    fun hasBlePermissions(): Boolean =
        blePermissions.all { ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }

    val advertiseCallback = remember {
        object : AdvertiseCallback() {
            override fun onStartSuccess(settingsInEffect: AdvertiseSettings) { isAdvertising = true }
            override fun onStartFailure(errorCode: Int) { isAdvertising = false }
        }
    }

    fun startAdvertising() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) return
        if (!hasBlePermissions()) return

        val advertiser = bluetoothAdapter.bluetoothLeAdvertiser ?: return
        val settings = AdvertiseSettings.Builder()
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setConnectable(false).build()

        val data = AdvertiseData.Builder()
            .addServiceUuid(ParcelUuid.fromString("12345678-1234-1234-1234-123456789ABC"))
            .setIncludeDeviceName(false).build()

        advertiser.startAdvertising(settings, data, advertiseCallback)
    }

    fun stopAdvertising() {
        bluetoothAdapter?.bluetoothLeAdvertiser?.stopAdvertising(advertiseCallback)
        isAdvertising = false
    }

    DisposableEffect(Unit) { onDispose { stopAdvertising() } }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            RadioWaveAnimation(isActive = isAdvertising)

            Button(
                onClick = { if (!isAdvertising) startAdvertising() else stopAdvertising() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isAdvertising) Color(0xFFA10DB0) else Color(0xFFF0A1F8)
                ),
                modifier = Modifier.size(240.dp),
                shape = CircleShape
            ) {
                Text(
                    text = if (isAdvertising) "Following" else "Activate",
                    fontSize = 20.sp,
                    color = if (isAdvertising) Color.White else Color.Black,
                    textAlign = TextAlign.Center,

                )
            }
        }
    }
}
