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
import kotlinx.coroutines.launch
import student.projects.jetpackpam.design_system.GoogleBtn
import student.projects.jetpackpam.design_system.LinkButton
import student.projects.jetpackpam.design_system.LongButton
import student.projects.jetpackpam.design_system.TextFieldLong
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.util.DeviceConfiguration

@Composable
fun SignUpScreen(navController: NavController, authViewModel: AuthorizationModelViewModel) {
    val scope = rememberCoroutineScope()

    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var nameText by remember { mutableStateOf("") }
    var surnameText by remember { mutableStateOf("") }
    var confirmPasswordText by remember { mutableStateOf("") }
    var phoneNumberText by remember { mutableStateOf("") }

    // ✅ Collect state from ViewModel (optional if you want to show success/error)
    val isSignedUp by authViewModel.isSignedUp.collectAsState(initial = false)

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
                    modifier = rootModifier
                        .verticalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    SignUpHeader(modifier = Modifier.fillMaxWidth())
                    SignUpFormSection(
                        emailText = emailText,
                        onEmailTextChange = { emailText = it },
                        passwordText = passwordText,
                        onPasswordTextChange = { passwordText = it },
                        nameText = nameText,
                        onNameTextChange = { nameText = it },
                        surnameText = surnameText,
                        onSurnameTextChange = { surnameText = it },
                        confirmPasswordText = confirmPasswordText,
                        onConfirmPasswordChange = { confirmPasswordText = it },
                        phonNumberText = phoneNumberText,
                        onPhoneNumberTextChange = { phoneNumberText = it },
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
                    SignUpHeader(modifier = Modifier.weight(1f))
                    SignUpFormSection(
                        emailText = emailText,
                        onEmailTextChange = { emailText = it },
                        passwordText = passwordText,
                        onPasswordTextChange = { passwordText = it },
                        nameText = nameText,
                        onNameTextChange = { nameText = it },
                        surnameText = surnameText,
                        onSurnameTextChange = { surnameText = it },
                        confirmPasswordText = confirmPasswordText,
                        onConfirmPasswordChange = { confirmPasswordText = it },
                        phonNumberText = phoneNumberText,
                        onPhoneNumberTextChange = { phoneNumberText = it },
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
                    SignUpHeader(
                        modifier = Modifier.widthIn(max = 540.dp),
                        alignment = Alignment.CenterHorizontally
                    )
                    SignUpFormSection(
                        emailText = emailText,
                        onEmailTextChange = { emailText = it },
                        passwordText = passwordText,
                        onPasswordTextChange = { passwordText = it },
                        nameText = nameText,
                        onNameTextChange = { nameText = it },
                        surnameText = surnameText,
                        onSurnameTextChange = { surnameText = it },
                        confirmPasswordText = confirmPasswordText,
                        onConfirmPasswordChange = { confirmPasswordText = it },
                        phonNumberText = phoneNumberText,
                        onPhoneNumberTextChange = { phoneNumberText = it },
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
fun SignUpHeader(alignment: Alignment.Horizontal = Alignment.Start, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = alignment) {
        Text(text = "Sign Up", style = MaterialTheme.typography.titleLarge)
        Text(text = "Enter the fields to create your new profile", style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun SignUpFormSection(
    emailText: String,
    onEmailTextChange: (String) -> Unit,
    passwordText: String,
    onPasswordTextChange: (String) -> Unit,
    nameText: String,
    onNameTextChange: (String) -> Unit,
    surnameText: String,
    onSurnameTextChange: (String) -> Unit,
    confirmPasswordText: String,
    onConfirmPasswordChange: (String) -> Unit,
    phonNumberText: String,
    onPhoneNumberTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthorizationModelViewModel
) {
    Column(modifier = modifier) {
        Row(modifier = Modifier.fillMaxWidth()) {
            TextFieldLong(
                text = nameText,
                onValueChange = onNameTextChange,
                label = "Name",
                hint = "John",
                isTextSecret = false,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            TextFieldLong(
                text = surnameText,
                onValueChange = onSurnameTextChange,
                label = "Surname",
                hint = "Doe",
                isTextSecret = false,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
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
            text = phonNumberText,
            onValueChange = onPhoneNumberTextChange,
            label = "Phone",
            hint = "0672221234",
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

        Spacer(modifier = Modifier.height(16.dp))
        TextFieldLong(
            text = confirmPasswordText,
            onValueChange = onConfirmPasswordChange,
            label = "Confirm Password",
            hint = "Confirm Password",
            isTextSecret = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
        LongButton(
            text = "Sign Up",
            onClick = {
                // ✅ Call ViewModel signUp
                authViewModel.signUp(
                    nameText,
                    surnameText,
                    emailText,
                    phonNumberText,
                    passwordText,
                    confirmPasswordText
                )
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        LinkButton(
            text = "You already have a profile?",
            onClick = { navController.navigate("login") },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(24.dp))
        GoogleBtn(
            text = "Log in Using Google",
            onClick = { /* SSO code */ },
            modifier = Modifier.fillMaxWidth(),
            imageRes = student.projects.jetpackpam.R.drawable.google_logo
        )
    }
}
