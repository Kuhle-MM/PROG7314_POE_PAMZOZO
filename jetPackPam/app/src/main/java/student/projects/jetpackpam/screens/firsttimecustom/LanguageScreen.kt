package student.projects.jetpackpam.screens.firsttimecustom

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import student.projects.jetpackpam.data.LanguageRequest
import student.projects.jetpackpam.retrofit.languageApi

@Composable
fun LanguageSelectionScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val languages = listOf("English", "Afrikaans", "isiZulu", "isiXhosa")

    // Store both the displayed language and the language code
    var selectedLanguage by remember { mutableStateOf("English") }
    var currentLanguageCode by remember { mutableStateOf("en") }

    var uiTexts by remember {
        mutableStateOf(
            mutableMapOf(
                "header" to "Select preferred language",
                "description" to "Choose the language you prefer for the app",
                "buttonNext" to "Next",
                "footer" to "Powered by Pam"
            )
        )
    }

    fun languageCode(language: String): String {
        return when (language) {
            "Afrikaans" -> "af"
            "isiZulu" -> "zu"
            "isiXhosa" -> "xh"
            else -> "en"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp, start = 16.dp, end = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Text(
            text = uiTexts["header"] ?: "Select preferred language",
            fontStyle = FontStyle.Italic,
            fontSize = 32.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Description
        Text(
            text = uiTexts["description"] ?: "Choose the language you prefer for the app",
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Language cards
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(22.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(languages) { language ->
                    val isSelected = selectedLanguage == language

                    OutlinedCard(
                        onClick = {
                            if (selectedLanguage != language) {
                                val newCode = languageCode(language)

                                scope.launch {
                                    try {
                                        val newTexts = mutableMapOf<String, String>()

                                        for ((key, text) in uiTexts) {
                                            // If going back to English, just reset defaults
                                            if (newCode == "en") {
                                                newTexts[key] = when (key) {
                                                    "header" -> "Select preferred language"
                                                    "description" -> "Choose the language you prefer for the app"
                                                    "buttonNext" -> "Next"
                                                    "footer" -> "Powered by Pam"
                                                    else -> text
                                                }
                                                continue
                                            }

                                            // Make API call translating from *previous language* to *new language*
                                            val response = languageApi.translate(
                                                LanguageRequest(
                                                    text = text,
                                                    from = currentLanguageCode,
                                                    to = newCode,
                                                    userId = "2"
                                                )
                                            )

                                            // Use translated text or fallback
                                            newTexts[key] = response.translated?.ifBlank { text } ?: text

                                        }

                                        // Update all states
                                        uiTexts = newTexts
                                        selectedLanguage = language
                                        currentLanguageCode = newCode

                                        Toast.makeText(
                                            context,
                                            "Language switched to $language",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } catch (e: Exception) {
                                        Log.e("LanguageAPI", "Translation error: ${e.message}", e)
                                        Toast.makeText(
                                            context,
                                            "Translation failed: ${e.message}",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Already using $language",
                                    Toast.LENGTH_SHORT
                                ).show()
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
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = language,
                                textAlign = TextAlign.Center,
                                fontSize = 22.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        // Example Button
        Button(
            onClick = {
                Toast.makeText(context, uiTexts["buttonNext"], Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB48CFF))
        ) {
            Text(
                text = uiTexts["buttonNext"] ?: "Next",
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Footer
        Text(
            text = uiTexts["footer"] ?: "Powered by Pam",
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
    }
}
