package student.projects.jetpackpam.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import student.projects.jetpackpam.ui.theme.Surface
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import student.projects.jetpackpam.data.local.OfflineRepository

@Composable
fun SettingsLogsScreen(
    navController: NavController,
    offlineRepo: OfflineRepository,
    uid: String
) {

    val LightPink = Color(0xFFF9C8FE)
    val PurpleDeep = Color(0xFFA10DB0)

    val timingOptions = listOf("10 min", "20 min", "30 min", "1 hour", "2 hours")
    var selected by remember { mutableStateOf("10 min") }
    val coroutineScope = rememberCoroutineScope()

    // Load saved timing from offline storage
    LaunchedEffect(Unit) {
        try {
            val savedItems = offlineRepo.getUnsynced(uid)
            val saved = savedItems.find { it.dataKey == "log_timing" }
            saved?.let {
                selected = Gson().fromJson(it.jsonData, String::class.java)
            }
        } catch (_: Exception) { /* ignore errors */ }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
    ) {

        Text(
            "Log Timing Options",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Text("Choose how often logs update:")

        Spacer(Modifier.height(16.dp))

        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            timingOptions.forEach { option ->

                val isSelected = selected == option

                Surface(
                    modifier = Modifier.clickable {
                        selected = option

                        // Save offline immediately
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                offlineRepo.saveOffline(uid, "log_timing", option)
                            } catch (_: Exception) { /* ignore save errors */ }
                        }
                    },
                    shape = RoundedCornerShape(50),
                    color = if (isSelected) PurpleDeep else LightPink
                ) {
                    Text(
                        option,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        color = if (isSelected) Color.White else Color.Black
                    )
                }
            }
        }
    }
}
