package student.projects.jetpackpam.screens.firsttimecustom

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

    val uiTexts by remember { derivedStateOf { languageViewModel.uiTexts } }

    var selectedLanguage by remember { mutableStateOf(languageViewModel.selectedLanguage) }

    val languages = listOf("English", "Afrikaans", "isiZulu", "isiXhosa")

    fun languageCode(language: String) = when (language) {
        "Afrikaans" -> "af"
        "isiZulu" -> "zu"
        "isiXhosa" -> "xh"
        else -> "en"
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = uiTexts["header"] ?: "Select preferred language",
                fontStyle = FontStyle.Italic,
                fontSize = 32.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = uiTexts["description"]
                    ?: "Choose the language you prefer for the app",
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                items(languages) { language ->

                    val isSelected = selectedLanguage == language

                    OutlinedCard(
                        onClick = {
                            selectedLanguage = language
                            val code = languageCode(language)

                            scope.launch {
                                languageViewModel.translateAll(code) { translated ->
                                    languageViewModel.setLanguage(
                                        language = language,
                                        code = code,
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
