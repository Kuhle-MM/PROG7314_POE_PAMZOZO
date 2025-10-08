package student.projects.jetpackpam.screens.bottomnavscreen

import androidx.activity.ComponentActivity
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import student.projects.jetpackpam.R
import student.projects.jetpackpam.localization.t
import student.projects.jetpackpam.util.DeviceConfiguration
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun HomeScreen(navController: NavHostController, onSignOut: () -> Unit) {

    // --- Animation states ---
    var showLogo by remember { mutableStateOf(false) }
    var showText by remember { mutableStateOf(false) }
    var showButton by remember { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition()
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    LaunchedEffect(Unit) {
        showLogo = true
        delay(300)
        showText = true
        delay(300)
        showButton = true
    }

    // --- Adaptive sizing ---
    val activity = LocalContext.current as ComponentActivity
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    val deviceConfig = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

    val logoSize = when (deviceConfig) {
        DeviceConfiguration.MOBILE_PORTRAIT -> 200.dp
        DeviceConfiguration.MOBILE_LANDSCAPE -> 150.dp
        DeviceConfiguration.TABLET_PORTRAIT -> 250.dp
        DeviceConfiguration.TABLET_LANDSCAPE -> 300.dp
        DeviceConfiguration.DESKTOP -> 350.dp
    }

    val verticalPadding = when (deviceConfig) {
        DeviceConfiguration.MOBILE_PORTRAIT -> 150.dp
        DeviceConfiguration.MOBILE_LANDSCAPE -> 80.dp
        DeviceConfiguration.TABLET_PORTRAIT -> 180.dp
        DeviceConfiguration.TABLET_LANDSCAPE -> 120.dp
        DeviceConfiguration.DESKTOP -> 200.dp
    }

    val headerFont = when (deviceConfig) {
        DeviceConfiguration.MOBILE_PORTRAIT -> 20.sp
        DeviceConfiguration.MOBILE_LANDSCAPE -> 18.sp
        DeviceConfiguration.TABLET_PORTRAIT -> 24.sp
        DeviceConfiguration.TABLET_LANDSCAPE -> 28.sp
        DeviceConfiguration.DESKTOP -> 32.sp
    }

    val messageFont = when (deviceConfig) {
        DeviceConfiguration.MOBILE_PORTRAIT -> 16.sp
        DeviceConfiguration.MOBILE_LANDSCAPE -> 14.sp
        DeviceConfiguration.TABLET_PORTRAIT -> 18.sp
        DeviceConfiguration.TABLET_LANDSCAPE -> 20.sp
        DeviceConfiguration.DESKTOP -> 22.sp
    }

    // --- Layout ---
    if (deviceConfig == DeviceConfiguration.MOBILE_PORTRAIT ||
        deviceConfig == DeviceConfiguration.MOBILE_LANDSCAPE
    ) {
        // --- Column layout for mobile ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .padding(top = verticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AnimatedVisibility(
                visible = showLogo,
                enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { it / 2 }
            ) {
                Image(
                    painter = painterResource(R.drawable.pamicon),
                    contentDescription = "PAM",
                    modifier = Modifier.size(logoSize).offset(y = floatAnim.dp)
                )
            }

            AnimatedVisibility(
                visible = showText,
                enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { it / 4 }
            ) {
                Text(
                    text = t("Welcome"),
                    fontSize = messageFont,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }

            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn(tween(600)) + scaleIn(tween(600), initialScale = 0.8f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    ElevatedButton(onClick = { navController.navigate("chat") }) {
                        Icon(Icons.Default.ChatBubbleOutline, contentDescription = t("chat"))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(t("chat"))
                    }
                }
            }
        }
    } else {
        // --- Row layout for tablets & desktop ---
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                AnimatedVisibility(
                    visible = showLogo,
                    enter = fadeIn(tween(800)) + slideInVertically(tween(800)) { it / 2 }
                ) {
                    Image(
                        painter = painterResource(R.drawable.pamicon),
                        contentDescription = "PAM",
                        modifier = Modifier.size(logoSize).offset(y = floatAnim.dp)
                    )
                }

                AnimatedVisibility(
                    visible = showText,
                    enter = fadeIn(tween(1000)) + slideInVertically(tween(1000)) { it / 4 }
                ) {
                    Text(
                        text = t("Welcome"),
                        fontSize = messageFont,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily.SansSerif,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = showButton,
                enter = fadeIn(tween(600)) + scaleIn(tween(600), initialScale = 0.8f)
            ) {
                ElevatedButton(
                    onClick = { navController.navigate("chat") },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = t("chat"))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(t("chat"))
                }
            }
        }
    }
}
