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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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

@Composable
fun PersonalitySelectionScreen()
{
    val context = LocalContext.current
    val personalities = listOf(
        "Sarcastic",
        "Friendly",
        "Gen Z",
        "Never in the mood",
        "Motivational Coach",
        "Wise Elder",
        "Cheerful Optimist",
        "Storyteller",
        "Shakespearean",
        "Tech Geek",
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 50.dp), // gap from top
        horizontalAlignment = Alignment.CenterHorizontally // center horizontally
    ) {
        // Top Heading
        PersonalityHeader()
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
        items(personalities) { personalities ->
            OutlinedCard(
                onClick = {
                    //sets whole app in selected language
                    Toast.makeText(context, "Clicked on $personalities", Toast.LENGTH_SHORT).show()
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
                        text = personalities,
                        textAlign = TextAlign.Center,
                        fontSize = 29.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PersonalityHeader(){
    Text(
        text = "Choose my personality",
        fontStyle = FontStyle.Italic,
        fontSize = 35.sp
    )
}