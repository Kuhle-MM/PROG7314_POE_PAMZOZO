package student.projects.jetpackpam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import student.projects.jetpackpam.appNavigation.AppNavGraph
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.ui.theme.JetPackPamTheme



// if you are lost look at the screen package and then the auth screens for log in
class MainActivity : ComponentActivity() {
    private val authViewModel: AuthorizationModelViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JetPackPamTheme () {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavGraph()
                }
            }
        }
    }
}
