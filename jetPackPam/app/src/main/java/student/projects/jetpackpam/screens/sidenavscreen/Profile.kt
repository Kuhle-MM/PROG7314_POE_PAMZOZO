package student.projects.jetpackpam.screens.sidenavscreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.google.firebase.Firebase
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.models.UserData

@Composable
fun ProfileScreen(
    userData: UserData?,
    languageViewModel: LanguageViewModel
)
 {
    // Use the same approach as HomeScreen
    val uiTexts = languageViewModel.uiTexts
    var username by remember { mutableStateOf(TextFieldValue()) }
    var name by remember { mutableStateOf(TextFieldValue()) }
    var email by remember { mutableStateOf(TextFieldValue()) }

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
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (userData?.profilePictureUrl != null) {
                AsyncImage(
                    model = userData.profilePictureUrl,
                    contentDescription = uiTexts["profilePicture"] ?: "Profile picture",
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (userData?.username != null) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    modifier = Modifier.padding(horizontal = 16.dp),
                    label = { Text(text = uiTexts["username"] ?: "Username") }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.padding(horizontal = 16.dp),
                label = { Text(text = uiTexts["name"] ?: "Name") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                modifier = Modifier.padding(horizontal = 16.dp),
                label = { Text(text = uiTexts["email"] ?: "Email") }
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    GlobalScope.launch {
                        val database = Firebase.database.reference
                        database.child("users").child(userData?.userId!!).setValue(
                            mapOf(
                                "username" to username.text,
                                "name" to name.text,
                                "email" to email.text
                            )
                        )
                    }
                }
            ) {
                // Use uiTexts map to get language-specific "Sign out"
                Text(text = uiTexts["save"] ?: "Save")
            }
        }
    }
}
