package student.projects.jetpackpam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import student.projects.jetpackpam.screens.accounthandler.LoginScreen
import student.projects.jetpackpam.screens.accounthandler.SignUpScreen
import student.projects.jetpackpam.screens.mainapp.MainScreen
import student.projects.jetpackpam.ui.theme.JetPackPamTheme


// if you are lost look at the screen package and then the auth screens for log in
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JetPackPamTheme(){

                //MainScreen()
               // LoginScreen()
                //SignUpScreen()
            }
        }
    }
}
