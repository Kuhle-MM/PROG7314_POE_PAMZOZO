package student.projects.jetpackpam.screens.accounthandler.authorization

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import student.projects.jetpackpam.screens.accounthandler.authorization.BiometricPrefs
import student.projects.jetpackpam.screens.accounthandler.authorization.BiometricPromptActivity
import student.projects.jetpackpam.models.AuthorizationModelViewModel


/**
 * Show a small button to toggle biometric login.
 *
 * Usage:
 *   BiometricToggleButton(authViewModel = authViewModel)
 *
 * It will:
 *  - Read BiometricPrefs to know current state
 *  - When enabling: launch BiometricPromptActivity to verify biometric works, then persist and update ViewModel
 *  - When disabling: clear pref and update ViewModel
 */
@Composable
fun BiometricToggleButton(authViewModel: AuthorizationModelViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(BiometricPrefs.isBiometricEnabled(context)) }


    // launcher for the BiometricPrompt test activity
    val biometricLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // success: persist and update viewmodel
            BiometricPrefs.setBiometricEnabled(context, true)
            authViewModel.setBiometricEnabled(context, true)
            enabled = true
            Toast.makeText(context, "Biometric enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Biometric authentication failed/cancelled", Toast.LENGTH_SHORT).show()
        }
    }


    Row(modifier = modifier) {
        if (!enabled) {
            Button(onClick = {
                // Before enabling, check device capability
                if (!authViewModel.isBiometricAvailable(context)) {
                    Toast.makeText(context, "Biometrics not available on this device", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val intent = BiometricPromptActivity.createIntent(context, title = "Enable biometric login", subtitle = "Confirm to enable")
                biometricLauncher.launch(intent)
            }) {
                Text("Enable Biometrics")
            }
        } else {
            Button(onClick = {
                // Disable
                BiometricPrefs.setBiometricEnabled(context, false)
                authViewModel.setBiometricEnabled(context, false)
                enabled = false
                Toast.makeText(context, "Biometric disabled", Toast.LENGTH_SHORT).show()
            }) {
                Text("Disable Biometrics")
            }
        }


        Spacer(modifier = Modifier.width(8.dp))
        Text(text = if (enabled) "Enabled" else "Disabled")
    }
}
