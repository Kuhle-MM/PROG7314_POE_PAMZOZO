package student.projects.jetpackpam.screens.splash

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import student.projects.jetpackpam.R

@Composable
fun WelcomeScreen(navController: NavController) {

    // Your curly font
    // val curlyFont = FontFamily(Font(R.font.cursivefont)) // Replace with your font

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
                radius = 325f,
                center = Offset(x = size.width - 50f, y = 50f),
                style = stroke
            )

            // Bottom-left circle
            drawCircle(
                color = Color(0xFFFF9BC9),
                radius = 720f,
                center = Offset(x = 50f, y = size.height - 50f),
                style = stroke
            )
        }

        // -----------------------
        // Main Content
        // -----------------------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Welcome text
            Text(
                text = "Welcome to",
                // fontFamily = curlyFont,
                fontSize = 60.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "P.A.M",
                fontSize = 56.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(55.dp))

            Text(
                text = "Personal Assistant with Mobility",
                fontSize = 24.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(75.dp))

            // Login Button
            Button(
                onClick = { navController.navigate("login") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE98AC8)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Text(text = "Login", fontSize = 28.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sign Up Button
            Button(
                onClick = { navController.navigate("signUp") },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB764A8)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
            ) {
                Text(text = "Sign Up", fontSize = 28.sp, color = Color.White)
            }
        }
    }
}
