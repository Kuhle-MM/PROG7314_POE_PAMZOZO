package student.projects.jetpackpam.screens.accounthandler

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import student.projects.jetpackpam.design_system.GoogleBtn
import student.projects.jetpackpam.design_system.LinkButton
import student.projects.jetpackpam.design_system.LongButton
import student.projects.jetpackpam.design_system.TextFieldLong
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.models.LanguageViewModel

/**
 * Clean SignUp screen with biometric opt-in dialog.
 */
@Composable
fun SignUpScreen(
    navController: NavController,
    authViewModel: AuthorizationModelViewModel,
    languageViewModel: LanguageViewModel,
    googleSignInLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
) {
    // Background decoration
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 15f)
            drawCircle(
                color = Color(0xFFF0A1F8),
                radius = 325f,
                center = Offset(x = size.width - 50f, y = 50f),
                style = stroke
            )
            drawCircle(
                color = Color(0xFFFF9BC9),
                radius = 720f,
                center = Offset(x = 50f, y = size.height - 50f),
                style = stroke
            )
        }
    }

    // Read UI texts (languageViewModel exposes a Map<String,String>)
    val uiTexts: Map<String, String> = languageViewModel.uiTexts

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Form state
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

    // Dialog state to offer biometrics after successful sign-up
    var showEnableBiometricDialog by remember { mutableStateOf(false) }

    // When sign-up completes, offer biometric opt-in (don't navigate here)
    LaunchedEffect(signUpSuccess) {
        if (signUpSuccess) {
            showEnableBiometricDialog = true
            authViewModel.resetSignUpState()
        }
    }

    // If Google sign-in produced a user, navigate directly to main
    LaunchedEffect(user) {
        user?.let {
            Toast.makeText(context, "Welcome, ${it.username ?: "user"}", Toast.LENGTH_SHORT).show()
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
                launchSingleTop = true
            }
        }
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
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = uiTexts["signup_title"] ?: "Sign Up", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = uiTexts["signup_subtitle"] ?: "Enter the fields to create your new profile",
                    style = MaterialTheme.typography.bodyLarge
                )
            }

            // Form section
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
                googleSignInLauncher = googleSignInLauncher,
                uiTexts = uiTexts
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

    // Biometric opt-in dialog
    if (showEnableBiometricDialog) {
        AlertDialog(
            onDismissRequest = { showEnableBiometricDialog = false },
            title = { Text(text = uiTexts["biometric_title"] ?: "Enable biometric login?") },
            text = {
                Text(
                    text = uiTexts["biometric_text"]
                        ?: "Use fingerprint or device credential to sign in faster next time?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    // Enable biometrics via ViewModel helpers (persisted inside ViewModel)
                    authViewModel.setBiometricEnabled(context, true)
                    // Persist last-saved-email (ViewModel holds last email in memory)
                    authViewModel.persistLastSavedEmail(context)

                    Toast.makeText(context, uiTexts["biometric_enabled_toast"] ?: "Biometric enabled", Toast.LENGTH_SHORT).show()

                    // Navigate into app
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                    showEnableBiometricDialog = false
                }) {
                    Text(text = uiTexts["yes_enable"] ?: "Yes, enable")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    // user opted out — navigate into app
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                    showEnableBiometricDialog = false
                }) {
                    Text(text = uiTexts["no_thanks"] ?: "No, thanks")
                }
            }
        )
    }
}

/**
 * Sign-up form section.
 */
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
    googleSignInLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>,
    uiTexts: Map<String, String>
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
                label = uiTexts["name_label"] ?: "Name",
                hint = uiTexts["name_hint"] ?: "John",
                isTextSecret = false,
                modifier = Modifier.weight(1f)
            )
            TextFieldLong(
                text = surnameText,
                onValueChange = onSurnameTextChange,
                label = uiTexts["surname_label"] ?: "Surname",
                hint = uiTexts["surname_hint"] ?: "Doe",
                isTextSecret = false,
                modifier = Modifier.weight(1f)
            )
        }

        // Email & Phone
        TextFieldLong(
            text = emailText,
            onValueChange = onEmailTextChange,
            label = uiTexts["email_label"] ?: "Email",
            hint = uiTexts["email_hint"] ?: "example@example.com",
            isTextSecret = false,
            modifier = Modifier.fillMaxWidth()
        )
        TextFieldLong(
            text = phoneNumberText,
            onValueChange = onPhoneNumberTextChange,
            label = uiTexts["phone_label"] ?: "Phone",
            hint = uiTexts["phone_hint"] ?: "0672221234",
            isTextSecret = false,
            modifier = Modifier.fillMaxWidth()
        )

        // Password & Confirm Password
        TextFieldLong(
            text = passwordText,
            onValueChange = onPasswordTextChange,
            label = uiTexts["password_label"] ?: "Password",
            hint = uiTexts["password_hint"] ?: "Password",
            isTextSecret = true,
            modifier = Modifier.fillMaxWidth()
        )
        TextFieldLong(
            text = confirmPasswordText,
            onValueChange = onConfirmPasswordChange,
            label = uiTexts["confirm_password_label"] ?: "Confirm Password",
            hint = uiTexts["confirm_password_hint"] ?: "Confirm Password",
            isTextSecret = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Sign-Up Button
        LongButton(
            text = if (isLoading) uiTexts["signup_loading"] ?: "Signing up..." else uiTexts["signup_button"] ?: "Sign Up",
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
            text = uiTexts["signup_prompt"] ?: "You already have a profile?",
            onClick = { navController.navigate("login") },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Google One Tap Button
        GoogleBtn(
            text = uiTexts["google_login"] ?: "Sign in with Google",
            onClick = {
                coroutineScope.launch {
                    try {
                        val intentSender = authViewModel.getGoogleSignInIntentSender()
                        if (intentSender != null) {
                            googleSignInLauncher.launch(
                                IntentSenderRequest.Builder(intentSender).build()
                            )
                        } else {
                            // fallback: use classic intent from GoogleAuthClient (UI caller owns oneTap client)
                            Toast.makeText(context, uiTexts["google_signin_failed"] ?: "Google Sign-In not available", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Log.e("SignUpForm", "Google sign-in error", e)
                        Toast.makeText(context, uiTexts["google_signin_failed"] ?: "Google Sign-In failed", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            imageRes = student.projects.jetpackpam.R.drawable.google_logo
        )
    }
}
