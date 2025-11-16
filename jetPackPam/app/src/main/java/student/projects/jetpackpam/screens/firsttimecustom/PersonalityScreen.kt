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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.util.DeviceConfiguration

@Composable
fun PersonalitySelectionScreen(languageViewModel: LanguageViewModel
) {
    val uiTexts by languageViewModel.uiTexts

    val context = LocalContext.current
    val personalities = listOf(
        "Sarcastic", "Friendly", "Gen Z", "Never in the mood", "Motivational Coach",
        "Wise Elder", "Cheerful Optimist", "Storyteller", "Shakespearean", "Tech Geek"
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

        // --- Adaptive UI layout ---
        when (deviceConfiguration) {
            DeviceConfiguration.MOBILE_PORTRAIT -> {
                PersonalityLayout(
                    personalities = personalities,
                    context = context,
                    modifier = rootModifier,
                    cardSize = 220.dp,
                    fontSize = 24.sp,
                    spacing = 16.dp
                )
            }

            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                PersonalityLayout(
                    personalities = personalities,
                    context = context,
                    modifier = rootModifier,
                    cardSize = 180.dp,
                    fontSize = 22.sp,
                    spacing = 12.dp
                )
            }

            else -> {
                PersonalityLayout(
                    personalities = personalities,
                    context = context,
                    modifier = rootModifier,
                    cardSize = 280.dp,
                    fontSize = 28.sp,
                    spacing = 20.dp
                )
            }
        }
    }
}

@Composable
private fun PersonalityLayout(
    personalities: List<String>,
    context: Context,
    modifier: Modifier = Modifier,
    cardSize: Dp,
    fontSize: TextUnit,
    spacing: Dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        PersonalityHeader()
        Spacer(modifier = Modifier.height(30.dp))

        // Scrollable row of personality options
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(spacing),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            items(personalities) { personality ->
                OutlinedCard(
                    onClick = {
                        Toast.makeText(context, "Clicked on $personality", Toast.LENGTH_SHORT).show()
                    },
                    shape = CircleShape,
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
fun PersonalityHeader(){
    Text(
        text = "Choose my personality",
        fontStyle = FontStyle.Italic,
        fontSize = 35.sp
    )
}