package student.projects.jetpackpam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.auth.api.identity.Identity
import student.projects.jetpackpam.appNavigation.AppNavGraph
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.screens.accounthandler.authorization.AuthorizationModelViewModelFactory
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient
import student.projects.jetpackpam.ui.theme.JetPackPamTheme

class MainActivity : ComponentActivity() {

    // --- Dependencies used throughout the app ---
    private lateinit var authViewModel: AuthorizationModelViewModel
    private lateinit var googleAuthClient: GoogleAuthClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- Initialize the GoogleAuthClient ---
        // This manages all Google One Tap / Sign-In operations
        googleAuthClient = GoogleAuthClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )

        // --- ViewModel setup using a Factory ---
        // The factory injects our googleAuthClient into the ViewModel
        val factory = AuthorizationModelViewModelFactory(googleAuthClient)
        authViewModel = ViewModelProvider(this, factory)[AuthorizationModelViewModel::class.java]

        // --- Compose UI setup ---
        setContent {
            JetPackPamTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    // AppNavGraph controls all navigation & session flow
                    AppNavGraph(
                        googleAuthClient = googleAuthClient,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}
