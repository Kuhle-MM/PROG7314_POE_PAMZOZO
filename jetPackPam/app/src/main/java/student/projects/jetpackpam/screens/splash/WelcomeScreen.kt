package student.projects.jetpackpam.screens.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import student.projects.jetpackpam.R

@Composable
fun WelcomeScreen(navController: NavController) {

    // Your curly font
    //val curlyFont = FontFamily(Font(R.font.cursivefont)) // change to your font name

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {

        // -----------------------
        // Background artwork
        // -----------------------
        Image(
            painter = painterResource(id = R.drawable.welcome_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

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
                //fontFamily = curlyFont,
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
                onClick = { navController.navigate("signup") },
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
