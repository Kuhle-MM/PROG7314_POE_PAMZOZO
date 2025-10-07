package student.projects.jetpackpam.screens.firsttimecustom

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
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
import student.projects.jetpackpam.data.LanguageRequest
import student.projects.jetpackpam.retrofit.languageApi
import student.projects.jetpackpam.util.DeviceConfiguration

@Composable
fun LanguageSelectionScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val languages = listOf("English", "Afrikaans", "isiZulu", "isiXhosa")

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

        when (deviceConfiguration) {
            DeviceConfiguration.MOBILE_PORTRAIT -> {
                Column(
                    modifier = rootModifier
                        .verticalScroll(rememberScrollState())
                        .padding(top = 40.dp),
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

                                                    val response = languageApi.translate(
                                                        LanguageRequest(
                                                            text = text,
                                                            from = currentLanguageCode,
                                                            to = newCode,
                                                            userId = "2"
                                                        )
                                                    )
                                                    newTexts[key] = response.translated?.ifBlank { text } ?: text
                                                }

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

                    Spacer(modifier = Modifier.height(40.dp))

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

                    Text(
                        text = uiTexts["footer"] ?: "Powered by Pam",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }

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
                        Text(
                            text = uiTexts["header"] ?: "Select preferred language",
                            fontStyle = FontStyle.Italic,
                            fontSize = 26.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiTexts["description"]
                                ?: "Choose the language you prefer for the app",
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                Toast.makeText(context, uiTexts["buttonNext"], Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB48CFF))
                        ) {
                            Text(text = uiTexts["buttonNext"] ?: "Next", fontSize = 18.sp)
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(languages) { language ->
                            val isSelected = selectedLanguage == language

                            OutlinedCard(
                                onClick = {
                                    selectedLanguage = language
                                    Toast.makeText(
                                        context,
                                        "Language switched to $language",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier.size(120.dp),
                                shape = CircleShape,
                                border = BorderStroke(
                                    3.dp,
                                    if (isSelected) Color(0xFFB48CFF) else Color(0xFFF0A1F8)
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Color(0x22B48CFF) else Color.Transparent
                                )
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = language,
                                        textAlign = TextAlign.Center,
                                        fontSize = 20.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                Row(
                    modifier = rootModifier,
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = uiTexts["header"] ?: "Select preferred language",
                            fontStyle = FontStyle.Italic,
                            fontSize = 36.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = uiTexts["description"]
                                ?: "Choose the language you prefer for the app",
                            fontSize = 20.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(40.dp))
                        Button(
                            onClick = {
                                Toast.makeText(context, uiTexts["buttonNext"], Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB48CFF))
                        ) {
                            Text(text = uiTexts["buttonNext"] ?: "Next", fontSize = 20.sp)
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 140.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(languages) { language ->
                            val isSelected = selectedLanguage == language

                            OutlinedCard(
                                onClick = {
                                    selectedLanguage = language
                                    Toast.makeText(
                                        context,
                                        "Language switched to $language",
                                        Toast.LENGTH_SHORT
                                    ).show()
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
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = language, fontSize = 22.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
