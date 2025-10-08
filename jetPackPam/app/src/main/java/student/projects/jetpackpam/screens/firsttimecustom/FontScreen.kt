package student.projects.jetpackpam.screens.firsttimecustom

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import student.projects.jetpackpam.localization.t
import student.projects.jetpackpam.util.DeviceConfiguration

@Composable
fun FontSelectionScreen() {
    var fontSize by remember { mutableFloatStateOf(20f) } // default size in sp

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->

        val rootModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .consumeWindowInsets(WindowInsets.navigationBars)

        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

        // --- Adaptive UI layout ---
        when (deviceConfiguration) {
            DeviceConfiguration.MOBILE_PORTRAIT -> {
                Box(modifier = rootModifier) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 50.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        FontHeader()
                        Spacer(modifier = Modifier.height(40.dp))
                        Text(
                            text = t("Font Preview"),
                            fontSize = fontSize.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Slider(
                        value = fontSize,
                        onValueChange = { fontSize = it },
                        valueRange = 16f..48f,
                        steps = 36,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(16.dp, 50.dp)
                    )
                }
            }

            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                Row(
                    modifier = rootModifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        FontHeader()
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = t("Font Preview"),
                            fontSize = fontSize.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Slider(
                        value = fontSize,
                        onValueChange = { fontSize = it },
                        valueRange = 16f..48f,
                        steps = 36,
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(50.dp)
                            .padding(horizontal = 8.dp)
                            .rotate(270f)
                    )
                }
            }

            else -> {
                Row(
                    modifier = rootModifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        FontHeader()
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = t("Font Preview"),
                            fontSize = fontSize.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Slider(
                        value = fontSize,
                        onValueChange = { fontSize = it },
                        valueRange = 16f..48f,
                        steps = 36,
                        modifier = Modifier
                            .height(250.dp)
                            .width(60.dp)
                            .rotate(270f)
                            .padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun FontHeader() {
    Text(
        text = t("Select your font size for our chat"),
        fontStyle = FontStyle.Italic,
        style = MaterialTheme.typography.bodyLarge
    )
}
