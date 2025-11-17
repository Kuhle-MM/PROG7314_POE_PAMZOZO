package student.projects.jetpackpam.screens.firsttimecustom


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import student.projects.jetpackpam.R
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.util.DeviceConfiguration
@Composable
fun PamThemeSelectionScreen(languageViewModel: LanguageViewModel) {
    var isDarkMode by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableIntStateOf(1) }

    val uiTexts by remember { derivedStateOf { languageViewModel.uiTexts } }

    // Gradient setup for dark mode
    val angleInRad = -55 * (Math.PI / 180)
    val gradientLength = 1000f
    val start = androidx.compose.ui.geometry.Offset(
        x = (gradientLength / 2f * (1 - kotlin.math.cos(angleInRad))).toFloat(),
        y = (gradientLength / 2f * (1 - kotlin.math.sin(angleInRad))).toFloat()
    )
    val end = androidx.compose.ui.geometry.Offset(
        x = (gradientLength / 2f * (1 + kotlin.math.cos(angleInRad))).toFloat(),
        y = (gradientLength / 2f * (1 + kotlin.math.sin(angleInRad))).toFloat()
    )

    val backgroundBrush = if (isDarkMode) {
        androidx.compose.ui.graphics.Brush.linearGradient(
            colors = listOf(Color(0xFF1B123D), Color(0xFF2F1982), Color(0xFF1B123D)),
            start = start,
            end = end
        )
    } else {
        androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.White, Color.White))
    }

    val themeImages = if (isDarkMode) {
        listOf(
            R.drawable.darkpamog,
            R.drawable.darkpamblue,
            R.drawable.darkpamgreen
        )
    } else {
        listOf(
            R.drawable.lightpamog,
            R.drawable.lightpamblue,
            R.drawable.lightpamgreen
        )
    }

    val scrollState = rememberScrollState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->

        val rootModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
            .background(brush = backgroundBrush)
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .consumeWindowInsets(WindowInsets.navigationBars)

        val windowSizeClass = androidx.compose.material3.adaptive.currentWindowAdaptiveInfo().windowSizeClass
        val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

        val darkModeText = if (isDarkMode) uiTexts["darkMode"] ?: "Dark Mode"
        else uiTexts["lightMode"] ?: "Light Mode"

        when (deviceConfiguration) {
            DeviceConfiguration.MOBILE_PORTRAIT -> {
                Column(
                    modifier = rootModifier
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = darkModeText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDarkMode) Color.White else Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = isDarkMode,
                            onCheckedChange = { isDarkMode = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    PamThemeHeader(isDarkMode, uiTexts)

                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        themeImages.forEachIndexed { index, imageRes ->
                            ThemeImage(
                                imageRes = imageRes,
                                isSelected = selectedTheme == index + 1,
                                onClick = { selectedTheme = index + 1 }
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(50.dp))
                }
            }

            // MOBILE LANDSCAPE
            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                Row(
                    modifier = rootModifier,
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = darkModeText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { isDarkMode = it }
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                        PamThemeHeader(isDarkMode, uiTexts)
                    }

                    LazyRow(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        items(themeImages.size) { index ->
                            ThemeImage(
                                imageRes = themeImages[index],
                                isSelected = selectedTheme == index + 1,
                                onClick = { selectedTheme = index + 1 }
                            )
                        }
                    }
                }
            }

            // TABLET / DESKTOP
            else -> {
                Row(
                    modifier = rootModifier,
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(0.6f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = darkModeText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (isDarkMode) Color.White else Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Switch(
                                checked = isDarkMode,
                                onCheckedChange = { isDarkMode = it }
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))
                        PamThemeHeader(isDarkMode, uiTexts)
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 220.dp),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp)
                    ) {
                        items(themeImages.size) { index ->
                            ThemeImage(
                                imageRes = themeImages[index],
                                isSelected = selectedTheme == index + 1,
                                onClick = { selectedTheme = index + 1 }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PamThemeHeader(isDarkMode: Boolean, uiTexts: Map<String, String>) {
    Text(
        text = uiTexts["themeHeader"] ?: "Choose the best colour for me",
        fontStyle = FontStyle.Italic,
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center,
        color = if (isDarkMode) Color.White else Color.Black
    )
}
@Composable
fun ThemeImage(imageRes: Int, isSelected: Boolean, onClick: () -> Unit)
{
    Box( modifier = Modifier
    .fillMaxWidth()
    .height(600.dp)
    .clickable { onClick() }
    .padding(horizontal = 10.dp) )
{
        Image( painter = painterResource(id = imageRes),
    contentDescription = "Theme preview",
    contentScale = ContentScale.FillWidth,
    modifier = Modifier.fillMaxWidth())
    }
}