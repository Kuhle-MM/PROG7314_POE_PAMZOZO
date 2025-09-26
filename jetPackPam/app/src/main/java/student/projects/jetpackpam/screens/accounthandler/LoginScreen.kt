package student.projects.jetpackpam.screens.accounthandler

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import student.projects.jetpackpam.design_system.GoogleBtn
import student.projects.jetpackpam.design_system.LinkButton
import student.projects.jetpackpam.design_system.LongButton
import student.projects.jetpackpam.design_system.TextFieldLong
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.util.DeviceConfiguration

@Composable
fun LoginScreen(navController: NavController, authViewModel: AuthorizationModelViewModel) {
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }


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

        when (deviceConfiguration) {
            DeviceConfiguration.MOBILE_PORTRAIT -> {
                Column(
                    modifier = rootModifier.background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    LoginHeader(modifier = Modifier.fillMaxWidth())
                    LoginFormSection(
                        emailText = emailText,
                        onEmailTextChange = { emailText = it },
                        passwordText = passwordText,
                        onPasswordTextChange = { passwordText = it },
                        modifier = Modifier.fillMaxWidth(),
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }
            }
            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                Row(
                    modifier = rootModifier
                        .windowInsetsPadding(WindowInsets.displayCutout)
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    LoginHeader(modifier = Modifier.weight(1f))
                    LoginFormSection(
                        emailText = emailText,
                        onEmailTextChange = { emailText = it },
                        passwordText = passwordText,
                        onPasswordTextChange = { passwordText = it },
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }
            }
            DeviceConfiguration.TABLET_PORTRAIT,
            DeviceConfiguration.TABLET_LANDSCAPE,
            DeviceConfiguration.DESKTOP -> {
                Column(
                    modifier = rootModifier
                        .verticalScroll(rememberScrollState())
                        .padding(top = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LoginHeader(modifier = Modifier.widthIn(max = 540.dp), alignment = Alignment.CenterHorizontally)
                    LoginFormSection(
                        emailText = emailText,
                        onEmailTextChange = { emailText = it },
                        passwordText = passwordText,
                        onPasswordTextChange = { passwordText = it },
                        modifier = Modifier.widthIn(max = 540.dp),
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun LoginHeader(alignment: Alignment.Horizontal = Alignment.Start, modifier: Modifier = Modifier) {
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
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthorizationModelViewModel
) {
    val isLoading by remember { derivedStateOf { authViewModel.isLoading } }
    val errorMessage by remember { derivedStateOf { authViewModel.errorMessage } }

    Column(modifier = modifier) {
        TextFieldLong(
            text = emailText,
            onValueChange = onEmailTextChange,
            label = "Email",
            hint = "example@example.com",
            isTextSecret = false,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextFieldLong(
            text = passwordText,
            onValueChange = onPasswordTextChange,
            label = "Password",
            hint = "Password",
            isTextSecret = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Error message
        errorMessage?.let { msg ->
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login button
        LongButton(
            text = if (isLoading) "Logging in..." else "Log in",
            onClick = {
                authViewModel.login(emailText, passwordText) {
                    // Navigate to main app (bottom navigation) on success
                    navController.navigate("main") { // 👈 changed from "home" to "main"
                        popUpTo("login") { inclusive = true }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))
        LinkButton(
            text = "Don't have an account?",
            onClick = { navController.navigate("signUp") },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(24.dp))
        GoogleBtn(
            text = "Log in Using Google",
            onClick = { /* SSO login */ },
            modifier = Modifier.fillMaxWidth(),
            imageRes = student.projects.jetpackpam.R.drawable.google_logo
        )
    }
}
