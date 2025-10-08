package student.projects.jetpackpam.screens.firsttimecustom

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import student.projects.jetpackpam.util.DeviceConfiguration
import java.util.UUID

@Composable
fun PersonalitySelectionScreen2() {
    val context = LocalContext.current
    val personalities = listOf(
        "Sarcastic", "Friendly", "Gen Z", "Never in the mood", "Motivational Coach",
        "Wise Elder", "Cheerful Optimist", "Storyteller", "Shakespearean", "Tech Geek"
    )

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
                PersonalityGridLayout(
                    personalities = personalities,
                    context = context,
                    modifier = rootModifier,
                    columns = 2,
                    cardSize = 160.dp,
                    fontSize = 20.sp,
                    horizontalSpacing = 16.dp,
                    verticalSpacing = 16.dp
                )
            }

            DeviceConfiguration.MOBILE_LANDSCAPE -> {
                PersonalityGridLayout(
                    personalities = personalities,
                    context = context,
                    modifier = rootModifier,
                    columns = 3,
                    cardSize = 140.dp,
                    fontSize = 18.sp,
                    horizontalSpacing = 12.dp,
                    verticalSpacing = 12.dp
                )
            }

            else -> {
                PersonalityGridLayout(
                    personalities = personalities,
                    context = context,
                    modifier = rootModifier,
                    columns = 4,
                    cardSize = 200.dp,
                    fontSize = 24.sp,
                    horizontalSpacing = 20.dp,
                    verticalSpacing = 20.dp
                )
            }
        }
    }
}

@Composable
private fun PersonalityGridLayout(
    personalities: List<String>,
    context: Context,
    modifier: Modifier = Modifier,
    columns: Int,
    cardSize: Dp,
    fontSize: TextUnit,
    horizontalSpacing: Dp,
    verticalSpacing: Dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        PersonalityHeader2()
        Spacer(modifier = Modifier.height(32.dp))

        // Adaptive Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            modifier = Modifier.fillMaxSize()
        ) {
                    items(personalities) { personality ->
                        OutlinedCard(
                            onClick = {
                                // 1. Get current user UID
                                val user = FirebaseAuth.getInstance().currentUser
                                if (user != null) {
                                    val uid = user.uid

                                    // 2. Create a unique preference ID
                                    val preferenceId = UUID.randomUUID().toString()

                                    // 3. Build preference map
                                    val preferenceData = mapOf(
                                        "preferenceName" to personality,
                                        "preferenceId" to preferenceId
                                    )

                                    // 4. Push to Firebase under /Users/uid/preference/preferenceId
                                    val dbRef = FirebaseDatabase.getInstance()
                                        .getReference("Users")
                                        .child(uid)
                                        .child("preference")
                                        .child(preferenceId)

                                    dbRef.setValue(preferenceData)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "$personality saved!", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, "Failed: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                } else {
                                    Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(30.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                            border = BorderStroke(7.dp, Color(0xFFF0A1F8)),
                            modifier = Modifier.size(cardSize)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = personality,
                                    textAlign = TextAlign.Center,
                                    fontSize = fontSize
                                )
                            }
                        }
                    }

        }
    }
}


@Composable
fun PersonalityHeader2(){
    Text(
        text = "Choose my personality",
        fontStyle = FontStyle.Italic,
        style = MaterialTheme.typography.titleLarge
    )
}