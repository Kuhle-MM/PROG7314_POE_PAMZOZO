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
    val uiTexts by languageViewModel.uiTexts

    val context = LocalContext.current

    // --- Local state for email & password ---
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }

    // --- Collect state from ViewModel ---
    val isLoading by authViewModel.isLoading.collectAsState()
    val errorMessage by authViewModel.errorMessage.collectAsState()

    // --- Log ViewModel state changes ---
    LaunchedEffect(isLoading, errorMessage) {
        Log.d("LoginScreen", "Loading state: $isLoading")
        Log.d("LoginScreen", "Error message: $errorMessage")
    }
    val coroutineScope = rememberCoroutineScope()


    // --- Classic Sign-In launcher (fallback) ---
    val classicLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        coroutineScope.launch {
            Log.d("LoginScreen", "Classic launcher callback triggered. Result code: ${result.resultCode}")

            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                try {
                    // Step 1: Get SignInResult from Google
                    val signInResult = googleAuthClient.signInWithIntent(result.data!!)

                    // Step 2: Handle sign-in via ViewModel
                    authViewModel.handleGoogleSignInResult(signInResult)

                    // Step 3: Observe userData to navigate only when Firebase confirms sign-in
                    authViewModel.userData.collect { user ->
                        if (user != null) {
                            Log.d("LoginScreen", "Google user signed in: ${user.email}")
                            navController.navigate("home") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    }

                } catch (e: Exception) {
                    Log.e("LoginScreen", "Classic Google sign-in failed", e)
                    Toast.makeText(
                        context,
                        "Google Sign-In failed: ${e.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                Log.d("LoginScreen", "Classic Google Sign-In cancelled or data is null: ${result.data}")
                Toast.makeText(context, "Google Sign-In cancelled", Toast.LENGTH_SHORT).show()
            }
        }
    }
// Inside LoginScreen Composable
    val userData by authViewModel.userData.collectAsState()

    LaunchedEffect(userData) {
        if (userData != null && navController.currentDestination?.route != "main") {
            Log.d("LoginScreen", "Navigating to main because user is signed in: ${userData!!.email}")
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }


    // --- Google Sign-In button click ---
    val onGoogleSignInClick: () -> Unit = {
        Log.d("LoginScreen", "Google Sign-In button clicked")
        coroutineScope.launch {
            try {
                val intentSender = googleAuthClient.signIn()
                if (intentSender != null) {
                    Log.d("LoginScreen", "Launching One Tap Sign-In")
                    googleSignInLauncher.launch(
                        IntentSenderRequest.Builder(intentSender).build()
                    )
                } else {
                    Log.d("LoginScreen", "One Tap not available, using classic launcher")
                    val fallbackIntent = googleAuthClient.getSignInIntent()
                    classicLauncher.launch(fallbackIntent)
                }
            } catch (e: Exception) {
                Log.e("LoginScreen", "Google Sign-In error", e)
                Toast.makeText(
                    context,
                    "Google Sign-In failed: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }


    // --- Scaffold Layout ---
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars
    ) { innerPadding ->

        val rootModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .clip(RoundedCornerShape(topStart = 15.dp, topEnd = 15.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(horizontal = 16.dp, vertical = 24.dp)
            .consumeWindowInsets(WindowInsets.navigationBars)

        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)

        // --- Adaptive UI layout ---
        when (deviceConfiguration) {
            DeviceConfiguration.MOBILE_PORTRAIT -> {
                Column(
                    modifier = rootModifier.background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    LoginHeader()
                    LoginFormSection(
                        emailText = emailText,
                        onEmailTextChange = { emailText = it },
                        passwordText = passwordText,
                        onPasswordTextChange = { passwordText = it },
                        navController = navController,
                        authViewModel = authViewModel,
                        onGoogleSignInClick = onGoogleSignInClick,
                        isLoading = isLoading,
                        errorMessage = errorMessage
                    )
                }
            }
            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                Row(
                    modifier = rootModifier.padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    LoginHeader(modifier = Modifier.weight(1f))
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
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
            else -> {
                Column(
                    modifier = rootModifier
                        .verticalScroll(rememberScrollState())
                        .padding(top = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoginHeader(
                        modifier = Modifier.widthIn(max = 540.dp),
                        alignment = Alignment.CenterHorizontally
                    )
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
                        modifier = Modifier.widthIn(max = 540.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun LoginHeader(
    alignment: Alignment.Horizontal = Alignment.Start,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = alignment) {
        Text(text = "Log in", style = MaterialTheme.typography.titleLarge)
        Text(text = "Log in to get started", style = MaterialTheme.typography.bodyLarge)
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
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = modifier) {
        // --- Email field ---
        TextFieldLong(
            text = emailText,
            onValueChange = onEmailTextChange,
            label = "Email",
            hint = "example@example.com",
            isTextSecret = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Password field ---
        TextFieldLong(
            text = passwordText,
            onValueChange = onPasswordTextChange,
            label = "Password",
            hint = "Password",
            isTextSecret = true,
            modifier = Modifier.fillMaxWidth()
        )

        // --- Show error message if present ---
        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Login Button ---
        LongButton(
            text = if (isLoading) "Logging in..." else "Log in",
            onClick = {
                coroutineScope.launch {
                    try {
                        Log.d("LoginScreen", "Attempting login for email: $emailText")
                        authViewModel.login(emailText, passwordText) {
                            Log.d("LoginScreen", "Login successful. Navigating to main screen.")
                            Toast.makeText(context, "Welcome back!", Toast.LENGTH_SHORT).show()
                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("LoginScreen", "Login failed: ${e.message}", e)
                        Toast.makeText(context, "Login failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        // --- Navigate to Sign Up ---
        LinkButton(
            text = "Don't have an account?",
            onClick = { navController.navigate("signUp") },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- Google Sign-In Button ---
        GoogleBtn(
            text = "Log in Using Google",
            onClick = onGoogleSignInClick,
            modifier = Modifier.fillMaxWidth(),
            imageRes = student.projects.jetpackpam.R.drawable.google_logo
        )

    }
}
