package student.projects.jetpackpam.screens.bottomnavscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import student.projects.jetpackpam.R
import student.projects.jetpackpam.screens.ChatScreen
import student.projects.jetpackpam.util.DeviceConfiguration

@Composable
fun HomeScreen() {
    var showChat by remember { mutableStateOf(false) }

    if (showChat) {
        ChatScreen()
        return
    }

    val windowInfo = currentWindowAdaptiveInfo()
    val deviceConfig = DeviceConfiguration.fromWindowSizeClass(windowInfo.windowSizeClass)

    val rootModifier = Modifier
        .fillMaxSize()
        .windowInsetsPadding(WindowInsets.statusBars)
        .consumeWindowInsets(WindowInsets.navigationBars)
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp)


    when (deviceConfig) {
        DeviceConfiguration.MOBILE_PORTRAIT -> {
            Column(
                modifier = rootModifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                HomeContent { showChat = true }
            }
        }
        DeviceConfiguration.MOBILE_LANDSCAPE -> {
            Row(
                modifier = rootModifier.verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HomeContent(modifier = Modifier.weight(1f)) { showChat = true }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
        DeviceConfiguration.TABLET_PORTRAIT,
        DeviceConfiguration.TABLET_LANDSCAPE,
        DeviceConfiguration.DESKTOP -> {
            Column(
                modifier = rootModifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                HomeContent(modifier = Modifier.widthIn(max = 540.dp)) { showChat = true }
            }
        }
    }
}

@Composable
fun HomeContent(modifier: Modifier = Modifier, onChatClick: () -> Unit) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Image
        Image(
            painter = painterResource(id = R.drawable.pamicon),
            contentDescription = "PAM",
            modifier = Modifier.size(270.dp)
        )

        // Text
        Text(
            text = "I’m ready to help you with anything.\nJust type below or say the word",
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center,
            fontFamily = FontFamily.SansSerif
        )

        // Chat Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            ElevatedButton(onClick = onChatClick) {
                Icon(
                    imageVector = Icons.Default.ChatBubbleOutline,
                    contentDescription = "Chat"
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun HomeScreenAdaptivePreview() {
    HomeScreen()
}
