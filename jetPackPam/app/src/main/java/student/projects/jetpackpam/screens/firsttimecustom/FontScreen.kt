package student.projects.jetpackpam.screens.firsttimecustom

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import student.projects.jetpackpam.data.local.SettingsEntity
import student.projects.jetpackpam.data.local.SettingsManager
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.util.DeviceConfiguration

@Composable
fun FontSelectionScreen(languageViewModel: LanguageViewModel) {
    val uiTexts = languageViewModel.uiTexts

    val repo = SettingsManager.repo()

    // Load settings from offline DB
    val settings by repo.settings.collectAsState(initial = SettingsEntity())

    // Coroutine scope for saving to DB
    val scope = rememberCoroutineScope()

    val fontSize = settings.fontSize

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 15f)
            drawCircle(
                color = Color(0xFFF0A1F8),
                radius = 325f,
                center = Offset(x = size.width - 50f, y = 50f),
                style = stroke
            )
            drawCircle(
                color = Color(0xFFFF9BC9),
                radius = 720f,
                center = Offset(x = 50f, y = size.height - 50f),
                style = stroke
            )
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->

        val rootModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(horizontal = 16.dp, vertical = 24.dp)

        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

        val save: (Float) -> Unit = { newSize ->
            scope.launch {
                repo.saveFontSize(newSize)
            }
        }

        when (deviceConfiguration) {

            DeviceConfiguration.MOBILE_PORTRAIT -> {
                Column(
                    modifier = rootModifier,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FontHeader(uiTexts["fontHeader"] ?: "Select your font size")
                    Spacer(modifier = Modifier.height(40.dp))

                    Text(
                        text = uiTexts["preview"] ?: "Preview Text",
                        fontSize = fontSize.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(25.dp))

                    Slider(
                        value = fontSize,
                        onValueChange = save,
                        valueRange = 16f..48f,
                        steps = 36
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
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        FontHeader(uiTexts["fontHeader"] ?: "Select your font size")
                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = uiTexts["preview"] ?: "Preview Text",
                            fontSize = fontSize.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Slider(
                        value = fontSize,
                        onValueChange = save,
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

            else -> {
                Row(
                    modifier = rootModifier,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        FontHeader(uiTexts["fontHeader"] ?: "Select your font size")
                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = uiTexts["preview"] ?: "Preview Text",
                            fontSize = fontSize.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Slider(
                        value = fontSize,
                        onValueChange = save,
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
fun FontHeader(headerText: String) {
    Text(
        text = headerText,
        fontStyle = FontStyle.Italic,
        fontSize = 35.sp
    )
}