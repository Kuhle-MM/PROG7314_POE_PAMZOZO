package student.projects.jetpackpam.screens.firsttimecustom

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FontSelectionScreen() {
    var fontSize by remember { mutableStateOf(20f) } // default size in sp
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 50.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FontHeader()
            Spacer(modifier = Modifier.height(40.dp))
            Text(
                text = "Preview Text", //will demo chat screen here, once ap is done with the main chat screen
                fontSize = fontSize.sp,
                textAlign = TextAlign.Center
            )
        }

        // Slider aligned at bottom
        Slider(
            value = fontSize,
            onValueChange = { fontSize = it },
            valueRange = 16f..48f,
            steps = 36,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(16.dp, 50.dp)
        )
    }
}

@Composable
fun FontHeader() {
    Text(
        text = "Select your font size for our chat",
        fontStyle = FontStyle.Italic,
        fontSize = 35.sp
    )
}