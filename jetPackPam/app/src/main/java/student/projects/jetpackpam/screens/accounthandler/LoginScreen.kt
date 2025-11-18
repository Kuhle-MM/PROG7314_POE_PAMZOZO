package student.projects.jetpackpam.screens.accounthandler

import android.app.Activity
import android.content.Intent
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import student.projects.jetpackpam.design_system.GoogleBtn
import student.projects.jetpackpam.design_system.LinkButton
import student.projects.jetpackpam.design_system.LongButton
import student.projects.jetpackpam.design_system.TextFieldLong
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.models.LanguageViewModel
import student.projects.jetpackpam.screens.accounthandler.authorization.BiometricPromptActivity
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient
import student.projects.jetpackpam.util.DeviceConfiguration

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthorizationModelViewModel,
    googleAuthClient: GoogleAuthClient,
    languageViewModel: LanguageViewModel,
    googleSignInLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
) {
    val uiTexts = languageViewModel.uiTexts // Map<String, String>
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }

    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by authViewModel.errorMessage.collectAsStateWithLifecycle()
    val userData by authViewModel.userData.collectAsStateWithLifecycle()

    // Biometric state
    val biometricEnabled by authViewModel.biometricEnabled.collectAsStateWithLifecycle(initialValue = false)
    var biometricAutoLaunched by remember { mutableStateOf(false) }

    // Offer-to-enable dialog state (shown after credential login success)
    var showEnableBiometricDialog by remember { mutableStateOf(false) }

    // --- Biometric transient launcher (uses BiometricPromptActivity) ---
    val biometricLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Biometric succeeded -> ask ViewModel to restore session and then navigate
            authViewModel.onBiometricAuthenticated {
                navController.navigate("main") {
                    popUpTo("login") { inclusive = true }
                }
            }
        } else {
            Toast.makeText(context, uiTexts["biometric_failed"] ?: "Biometric failed or cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    // Load biometric pref once on composition
    LaunchedEffect(Unit) {
        authViewModel.loadBiometricPreference(context)
    }

    // Auto-launch biometric prompt once per session if enabled and available
    // BEFORE launching biometric, ensure we have a valid local session OR a last-saved-email
    LaunchedEffect(biometricEnabled) {
        if (biometricEnabled && !biometricAutoLaunched) {
            // NEW guard: do not auto-launch if we are currently signed out
            val hasLocalSession = authViewModel.userData.value != null
            val hasLastEmail = authViewModel.lastSavedEmail.value != null

            if (!hasLocalSession && !hasLastEmail) {
                // nothing to unlock — avoid launching biometric prompt
                return@LaunchedEffect
            }

            if (authViewModel.isBiometricAvailable(context)) {
                biometricAutoLaunched = true
                val intent = BiometricPromptActivity.createIntent(
                    context = context,
                    title = uiTexts["biometric_auto_title"] ?: "Unlock JetPackPam",
                    subtitle = uiTexts["biometric_auto_subtitle"] ?: "Authenticate to continue"
                )
                biometricLauncher.launch(intent)
            } else {
                authViewModel.setBiometricEnabled(context, false)
            }
        }
    }


    // Navigate to main when a user appears and we're not showing the enable-biometrics dialog.
    // This allows credential login to first show the dialog (we set that flag from the login callback).
    LaunchedEffect(userData, showEnableBiometricDialog) {
        if (userData != null && !showEnableBiometricDialog) {
            if (navController.currentDestination?.route != "main") {
                navController.navigate("main") { popUpTo("login") { inclusive = true } }
            }
        }
    }

    // --- Classic fallback launcher for Google sign-in ---
    val classicLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            try {
                val signInResult = googleAuthClient.signInWithIntent(result.data!!)
                authViewModel.handleGoogleSignInResult(signInResult)
                // handleGoogleSignInResult will update view state -> LaunchedEffect above navigates
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    uiTexts["google_sign_in_failed"] ?: "Google Sign-In failed: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e("LoginScreen", "Classic Google sign-in failed", e)
            }
        } else {
            Toast.makeText(
                context,
                uiTexts["google_sign_in_cancelled"] ?: "Google Sign-In cancelled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val onGoogleSignInClick: () -> Unit = {
        coroutineScope.launch {
            try {
                val intentSender = googleAuthClient.signIn()
                if (intentSender != null) {
                    googleSignInLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                } else {
                    classicLauncher.launch(googleAuthClient.getSignInIntent())
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    uiTexts["google_sign_in_failed"] ?: "Google Sign-In failed: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // --- Scaffold Layout ---
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        val rootModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .consumeWindowInsets(WindowInsets.navigationBars)

        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

        when (deviceConfiguration) {
            DeviceConfiguration.MOBILE_PORTRAIT -> {
                Column(
                    modifier = rootModifier.background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    LoginHeader(uiTexts)
                    LoginFormSection(
                        emailText = emailText,
                        onEmailTextChange = { emailText = it },
                        passwordText = passwordText,
                        onPasswordTextChange = { passwordText = it },
                        navController = navController,
                        authViewModel = authViewModel,
                        onGoogleSignInClick = onGoogleSignInClick,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        uiTexts = uiTexts,
                        onCredentialLoginSuccess = {
                            // Show enable-biometric dialog instead of immediately navigating
                            showEnableBiometricDialog = true
                        },
                        biometricEnabled = biometricEnabled,
                        biometricLauncher = biometricLauncher
                    )
                }
            }
            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                Row(
                    modifier = rootModifier.padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    LoginHeader(uiTexts, modifier = Modifier.weight(1f))
                    LoginFormSection(
                        emailText = emailText,
                        onEmailTextChange = { emailText = it },
                        passwordText = passwordText,
                        onPasswordTextChange = { passwordText = it },
                        navController = navController,
                        authViewModel = authViewModel,
                        onGoogleSignInClick = onGoogleSignInClick,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        uiTexts = uiTexts,
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        onCredentialLoginSuccess = { showEnableBiometricDialog = true },
                        biometricEnabled = biometricEnabled,
                        biometricLauncher = biometricLauncher
                    )
                }
            }
            else -> {
                Column(
                    modifier = rootModifier.verticalScroll(rememberScrollState()).padding(top = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoginHeader(uiTexts, modifier = Modifier.widthIn(max = 540.dp))
                    LoginFormSection(
                        emailText = emailText,
                        onEmailTextChange = { emailText = it },
                        passwordText = passwordText,
                        onPasswordTextChange = { passwordText = it },
                        navController = navController,
                        authViewModel = authViewModel,
                        onGoogleSignInClick = onGoogleSignInClick,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        uiTexts = uiTexts,
                        modifier = Modifier.widthIn(max = 540.dp),
                        onCredentialLoginSuccess = { showEnableBiometricDialog = true },
                        biometricEnabled = biometricEnabled,
                        biometricLauncher = biometricLauncher
                    )
                }
            }
        }
    }

    // --- Biometric opt-in dialog after credential login success ---
    if (showEnableBiometricDialog) {
        AlertDialog(
            onDismissRequest = { showEnableBiometricDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    // Use captured 'context' (not LocalContext.current here)
                    authViewModel.setBiometricEnabled(context, true)
                    authViewModel.persistLastSavedEmail(context)

                    Toast.makeText(context, uiTexts["biometric_enabled_toast"] ?: "Biometrics enabled", Toast.LENGTH_SHORT).show()

                    // navigate into the app
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                    showEnableBiometricDialog = false
                }) {
                    Text(text = uiTexts["yes_enable"] ?: "Yes, enable")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    // user declined — navigate anyway
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                    showEnableBiometricDialog = false
                }) {
                    Text(text = uiTexts["no_thanks"] ?: "No, thanks")
                }
            },
            title = { Text(text = uiTexts["biometric_title"] ?: "Enable biometric login?") },
            text = { Text(text = uiTexts["biometric_text"] ?: "Use fingerprint or device credential to sign in faster next time?") }
        )
    }
}

@Composable
fun LoginHeader(
    uiTexts: Map<String, String>,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(modifier = modifier, horizontalAlignment = alignment) {
        Text(
            text = uiTexts["login_title"] ?: "Log in",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = uiTexts["login_subtitle"] ?: "Log in to get started",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun LoginFormSection(
    emailText: String,
    onEmailTextChange: (String) -> Unit,
    passwordText: String,
    onPasswordTextChange: (String) -> Unit,
    navController: NavController,
    authViewModel: AuthorizationModelViewModel,
    onGoogleSignInClick: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    uiTexts: Map<String, String>,
    modifier: Modifier = Modifier,
    onCredentialLoginSuccess: () -> Unit = {},
    biometricEnabled: Boolean = false,
    biometricLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier) {
        TextFieldLong(
            text = emailText,
            onValueChange = onEmailTextChange,
            label = uiTexts["email_label"] ?: "Email",
            hint = uiTexts["email_hint"] ?: "example@example.com",
            isTextSecret = false,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextFieldLong(
            text = passwordText,
            onValueChange = onPasswordTextChange,
            label = uiTexts["password_label"] ?: "Password",
            hint = uiTexts["password_hint"] ?: "Password",
            isTextSecret = true,
            modifier = Modifier.fillMaxWidth()
        )
        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Login button - triggers ViewModel.login and then the onCredentialLoginSuccess callback
        LongButton(
            text = if (isLoading) uiTexts["logging_in"] ?: "Logging in..." else uiTexts["login_button"] ?: "Log in",
            onClick = {
                coroutineScope.launch {
                    try {
                        authViewModel.login(emailText, passwordText) {
                            // ViewModel will update userData and call this onSuccess lambda.
                            // We prefer to show opt-in dialog before navigating, so invoke callback.
                            onCredentialLoginSuccess()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, uiTexts["login_failed"] ?: "Login failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        LinkButton(
            text = uiTexts["signup_prompt"] ?: "Don't have an account?",
            onClick = { navController.navigate("signUp") },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        GoogleBtn(
            text = uiTexts["login_google"] ?: "Log in Using Google",
            onClick = onGoogleSignInClick,
            modifier = Modifier.fillMaxWidth(),
            imageRes = student.projects.jetpackpam.R.drawable.google_logo
        )

        // Manual biometric login button (only when enabled and device is available)
        if (biometricEnabled && authViewModel.isBiometricAvailable(context)) {
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(
                onClick = {
                    val intent = BiometricPromptActivity.createIntent(
                        context = context,
                        title = uiTexts["biometric_prompt_title"] ?: "Unlock JetPackPam",
                        subtitle = uiTexts["biometric_prompt_subtitle"] ?: "Authenticate to continue"
                    )
                    biometricLauncher.launch(intent)
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()    // remove this if you want a normal circular icon
            ) {
                Icon(
                    imageVector = Icons.Default.Fingerprint,
                    contentDescription = "Log in with Biometrics"
                )
            }

        }
    }
}
