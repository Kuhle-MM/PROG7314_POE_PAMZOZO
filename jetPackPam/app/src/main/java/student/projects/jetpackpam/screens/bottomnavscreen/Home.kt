package student.projects.jetpackpam.screens.bottomnavscreen

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import student.projects.jetpackpam.R
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.models.AuthorizationModelViewModel

@Composable
fun HomeScreen(
    navController: NavHostController,
    authViewModel: AuthorizationModelViewModel,
    languageViewModel: LanguageViewModel,
    context: Context = LocalContext.current,
    onSignOut: () -> Unit = {
        authViewModel.signOutSafely(
            context = context,
            navController = navController,

        )
    }
) {
    val uiTexts = languageViewModel.uiTexts
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
                radius = 125f,

                center = Offset(x = size.width - 50f, y = 50f),
                style = stroke
            )

            // Bottom-left circle
            drawCircle(
                color = Color(0xFFFF9BC9),
                radius = 320f,
                center = Offset(x = 50f, y = size.height - 50f),
                style = stroke
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            Image(
                painter = painterResource(id = R.drawable.pamicon),
                contentDescription = "PAM",
                modifier = Modifier.size(270.dp)
            )

            Text(
                text = uiTexts["welcomeMessage"]
                    ?: "I’m ready to help you with anything.\nJust type below or say the word",
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.SansSerif
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ElevatedButton(
                    onClick = { navController.navigate("chat") }
                ) {
                    Icon(
                        imageVector = Icons.Default.ChatBubbleOutline,
                        contentDescription = "Chat"
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = uiTexts["chatButton"] ?: "Chat")
                }
            }
        }
    }
}