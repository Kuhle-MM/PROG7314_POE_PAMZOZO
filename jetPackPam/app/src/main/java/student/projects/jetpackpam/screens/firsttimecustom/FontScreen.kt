package student.projects.jetpackpam.screens.firsttimecustom


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import student.projects.jetpackpam.models.LanguageViewModel

@Composable
fun FontSelectionScreen(languageViewModel: LanguageViewModel) {
    var sliderValue by remember { mutableFloatStateOf(languageViewModel.fontSize) }
    val uiTexts by remember { derivedStateOf { languageViewModel.uiTexts } }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 24.dp)
        ) {

            Slider(
                value = sliderValue,
                onValueChange = { newValue ->
                    sliderValue = newValue                   // update UI state
                    languageViewModel.updateFontSize(newValue) // update global font size
                },
                valueRange = 16f..48f,
                steps = 32
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = uiTexts["fontPreview"] ?: "Font Preview",
                fontSize = sliderValue.sp
            )
        }
    }
}

