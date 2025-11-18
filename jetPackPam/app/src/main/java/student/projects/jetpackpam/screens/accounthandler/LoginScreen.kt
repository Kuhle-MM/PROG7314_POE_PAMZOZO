package student.projects.jetpackpam.screens.accounthandler

import android.app.Activity
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
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import student.projects.jetpackpam.design_system.GoogleBtn
import student.projects.jetpackpam.design_system.LinkButton
import student.projects.jetpackpam.design_system.LongButton
import student.projects.jetpackpam.design_system.TextFieldLong
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.models.LanguageViewModel
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

    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()
    val userData by authViewModel.userData.collectAsState()

    // --- Navigate if user already signed in ---
    LaunchedEffect(userData) {
        userData?.let {
            if (navController.currentDestination?.route != "main") {
                navController.navigate("main") { popUpTo("login") { inclusive = true } }
            }
        }
    }

    // --- Classic fallback launcher ---
    val classicLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            try {
                val signInResult = googleAuthClient.signInWithIntent(result.data!!)
                authViewModel.handleGoogleSignInResult(signInResult)
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
                        emailText, { emailText = it },
                        passwordText, { passwordText = it },
                        navController, authViewModel,
                        onGoogleSignInClick, isLoading, errorMessage,
                        uiTexts
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
                        emailText, { emailText = it },
                        passwordText, { passwordText = it },
                        navController, authViewModel,
                        onGoogleSignInClick, isLoading, errorMessage,
                        uiTexts,
                        modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
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
                        emailText, { emailText = it },
                        passwordText, { passwordText = it },
                        navController, authViewModel,
                        onGoogleSignInClick, isLoading, errorMessage,
                        uiTexts,
                        modifier = Modifier.widthIn(max = 540.dp)
                    )
                }
            }
        }
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
    modifier: Modifier = Modifier
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
        LongButton(
            text = if (isLoading) uiTexts["logging_in"] ?: "Logging in..." else uiTexts["login_button"] ?: "Log in",
            onClick = {
                coroutineScope.launch {
                    authViewModel.login(emailText, passwordText) {
                        Toast.makeText(context, uiTexts["welcome_back"] ?: "Welcome back!", Toast.LENGTH_SHORT).show()
                        navController.navigate("main") { popUpTo("login") { inclusive = true } }
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
    }
}
