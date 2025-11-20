package student.projects.jetpackpam.screens.accounthandler

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
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

/**
 * LoginScreen — uses the enrollmentRequested -> LaunchedEffect flow to launch biometric.
 * Keeps your original logic and improves UX (no blank flash, requires credentials to enable).
 */
@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthorizationModelViewModel,
    googleAuthClient: GoogleAuthClient,
    languageViewModel: LanguageViewModel,
    googleSignInLauncher: ManagedActivityResultLauncher<IntentSenderRequest, ActivityResult>
) {
    val uiTexts = languageViewModel.uiTexts
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var emailText by rememberSaveable { mutableStateOf("") }
    var passwordText by rememberSaveable { mutableStateOf("") }

    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by authViewModel.errorMessage.collectAsStateWithLifecycle()
    val userData by authViewModel.userData.collectAsStateWithLifecycle()
    val biometricEnabled by authViewModel.biometricEnabled.collectAsStateWithLifecycle(initialValue = false)

    // enrollment state
    var enrollmentRequested by remember { mutableStateOf(false) }
    var pendingEmail by remember { mutableStateOf<String?>(null) }
    var pendingPassword by remember { mutableStateOf<String?>(null) }

    // Launcher that starts the BiometricPromptActivity (transparent)
    val biometricLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Two possible flows: enrollment (pending creds exist) OR unlock (no pending creds)
            val email = pendingEmail
            val pw = pendingPassword

            if (!email.isNullOrBlank() && !pw.isNullOrBlank()) {
                // enrollment: perform login first (verifies credentials) then persist
                scope.launch {
                    authViewModel.login(email, pw) {
                        // persist securely and enable biometric flag
                        authViewModel.persistCredentialsEncrypted(context, email, pw)
                        authViewModel.setBiometricEnabled(context, true)
                        authViewModel.persistLastSavedEmail(context)

                        // clear pending state
                        pendingEmail = null
                        pendingPassword = null
                        enrollmentRequested = false

                        Toast.makeText(context, uiTexts["biometric_enabled_toast"] ?: "Biometrics enabled", Toast.LENGTH_SHORT).show()

                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            } else {
                // unlock flow: restore using saved creds or active Firebase session
                authViewModel.onBiometricAuthenticatedWithSignIn(context) {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }
        } else {
            // cancelled/failed
            pendingEmail = null
            pendingPassword = null
            enrollmentRequested = false
            Toast.makeText(context, uiTexts["biometric_failed"] ?: "Fingerprint cancelled/failed", Toast.LENGTH_SHORT).show()
        }
    }

    // Google classic fallback launcher (unchanged)
    val classicLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            try {
                val signInResult = googleAuthClient.signInWithIntent(result.data!!)
                authViewModel.handleGoogleSignInResult(signInResult)
            } catch (e: Exception) {
                Log.e("LoginScreen", "Google classic failed", e)
                Toast.makeText(context, uiTexts["google_sign_in_failed"] ?: "Google Sign-In failed", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, uiTexts["google_sign_in_cancelled"] ?: "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    val onGoogleSignInClick: () -> Unit = {
        scope.launch {
            try {
                val intentSender = googleAuthClient.signIn()
                if (intentSender != null) {
                    googleSignInLauncher.launch(IntentSenderRequest.Builder(intentSender).build())
                } else {
                    classicLauncher.launch(googleAuthClient.getSignInIntent())
                }
            } catch (e: Exception) {
                Toast.makeText(context, uiTexts["google_sign_in_failed"] ?: "Google Sign-In failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    // load prefs and pre-fill last email
    LaunchedEffect(Unit) {
        authViewModel.loadBiometricPreference(context)
        authViewModel.getLastSavedEmailFromPrefs(context)?.let { existing -> emailText = existing }
    }

    // navigate if user already present
    LaunchedEffect(userData) {
        if (userData != null && navController.currentDestination?.route != "main") {
            navController.navigate("main") { popUpTo("login") { inclusive = true } }
        }
    }

    // When enrollmentRequested flips true we launch the biometric activity from here.
    // Delay slightly so the dialog finishes dismissing and Compose stabilizes (prevents flash).
    LaunchedEffect(enrollmentRequested) {
        if (!enrollmentRequested) return@LaunchedEffect
        // small delay to let dialog dismiss
        delay(250L)

        // double-check fingerprint-only availability
        val bm = BiometricManager.from(context)
        val canStrong = bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
        val hasFpFeature = context.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)

        if (!canStrong && !hasFpFeature) {
            Toast.makeText(context, uiTexts["biometric_unavailable"] ?: "Fingerprint not available on device", Toast.LENGTH_LONG).show()
            enrollmentRequested = false
            pendingEmail = null
            pendingPassword = null
            return@LaunchedEffect
        }

        val intent = BiometricPromptActivity.createIntent(
            context = context,
            title = uiTexts["biometric_confirm_title"] ?: "Confirm fingerprint to enable",
            subtitle = uiTexts["biometric_confirm_subtitle"] ?: "Touch fingerprint to finish setup"
        )
        biometricLauncher.launch(intent)
    }

    // ---- UI scaffold ----
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
                Column(modifier = rootModifier.background(MaterialTheme.colorScheme.background), verticalArrangement = Arrangement.spacedBy(32.dp)) {
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
                        biometricEnabled = biometricEnabled,
                        biometricLauncher = biometricLauncher,
                        onUserConfirmedEnableBiometrics = { e, p ->
                            // parent sets pending credentials and flips request flag
                            pendingEmail = e
                            pendingPassword = p
                            enrollmentRequested = true
                        }
                    )
                }
            }
            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                Row(modifier = rootModifier.padding(horizontal = 32.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
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
                        biometricEnabled = biometricEnabled,
                        biometricLauncher = biometricLauncher,
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                        onUserConfirmedEnableBiometrics = { e, p ->
                            pendingEmail = e
                            pendingPassword = p
                            enrollmentRequested = true
                        }
                    )
                }
            }
            else -> {
                Column(modifier = rootModifier.verticalScroll(rememberScrollState()).padding(top = 48.dp), verticalArrangement = Arrangement.spacedBy(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
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
                        biometricEnabled = biometricEnabled,
                        biometricLauncher = biometricLauncher,
                        modifier = Modifier.widthIn(max = 540.dp),
                        onUserConfirmedEnableBiometrics = { e, p ->
                            pendingEmail = e
                            pendingPassword = p
                            enrollmentRequested = true
                        }
                    )
                }
            }
        }
    }
}

