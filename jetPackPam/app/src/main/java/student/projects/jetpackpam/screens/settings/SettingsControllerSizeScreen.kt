package student.projects.jetpackpam.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import student.projects.jetpackpam.data.local.OfflineRepository

@Composable
fun SettingsControllerSizeScreen(
    navController: NavController,
    offlineRepo: OfflineRepository,
    uid: String
) {
    val PurpleDeep = Color(0xFFA10DB0)
    var size by remember { mutableFloatStateOf(0.5f) }
    val coroutineScope = rememberCoroutineScope()

    // Load saved size from offline storage
    LaunchedEffect(Unit) {
        try {
            val savedItems = offlineRepo.getUnsynced(uid)
            val saved = savedItems.find { it.dataKey == "controller_size" }
            saved?.let {
                size = Gson().fromJson(it.jsonData, Float::class.java)
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
            "Controller Size",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 22.dp)
        )

        Text("Adjust the size of your remote controller:")

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size((120 + size * 140).dp)
                .background(PurpleDeep, RoundedCornerShape(24.dp))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(24.dp))

        Slider(
            value = size,
            onValueChange = { newValue ->
                size = newValue

                // Save offline immediately
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        offlineRepo.saveOffline(uid, "controller_size", newValue)
                    } catch (_: Exception) { /* ignore save errors */ }
                }
            },
            valueRange = 0.0f..1f
        )
    }
}
