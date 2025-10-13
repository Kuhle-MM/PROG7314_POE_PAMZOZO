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
    var sliderValue by remember { mutableFloatStateOf(languageViewModel.fontSize) } // initialize from ViewModel
    val uiTexts by remember { derivedStateOf { languageViewModel.uiTexts } }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->

        val rootModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(horizontal = 16.dp, vertical = 24.dp)
Column (rootModifier){

    var fontSize = 0.0f
    Slider(
        value = fontSize,
        onValueChange = { newValue ->
            fontSize = newValue
            languageViewModel.updateFontSize(newValue) // This updates Typography globally
        },
        valueRange = 16f..48f,
        steps = 32
    )
Spacer(modifier = Modifier.height(10.dp))

    // Preview text
    Text(
        text = uiTexts["fontPreview"] ?: "Font Preview",
        fontSize = sliderValue.sp,

        )

}

    }
}

