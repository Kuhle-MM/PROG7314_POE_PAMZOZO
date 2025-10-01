package student.projects.jetpackpam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import student.projects.jetpackpam.appNavigation.RootNavGraph
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.screens.ChatScreen
import student.projects.jetpackpam.screens.accounthandler.LoginScreen
import student.projects.jetpackpam.screens.accounthandler.SignUpScreen
import student.projects.jetpackpam.screens.mainapp.MainScreen
import student.projects.jetpackpam.ui.theme.JetPackPamTheme
import student.projects.jetpackpam.ui.theme.Surface


// if you are lost look at the screen package and then the auth screens for log in
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthorizationModelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetPackPamTheme () {
                Surface(color = MaterialTheme.colorScheme.background) {
                    RootNavGraph(authViewModel)
                }
            }
        }
    }
}