/* ---------------------------
   LoginHeader + LoginFormSection
   LoginFormSection shows the opt-in dialog and hands pending credentials back to parent.
   --------------------------- */

@Composable
fun LoginHeader(
    uiTexts: Map<String, String>,
    modifier: Modifier = Modifier,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(modifier = modifier, horizontalAlignment = alignment) {
        Text(text = uiTexts["login_title"] ?: "Log in", style = MaterialTheme.typography.titleLarge)
        Text(text = uiTexts["login_subtitle"] ?: "Log in to get started", style = MaterialTheme.typography.bodyLarge)
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
    biometricEnabled: Boolean = false,
    biometricLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>,
    modifier: Modifier = Modifier,
    onUserConfirmedEnableBiometrics: (email: String, password: String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showEnableDialog by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        TextFieldLong(text = emailText, onValueChange = onEmailTextChange, label = uiTexts["email_label"] ?: "Email", hint = uiTexts["email_hint"] ?: "example@example.com", isTextSecret = false, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(16.dp))
        TextFieldLong(text = passwordText, onValueChange = onPasswordTextChange, label = uiTexts["password_label"] ?: "Password", hint = uiTexts["password_hint"] ?: "Password", isTextSecret = true, modifier = Modifier.fillMaxWidth())

        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = msg, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Spacer(modifier = Modifier.height(24.dp))

        LongButton(text = if (isLoading) uiTexts["logging_in"] ?: "Logging in..." else uiTexts["login_button"] ?: "Log in",
            onClick = {
                // Show opt-in dialog BEFORE login if fingerprint hardware available
                val bm = BiometricManager.from(context)
                val canStrong = bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
                val hasFp = context.packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
                if (canStrong || hasFp) {
                    showEnableDialog = true
                } else {
                    // fallback directly login
                    coroutineScope.launch {
                        authViewModel.login(emailText, passwordText) {
                            navController.navigate("main") { popUpTo("login") { inclusive = true } }
                        }
                    }
                }
            }, modifier = Modifier.fillMaxWidth(), enabled = !isLoading)

        Spacer(modifier = Modifier.height(16.dp))

        LinkButton(text = uiTexts["signup_prompt"] ?: "Don't have an account?", onClick = { navController.navigate("signUp") }, modifier = Modifier.align(Alignment.CenterHorizontally))

        Spacer(modifier = Modifier.height(24.dp))

        GoogleBtn(text = uiTexts["login_google"] ?: "Log in Using Google", onClick = onGoogleSignInClick, modifier = Modifier.fillMaxWidth(), imageRes = student.projects.jetpackpam.R.drawable.google_logo)

        // Manual fingerprint unlock / icon (appears when saved email exists or biometric enabled)
        val lastSaved = authViewModel.getLastSavedEmailFromPrefs(context)
        val bm2 = BiometricManager.from(context)
        val fpAvailable = bm2.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS
        val shouldShowFp = (biometricEnabled || lastSaved != null) && fpAvailable

        if (shouldShowFp) {
            Spacer(modifier = Modifier.height(16.dp))
            IconButton(onClick = {
                val intent = BiometricPromptActivity.createIntent(context = context, title = uiTexts["biometric_prompt_title"] ?: "Unlock JetPackPam", subtitle = uiTexts["biometric_prompt_subtitle"] ?: "Authenticate to continue")
                biometricLauncher.launch(intent)
            }) {
                Icon(imageVector = Icons.Default.Fingerprint, contentDescription = uiTexts["login_biometric"] ?: "Login with fingerprint")
            }
        }
    }

    if (showEnableDialog) {
        AlertDialog(
            onDismissRequest = { showEnableDialog = false },
            title = { Text(text = uiTexts["biometric_title"] ?: "Enable fingerprint login?") },
            text = { Text(text = uiTexts["biometric_text"] ?: "Use fingerprint next time?") },
            confirmButton = {
                TextButton(onClick = {
                    // require non-empty credentials before continuing
                    if (emailText.isBlank() || passwordText.isBlank()) {
                        Toast.makeText(context, uiTexts["enter_credentials_first"] ?: "Fill email & password before enabling", Toast.LENGTH_LONG).show()
                    } else {
                        onUserConfirmedEnableBiometrics(emailText, passwordText)
                        showEnableDialog = false
                    }
                }) {
                    Text(uiTexts["yes_enable"] ?: "Enable")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    // user declined -> normal login and dismiss dialog
                    showEnableDialog = false
                    coroutineScope.launch {
                        authViewModel.login(emailText, passwordText) {
                            navController.navigate("main") { popUpTo("login") { inclusive = true } }
                        }
                    }
                }) {
                    Text(uiTexts["no_thanks"] ?: "No thanks")
                }
            }
        )
    }
}