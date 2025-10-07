package student.projects.jetpackpam

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.LaunchedEffect
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.ui.theme.JetPackPamTheme

class MainActivity : ComponentActivity() {

    // --- Dependencies used throughout the app ---
    private lateinit var authViewModel: AuthorizationModelViewModel
    private lateinit var googleAuthClient: GoogleAuthClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //microphone permissions
        val requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
                if (!isGranted) {
                    Toast.makeText(this, "Microphone permission is required.", Toast.LENGTH_LONG).show()
                }
            }

        // Check permission
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Already granted
            }
            else -> {
                // Ask for permission
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
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
            val languageViewModel: LanguageViewModel = viewModel()

            // Load saved language preference
            LaunchedEffect(Unit) {
                languageViewModel.loadLanguage()
            }
            JetPackPamTheme {
                Surface(color = MaterialTheme.colorScheme.background) {

                    AppNavGraph(
                        googleAuthClient = googleAuthClient,
                        authViewModel = authViewModel,
                        languageViewModel = languageViewModel
                    )

                    //MainScreen()
                    //LoginScreen()
                    //LanguageSelectionScreen()
                    //PersonalitySelectionScreen()
                    //PersonalitySelectionScreen2()
                    //FontSelectionScreen()
                    // PamThemeSelectionScreen()
                }
            }
        }

    }
}
