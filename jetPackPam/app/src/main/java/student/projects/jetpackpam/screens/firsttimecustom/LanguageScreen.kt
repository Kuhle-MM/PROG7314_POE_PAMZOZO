package student.projects.jetpackpam.screens.firsttimecustom

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LanguageSelectionScreen() {
    val context = LocalContext.current
    val languages = listOf(
        "English",
        "Afrikaans",
        "isiZulu"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp), // gap from top
        horizontalAlignment = Alignment.CenterHorizontally // center horizontally
    ) {
        // Top Heading
        LanguageHeader()
    }
    //Option carousel
    LazyRow(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(22.dp),
        verticalAlignment = Alignment.CenterVertically
    )
    {
        items(languages) { language ->
            OutlinedCard(
                onClick = {
                    //sets whole app in selected language
                    Toast.makeText(context, "Clicked on $language", Toast.LENGTH_SHORT).show()
                },
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color.Transparent
                ),
                border = BorderStroke(7.dp, Color(0xFFF0A1F8)),
                modifier = Modifier.size(250.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = language,
                        textAlign = TextAlign.Center,
                        fontSize = 29.sp
                    )
                }
            }
        }
    }
}

@Composable
fun LanguageHeader(){
    Text(
        text = "Select preferred language",
        fontStyle = FontStyle.Italic,
        fontSize = 35.sp
    )

}
