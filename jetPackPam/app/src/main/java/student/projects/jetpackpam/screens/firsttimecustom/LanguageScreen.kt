package student.projects.jetpackpam.screens.firsttimecustom

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import student.projects.jetpackpam.models.LanguageViewModel

@Composable
fun LanguageSelectionScreen(languageViewModel: LanguageViewModel) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val uiTexts = languageViewModel.uiTexts
    val selectedLanguage = languageViewModel.selectedLanguage

    val languages = listOf("English", "Afrikaans", "isiZulu", "isiXhosa")

    fun languageCode(lang: String) = when (lang) {
        "Afrikaans" -> "af"
        "isiZulu" -> "zu"
        "isiXhosa" -> "xh"
        else -> "en"
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // -----------------------
        // Canvas background circles
        // -----------------------
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 15f)

            // Top-right circle
            drawCircle(
                color = Color(0xFFF0A1F8),
                radius = 325f,

                center = Offset(x = size.width - 50f, y = 50f),
                style = stroke
            )

            // Bottom-left circle
            drawCircle(
                color = Color(0xFFFF9BC9),
                radius = 720f,
                center = Offset(x = 50f, y = size.height - 50f),
                style = stroke
            )
        }
    }
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = uiTexts["header"] ?: "Select preferred language",
                fontSize = 32.sp,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = uiTexts["description"] ?: "Choose the language you prefer for the app",
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                items(languages.size) { index ->
                    val language = languages[index]
                    val isSelected = selectedLanguage == language

                    OutlinedCard(
                        onClick = {
                            val newCode = languageCode(language)

                            scope.launch {
                                languageViewModel.translateAll(newCode) { translated ->
                                    languageViewModel.setLanguage(
                                        language = language,
                                        code = newCode,
                                        texts = translated
                                    )
                                    languageViewModel.updateTexts(translated)
                                }
                            }
                        },
                        modifier = Modifier.size(150.dp),
                        shape = CircleShape,
                        border = BorderStroke(
                            3.dp,
                            if (isSelected) Color(0xFFB48CFF) else Color(0xFFF0A1F8)
                        ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0x22B48CFF) else Color.Transparent
                        )
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(text = language, fontSize = 22.sp, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // UPDATED BUTTON — NOW SAYS "Set" AND SAVES GLOBALLY
            Button(
                onClick = {
                    Toast.makeText(
                        context,
                        uiTexts["buttonNext"] ?: "Saved",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB48CFF))
            ) {
                Text(text = uiTexts["buttonNext"] ?: "Set", fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = uiTexts["footer"] ?: "Powered by Pam",
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
