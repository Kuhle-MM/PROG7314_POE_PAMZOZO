package student.projects.jetpackpam.screens.accounthandler

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.OffsetMapping.Companion.Identity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import student.projects.jetpackpam.data.local.LocalDB
import student.projects.jetpackpam.data.local.OfflineRepository
import student.projects.jetpackpam.data.sync.FirebaseSyncManager
import student.projects.jetpackpam.design_system.GoogleBtn
import student.projects.jetpackpam.design_system.LinkButton
import student.projects.jetpackpam.design_system.LongButton
import student.projects.jetpackpam.design_system.TextFieldLong
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.screens.accounthandler.authorization.AuthorizationModelViewModelFactory
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient
import student.projects.jetpackpam.util.DeviceConfiguration
//import com.google.android.gms.identity.client.Identity


@Composable
fun SignUpScreen(
    navController: NavController,
    authViewModel: AuthorizationModelViewModel,
    languageViewModel: LanguageViewModel,
    googleSignInLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
) {
    val uiTexts by languageViewModel.uiTexts

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val db = LocalDB.getInstance(context)
    val repo = remember { OfflineRepository(db.offlineDao()) }
    val sync = remember { FirebaseSyncManager(repo, FirebaseDatabase.getInstance().reference) }




    // UI state
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var confirmPasswordText by remember { mutableStateOf("") }
    var nameText by remember { mutableStateOf("") }
    var surnameText by remember { mutableStateOf("") }
    var phoneNumberText by remember { mutableStateOf("") }

    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by authViewModel.errorMessage.collectAsStateWithLifecycle()
    val signUpSuccess by authViewModel.signUpSuccess.collectAsStateWithLifecycle()
    val user by authViewModel.userData.collectAsStateWithLifecycle()

    // --- Navigate on sign-up success ---
    LaunchedEffect(signUpSuccess) {
        if (signUpSuccess) {
            Toast.makeText(context, "Sign up successful!", Toast.LENGTH_SHORT).show()
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
                launchSingleTop = true
            }
            authViewModel.resetSignUpState()
        }
    }

    // --- Navigate on Google sign-in success ---
    LaunchedEffect(user) {
        user?.let {
            Toast.makeText(context, "Welcome, ${it.username}", Toast.LENGTH_SHORT).show()
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    LaunchedEffect(true) {
        authViewModel.setupOfflineSupport(repo, sync)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SignUpHeader()

            SignUpFormSection(
                emailText = emailText,
                onEmailTextChange = { emailText = it },
                passwordText = passwordText,
                onPasswordTextChange = { passwordText = it },
                confirmPasswordText = confirmPasswordText,
                onConfirmPasswordChange = { confirmPasswordText = it },
                nameText = nameText,
                onNameTextChange = { nameText = it },
                surnameText = surnameText,
                onSurnameTextChange = { surnameText = it },
                phoneNumberText = phoneNumberText,
                onPhoneNumberTextChange = { phoneNumberText = it },
                navController = navController,
                authViewModel = authViewModel,
                isLoading = isLoading,
                coroutineScope = coroutineScope,
                context = context,
                googleSignInLauncher = googleSignInLauncher
            )

            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}



@Composable
fun SignUpHeader() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Sign Up", style = MaterialTheme.typography.titleLarge)
        Text(
            text = "Enter the fields to create your new profile",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// SignUpFormSection.kt
// SignUpFormSection.kt
@Composable
fun SignUpFormSection(
    emailText: String,
    onEmailTextChange: (String) -> Unit,
    passwordText: String,
    onPasswordTextChange: (String) -> Unit,
    confirmPasswordText: String,
    onConfirmPasswordChange: (String) -> Unit,
    nameText: String,
    onNameTextChange: (String) -> Unit,
    surnameText: String,
    onSurnameTextChange: (String) -> Unit,
    phoneNumberText: String,
    onPhoneNumberTextChange: (String) -> Unit,
    navController: NavController,
    authViewModel: AuthorizationModelViewModel,
    isLoading: Boolean,
    coroutineScope: CoroutineScope,
    context: Context,
    googleSignInLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Name & Surname
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TextFieldLong(
                text = nameText,
                onValueChange = onNameTextChange,
                label = "Name",
                hint = "John",
                isTextSecret = false,
                modifier = Modifier.weight(1f)
            )
            TextFieldLong(
                text = surnameText,
                onValueChange = onSurnameTextChange,
                label = "Surname",
                hint = "Doe",
                isTextSecret = false,
                modifier = Modifier.weight(1f)
            )
        }

        // Email & Phone
        TextFieldLong(
            text = emailText,
            onValueChange = onEmailTextChange,
            label = "Email",
            hint = "example@example.com",
            isTextSecret = false,
            modifier = Modifier.fillMaxWidth()
        )
        TextFieldLong(
            text = phoneNumberText,
            onValueChange = onPhoneNumberTextChange,
            label = "Phone",
            hint = "0672221234",
            isTextSecret = false,
            modifier = Modifier.fillMaxWidth()
        )

        // Password & Confirm Password
        TextFieldLong(
            text = passwordText,
            onValueChange = onPasswordTextChange,
            label = "Password",
            hint = "Password",
            isTextSecret = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextFieldLong(
            text = confirmPasswordText,
            onValueChange = onConfirmPasswordChange,
            label = "Confirm Password",
            hint = "Confirm Password",
            isTextSecret = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Sign-Up Button
        LongButton(
            text = if (isLoading) "Signing up..." else "Sign Up",
            onClick = {
                coroutineScope.launch {
                    authViewModel.signUp(
                        nameText, surnameText, emailText, passwordText, confirmPasswordText
                    ) {}
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        // Navigate to Login
        LinkButton(
            text = "You already have a profile?",
            onClick = { navController.navigate("login") },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Google One Tap Button
        GoogleBtn(
            text = "Sign in with Google",
            onClick = {
                coroutineScope.launch {
                    val intentSender = authViewModel.getGoogleSignInIntentSender()
                    if (intentSender != null) {
                        googleSignInLauncher.launch(
                            IntentSenderRequest.Builder(intentSender).build()
                        )
                    } else {
                        Toast.makeText(context, "Google Sign-In not available", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            imageRes = student.projects.jetpackpam.R.drawable.google_logo
        )
    }
}
