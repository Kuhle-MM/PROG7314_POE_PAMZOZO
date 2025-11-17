package student.projects.jetpackpam.screens.bottomnavscreen

import android.content.Context
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import student.projects.jetpackpam.R
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.util.DeviceConfiguration
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo

@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthorizationModelViewModel,
    languageViewModel: LanguageViewModel,
    uiTexts: Map<String, String> = emptyMap()
) {

    val context: Context = LocalContext.current

    // Animation State
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

    // Adaptive Layout Setup
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val deviceConfig = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

    val logoSize = when (deviceConfig) {
        DeviceConfiguration.MOBILE_PORTRAIT -> 200.dp
        DeviceConfiguration.MOBILE_LANDSCAPE -> 150.dp
        DeviceConfiguration.TABLET_PORTRAIT -> 250.dp
        DeviceConfiguration.TABLET_LANDSCAPE -> 300.dp
        DeviceConfiguration.DESKTOP -> 350.dp
    }

    val messageFont = when (deviceConfig) {
        DeviceConfiguration.MOBILE_PORTRAIT -> 16.sp
        DeviceConfiguration.MOBILE_LANDSCAPE -> 14.sp
        DeviceConfiguration.TABLET_PORTRAIT -> 18.sp
        DeviceConfiguration.TABLET_LANDSCAPE -> 20.sp
        DeviceConfiguration.DESKTOP -> 22.sp
    }

    // ROOT CONTENT
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        // SIGN OUT BUTTON
        Button(onClick = {
            authViewModel.signOutSafely(
                context = context,
                navController = navController,
                authViewModel = authViewModel
            )
        }) {
            Text(uiTexts["signOut"] ?: "Sign Out")
        }

        // FLOATING LOGO ANIMATION
        AnimatedVisibility(
            visible = showLogo,
            enter = fadeIn(tween(800)) + slideInVertically { it / 2 }
        ) {
            Image(
                painter = painterResource(R.drawable.pamicon),
                contentDescription = uiTexts["appLogo"] ?: "PAM",
                modifier = Modifier.size(logoSize).offset(y = floatAnim.dp)
            )
        }

        // WELCOME TEXT
        AnimatedVisibility(
            visible = showText,
            enter = fadeIn(tween(1000)) + slideInVertically { it / 4 }
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = uiTexts["welcome"] ?: "Welcome",
                    fontSize = messageFont,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = uiTexts["welcomeMessage"]
                        ?: "I’m ready to help you.\nType below or say the word.",
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.SansSerif
                )
            }
        }

        // CHAT BUTTON
        AnimatedVisibility(
            visible = showButton,
            enter = fadeIn(tween(600)) + scaleIn(tween(600), initialScale = 0.8f)
        ) {
            ElevatedButton(onClick = { navController.navigate("chat") }) {
                Icon(Icons.Default.ChatBubbleOutline, contentDescription = "Chat")
                Spacer(modifier = Modifier.width(8.dp))
                Text(uiTexts["chatButton"] ?: "Chat")
            }
        }
    }
}
