package student.projects.jetpackpam.screens.firsttimecustom

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import student.projects.jetpackpam.R
import kotlin.math.cos
import kotlin.math.sin


@Composable
fun PamThemeSelectionScreen() {
    var isDarkMode by remember { mutableStateOf(false) }
    var selectedTheme by remember { mutableStateOf(1) }

    // Gradient setup for dark mode
    val angleInRad = -55 * (Math.PI / 180)
    val gradientLength = 1000f
    val start = Offset(
        x = (gradientLength / 2f * (1 - cos(angleInRad))).toFloat(),
        y = (gradientLength / 2f * (1 - sin(angleInRad))).toFloat()
    )
    val end = Offset(
        x = (gradientLength / 2f * (1 + cos(angleInRad))).toFloat(),
        y = (gradientLength / 2f * (1 + sin(angleInRad))).toFloat()
    )

    // Background brush
    val backgroundBrush = if (isDarkMode) {
        Brush.linearGradient(
            colors = listOf(Color(0xFF1B123D), Color(0xFF2F1982), Color(0xFF1B123D)),
            start = start,
            end = end
        )
    } else {
        Brush.linearGradient(listOf(Color.White, Color.White))
    }

    // Images depending on mode
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundBrush)
            .padding(32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Dark mode toggle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isDarkMode) "Dark Mode" else "Light Mode",
                    fontSize = 20.sp,
                    color = if (isDarkMode) Color.White else Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = { isDarkMode = it }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Header
            PamThemeHeader(isDarkMode)

            Spacer(modifier = Modifier.height(24.dp))

            // Vertically aligned images
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

            Spacer(modifier = Modifier.height(50.dp)) // Extra bottom spacing
        }
    }
}

@Composable
fun PamThemeHeader(isDarkMode: Boolean) {
    Text(
        text = "Choose the best colour for me",
        fontStyle = FontStyle.Italic,
        fontSize = 35.sp,
        textAlign = TextAlign.Center,
        color = if (isDarkMode) Color.White else Color.Black
    )
}

@Composable
fun ThemeImage(imageRes: Int, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
            .clickable { onClick() }
            .padding(horizontal = 10.dp)
    ) {
        Image(
            painter = painterResource(id = imageRes),
            contentDescription = "Theme preview",
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
