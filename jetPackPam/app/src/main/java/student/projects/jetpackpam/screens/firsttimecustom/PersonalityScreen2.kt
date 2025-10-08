package student.projects.jetpackpam.screens.firsttimecustom

import android.content.Context

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.util.DeviceConfiguration

@Composable
fun PersonalitySelectionScreen2(languageViewModel: LanguageViewModel) {
    val context = LocalContext.current
    val uiTexts by remember { derivedStateOf { languageViewModel.uiTexts } }

    // Replace static personality names with keys from uiTexts or fallback
    val personalities = listOf(
        uiTexts["sarcastic"] ?: "Sarcastic",
        uiTexts["friendly"] ?: "Friendly",
        uiTexts["genZ"] ?: "Gen Z",
        uiTexts["neverInMood"] ?: "Never in the mood",
        uiTexts["motivationalCoach"] ?: "Motivational Coach",
        uiTexts["wiseElder"] ?: "Wise Elder",
        uiTexts["cheerfulOptimist"] ?: "Cheerful Optimist",
        uiTexts["storyteller"] ?: "Storyteller",
        uiTexts["shakespearean"] ?: "Shakespearean",
        uiTexts["techGeek"] ?: "Tech Geek"
    )

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

        val windowSizeClass = androidx.compose.material3.adaptive.currentWindowAdaptiveInfo().windowSizeClass
        val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

        when (deviceConfiguration) {
            DeviceConfiguration.MOBILE_PORTRAIT -> {
                PersonalityGridLayout(
                    personalities = personalities,
                    context = context,
                    modifier = rootModifier,
                    columns = 2,
                    cardSize = 160.dp,
                    fontSize = 20.sp,
                    horizontalSpacing = 16.dp,
                    verticalSpacing = 16.dp
                )
            }
            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                PersonalityGridLayout(
                    personalities = personalities,
                    context = context,
                    modifier = rootModifier,
                    columns = 3,
                    cardSize = 140.dp,
                    fontSize = 18.sp,
                    horizontalSpacing = 12.dp,
                    verticalSpacing = 12.dp
                )
            }
            else -> {
                PersonalityGridLayout(
                    personalities = personalities,
                    context = context,
                    modifier = rootModifier,
                    columns = 4,
                    cardSize = 200.dp,
                    fontSize = 24.sp,
                    horizontalSpacing = 20.dp,
                    verticalSpacing = 20.dp
                )
            }
        }
    }
}

@Composable
fun PersonalityGridLayout(
    personalities: List<String>,
    context: Context,
    modifier: Modifier,
    columns: Int,
    cardSize: Dp,
    fontSize: TextUnit,
    horizontalSpacing: Dp,
    verticalSpacing: Dp
) {
    TODO("Not yet implemented")
}

@Composable
fun PersonalityHeader2(languageViewModel: LanguageViewModel) {
    val uiTexts by remember { derivedStateOf { languageViewModel.uiTexts } }
    Text(
        text = uiTexts["personalityHeader"] ?: "Choose my personality",
        fontStyle = FontStyle.Italic,
        style = MaterialTheme.typography.titleLarge
    )
}
