package student.projects.jetpackpam.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import student.projects.jetpackpam.data.local.OfflineRepository

@Composable
fun SettingsBiometricsScreen(
    navController: NavController,
    offlineRepo: OfflineRepository,
    uid: String
) {

    var biometricsEnabled by remember { mutableStateOf(false) }
    val PinkAccent = Color(0xFFE34FF2)

    // Load saved value on Composable launch
    LaunchedEffect(Unit) {
        try {
            val savedItems = offlineRepo.getUnsynced(uid)
            val saved = savedItems.find { it.dataKey == "biometrics" }
            saved?.let {
                biometricsEnabled = Gson().fromJson(it.jsonData, Boolean::class.java)
            }
        } catch (_: Exception) { /* ignore */ }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
    ) {

        Text(
            "Biometrics",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9C8FE)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Enable Biometrics", fontSize = 18.sp)
                Switch(
                    checked = biometricsEnabled,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PinkAccent
                    ),
                    onCheckedChange = { newValue ->
                        biometricsEnabled = newValue

                        // Save offline immediately

                            try {
                                // Debugging: log the new value
                                println("Saving biometrics: $newValue")

                                CoroutineScope(Dispatchers.IO).launch {
                                    // Save offline asynchronously
                                    offlineRepo.saveOffline(uid, "biometrics", newValue)
                                }
                            } catch (e: Exception) {
                                // Debugging: log save errors
                                println("Error saving biometrics: ${e.message}")
                            }

                    }
                )
            }
        }
    }
}
