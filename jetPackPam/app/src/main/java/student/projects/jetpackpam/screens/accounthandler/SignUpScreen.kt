package student.projects.jetpackpam.screens.accounthandler

import android.content.ContentValues.TAG
import android.util.Log
import android.widget.Toast
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
import student.projects.jetpackpam.util.DeviceConfiguration

@Composable
fun SignUpScreen(navController: NavController, authViewModel: AuthorizationModelViewModel) {
    val TAG = "SignUpDebug"
    // UI state
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var confirmPasswordText by remember { mutableStateOf("") }
    var nameText by remember { mutableStateOf("") }
    var surnameText by remember { mutableStateOf("") }
    var phoneNumberText by remember { mutableStateOf("") }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Collect ViewModel state
    val isLoading by authViewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by authViewModel.errorMessage.collectAsStateWithLifecycle()
    val signUpSuccess by authViewModel.signUpSuccess.collectAsStateWithLifecycle()

    // Navigate to login when signup is successful
    LaunchedEffect(signUpSuccess) {
        if (signUpSuccess) {
            Log.d(TAG, "Sign-up success observed in Composable")
            Toast.makeText(context, "Sign up successful!", Toast.LENGTH_LONG).show()
            navController.navigate("login") {
                popUpTo("login") { inclusive = false }
                launchSingleTop = true
            }
            authViewModel.resetSignUpState()
        }
    }

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
            .verticalScroll(rememberScrollState())

        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)
        when (deviceConfiguration) {
            DeviceConfiguration.MOBILE_PORTRAIT -> {
                Column(
                    modifier = rootModifier,
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
                        context = context
                    )
                        Spacer(modifier = Modifier.height(10.dp))
                    // Show error message
                    errorMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                Row(
                    modifier = rootModifier
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Column(
                        modifier = rootModifier,
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
                            context = context
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        // Show error message
                        errorMessage?.let { msg ->
                            Text(
                                text = msg,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
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
                    Column(
                        modifier = rootModifier,
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
                            context = context
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        // Show error message
                        errorMessage?.let { msg ->
                            Text(
                                text = msg,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
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
    context: android.content.Context
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {

        // Name & Surname fields
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
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

        // Sign Up button with coroutine
        LongButton(
            text = if (isLoading) "Signing up..." else "Sign Up",
            onClick = {
                Log.d(TAG, "Sign-up button clicked")
                coroutineScope.launch {
                    try {
                        authViewModel.signUp(
                            nameText,
                            surnameText,
                            emailText,
                            passwordText,
                            confirmPasswordText,
                        ) {
                            Log.d(TAG, "onSuccess lambda triggered")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Sign-up exception: ${e.localizedMessage}")
                        Toast.makeText(context, "Sign up failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        )

        // Navigate to login
        LinkButton(
            text = "You already have a profile?",
            onClick = { navController.navigate("login") },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Google SSO placeholder
        GoogleBtn(
            text = "Log in Using Google",
            onClick = {
                // TODO: Implement Google SSO here
            },
            modifier = Modifier.fillMaxWidth(),
            imageRes = student.projects.jetpackpam.R.drawable.google_logo
        )
    }
}
