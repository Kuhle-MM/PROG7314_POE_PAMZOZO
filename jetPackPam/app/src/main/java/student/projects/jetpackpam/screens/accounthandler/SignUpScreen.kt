package student.projects.jetpackpam.screens.accounthandler


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import student.projects.jetpackpam.design_system.GoogleBtn
import student.projects.jetpackpam.design_system.LinkButton
import student.projects.jetpackpam.design_system.LongButton
import student.projects.jetpackpam.design_system.TextFieldLong
import student.projects.jetpackpam.util.DeviceConfiguration



@Composable
fun SignUpScreen() {

    // its the same here too
    var emailText by remember{ mutableStateOf("") }
    var passwordText by remember{ mutableStateOf("") }
    var nameText by remember{ mutableStateOf("") }
    var surnameText by remember{ mutableStateOf("") }
    var confirmPasswordText by remember{ mutableStateOf("") }
    var phoneNumberText by remember{ mutableStateOf("") }
    Scaffold (
        modifier = Modifier
            .fillMaxSize(),
        contentWindowInsets = WindowInsets.statusBars
    ){innerPadding ->
        //dynamic variables
        val rootModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .clip(
                RoundedCornerShape(
                    topStart = 15.dp,
                    topEnd = 15.dp
                )
            )
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(
                horizontal = 16.dp,
                vertical = 24.dp

            )
            .consumeWindowInsets(WindowInsets.navigationBars) // so that the column doesn't overlap


        //This part of the code deals with the adaptiveness of the screen
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val deviceConfiguration = DeviceConfiguration.fromWindowSizeClass(windowSizeClass)
        when(deviceConfiguration){
            DeviceConfiguration.MOBILE_PORTRAIT -> {
                Column (
                    modifier = rootModifier
                        .verticalScroll(rememberScrollState())
                        .background(MaterialTheme.colorScheme.background),
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ){
                    SignUpHeader(
                        modifier = Modifier.fillMaxWidth()
                    )
                    SignUpFormSection(
                        emailText = emailText,
                        onEmailTextChange = {emailText = it},
                        passwordText = passwordText,
                        onPasswordTextChange = {passwordText = it},
                        nameText = nameText,
                        onNameTextChange = {nameText = it},
                        surnameText = surnameText,
                        onSurnameTextChange = {surnameText = it},
                        confirmPasswordText = confirmPasswordText,
                        onConfirmPasswordChange =   {confirmPasswordText = it},
                        phonNumberText = phoneNumberText,
                        onPhoneNumberTextChange = {phoneNumberText = it},
                        modifier = Modifier
                            .fillMaxWidth(),
                    )
                }

            }
            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                Row (
                    modifier = rootModifier
                        .windowInsetsPadding(WindowInsets.displayCutout)
                        .padding(horizontal = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(32.dp)
                ){
                    SignUpHeader(
                        modifier = Modifier
                            .weight(1f)
                    )
                    SignUpFormSection(
                        emailText = emailText,
                        onEmailTextChange = {emailText = it},
                        passwordText = passwordText,
                        onPasswordTextChange = {passwordText = it},
                        nameText = nameText,
                        onNameTextChange = {nameText = it},
                        surnameText = surnameText,
                        onSurnameTextChange = {surnameText = it},
                        confirmPasswordText = confirmPasswordText,
                        onConfirmPasswordChange =   {confirmPasswordText = it},
                        phonNumberText = phoneNumberText,
                        onPhoneNumberTextChange = {phoneNumberText = it},
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                    )
                }

            }
            DeviceConfiguration.TABLET_PORTRAIT,
            DeviceConfiguration.TABLET_LANDSCAPE,
            DeviceConfiguration.DESKTOP -> {
                Column (
                    modifier = rootModifier
                        .verticalScroll(rememberScrollState())
                        .padding(top = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    SignUpHeader(
                        modifier = Modifier
                            .widthIn(max = 540.dp),
                        alignment = Alignment.CenterHorizontally
                    )
                    SignUpFormSection(
                        emailText = emailText,
                        onEmailTextChange = {emailText = it},
                        passwordText = passwordText,
                        onPasswordTextChange = {passwordText = it},
                        nameText = nameText,
                        onNameTextChange = {nameText = it},
                        surnameText = surnameText,
                        onSurnameTextChange = {surnameText = it},
                        confirmPasswordText = confirmPasswordText,
                        onConfirmPasswordChange =   {confirmPasswordText = it},
                        phonNumberText = phoneNumberText,
                        onPhoneNumberTextChange = {phoneNumberText = it},
                        modifier = Modifier
                            .widthIn(max = 540.dp),
                    )
                }

            }
        }


    }

}

@Composable
fun SignUpHeader(
    alignment: Alignment.Horizontal = Alignment.Start,
    modifier: Modifier = Modifier
)
{
    Column(
        modifier = modifier,
        horizontalAlignment = alignment
    ){
        Text(
            text = "Sign Up",
            style = MaterialTheme.typography.titleLarge,

        )
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
    modifier: Modifier = Modifier,
    nameText: String,
    onNameTextChange: (String) -> Unit,
    surnameText: String,
    onSurnameTextChange: (String) -> Unit,
    confirmPasswordText: String,
    onConfirmPasswordChange: (String) -> Unit,
    phonNumberText: String,
    onPhoneNumberTextChange: (String) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier =modifier
        ){
            TextFieldLong(
                text = nameText,
                onValueChange = onNameTextChange,
                label = "Name",
                hint = "John",
                isTextSecret = false,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            TextFieldLong(
                text = surnameText,
                onValueChange = onSurnameTextChange,
                label = "Surname",
                hint = "Doe",
                isTextSecret = false,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f)
            )
        }
        TextFieldLong(
            text = emailText,
            onValueChange = onEmailTextChange,
            label = "Email",
            hint = "example@example.com",
            isTextSecret = false,
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextFieldLong(
            text = phonNumberText,
            onValueChange = onPhoneNumberTextChange,
            label = "Phone",
            hint = "0672221234",
            isTextSecret = false,
            modifier = Modifier
                .fillMaxWidth()

        )
        Spacer(modifier = Modifier.height(16.dp))
        TextFieldLong(
            text = passwordText,
            onValueChange = onPasswordTextChange,
            label = "Password",
            hint = "Password",
            isTextSecret = true,
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        TextFieldLong(
            text = confirmPasswordText,
            onValueChange = onConfirmPasswordChange,
            label = "Confirm Password",
            hint = "Confirm Password",
            isTextSecret = true,
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        LongButton(
            text = "Sign Up",
            onClick = {
                //code here for sso
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        LinkButton(
            text = "You already have a profile?",
            onClick = {},
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(12.dp))
        GoogleBtn(
            text = "Log in Using Google",
            onClick = {
                //code here for sso
            },
            modifier = Modifier.fillMaxWidth(),
            imageRes =  student.projects.jetpackpam.R.drawable.google_logo
        )
    }
}