package student.projects.jetpackpam.screens.bottomnavscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

import kotlinx.coroutines.delay

@Composable
fun VideoScreen() {
    var showDialog by remember { mutableStateOf(false) }
    var imageUrl by remember { mutableStateOf("http://10.0.2.2:5000/api/camera/stream") } // Replace with your API URL

    // Auto-refresh every 1 second (simulates video)
    LaunchedEffect(Unit) {
        while (true) {
            imageUrl = "http://10.0.2.2:5000/api/camera/stream?ts=${System.currentTimeMillis()}"
            delay(1000)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Video feed (refreshing image)
        Image(
            painter = rememberAsyncImagePainter(imageUrl),
            contentDescription = "Camera Feed",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Floating button for suggestions
        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Message, contentDescription = "Suggestions")
        }

        // Suggestion dialog
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Suggestions") },
                text = { Text("Would you like to suggest an improvement?") },
                confirmButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Yes")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}


@Composable
@Preview
fun VideoScreenPreview() {
    VideoScreen()
}
