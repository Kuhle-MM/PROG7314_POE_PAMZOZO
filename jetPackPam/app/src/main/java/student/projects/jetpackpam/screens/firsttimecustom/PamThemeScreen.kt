package student.projects.jetpackpam.screens.firsttimecustom

import android.R.attr.onClick
import android.widget.Button
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun PamThemeSelectionScreen() {
    var isDarkMode by remember { mutableStateOf(false) }

    // Convert angle to offsets for gradient
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = if (isDarkMode) {
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF1B123D), Color(0xFF2F1982), Color(0xFF1B123D)),
                        start = start,
                        end = end
                    )
                } else {
                    Brush.linearGradient(listOf(Color.White, Color.White))
                }
            )

            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            // Toggle switch at the top
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
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
            PamThemeHeader()
        }
    }
}

@Composable
fun PamThemeHeader() {
    Text(
        text = "Choose the best colour for me",
        fontStyle = FontStyle.Italic,
        fontSize = 35.sp,
        textAlign = TextAlign.Center
    )
}
