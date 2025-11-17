package student.projects.jetpackpam.screens.firsttimecustom

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.util.DeviceConfiguration

@Composable
fun PersonalitySelectionScreen2(languageViewModel: LanguageViewModel) {
    val uiTexts = languageViewModel.uiTexts
    val context = LocalContext.current

    // Use uiTexts for personality options
    val personalities = listOf(
        uiTexts["sarcastic"] ?: "Sarcastic",
        uiTexts["friendly"] ?: "Friendly",
        uiTexts["genz"] ?: "Gen Z",
        uiTexts["neverInTheMood"] ?: "Never in the mood",
        uiTexts["motivationalCoach"] ?: "Motivational Coach",
        uiTexts["wiseElder"] ?: "Wise Elder",
        uiTexts["cheerfulOptimist"] ?: "Cheerful Optimist",
        uiTexts["storyTeller"] ?: "Storyteller",
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

        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
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
                    verticalSpacing = 16.dp,
                    headerText = uiTexts["personalityHeader"] ?: "Choose my personality"
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
                    verticalSpacing = 12.dp,
                    headerText = uiTexts["personalityHeader"] ?: "Choose my personality"
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
                    verticalSpacing = 20.dp,
                    headerText = uiTexts["personalityHeader"] ?: "Choose my personality"
                )
            }
        }
    }
}

@Composable
private fun PersonalityGridLayout(
    personalities: List<String>,
    context: Context,
    modifier: Modifier = Modifier,
    columns: Int,
    cardSize: Dp,
    fontSize: TextUnit,
    horizontalSpacing: Dp,
    verticalSpacing: Dp,
    headerText: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with language-aware text
        PersonalityHeader2(headerText)
        Spacer(modifier = Modifier.height(32.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            modifier = Modifier.fillMaxSize()
        ) {
            items(personalities) { personality ->
                OutlinedCard(
                    onClick = {
                        Toast.makeText(context, personality, Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(30.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(7.dp, Color(0xFFF0A1F8)),
                    modifier = Modifier.size(cardSize)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = personality,
                            textAlign = TextAlign.Center,
                            fontSize = fontSize
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PersonalityHeader2(headerText: String) {
    Text(
        text = headerText,
        fontStyle = FontStyle.Italic,
        fontSize = 35.sp
    )
}
