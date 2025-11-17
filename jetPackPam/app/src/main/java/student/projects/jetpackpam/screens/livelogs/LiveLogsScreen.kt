package student.projects.jetpackpam.screens.livelogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Timestamp
import student.projects.jetpackpam.data.LogEntry
import java.text.SimpleDateFormat
import java.util.*

/* -------------------------------------------------------
   COLORS (LinkedIn-like Pale)
-------------------------------------------------------- */
val paleBackground = Color(0xFFF4F4F4)
val paleCard = Color(0xFFFFFFFF)
val palePrimary = Color(0xFF0077B5)
val paleSecondary = Color(0xFFE8E8E8)

/* -------------------------------------------------------
   HARDCODED LOG ENTRIES
-------------------------------------------------------- */
val sampleLogs = listOf(
    LogEntry(1, "Turning left at corner", Timestamp(Date(2025 - 1900, 10, 17, 9, 15))),
    LogEntry(2, "Going straight for 50cm", Timestamp(Date(2025 - 1900, 10, 17, 9, 20))),
    LogEntry(3, "Stopped briefly", Timestamp(Date(2025 - 1900, 10, 17, 10, 5))),
    LogEntry(4, "Turning right at intersection", Timestamp(Date(2025 - 1900, 10, 17, 10, 30))),
    LogEntry(5, "Going straight for 100cm", Timestamp(Date(2025 - 1900, 10, 16, 14, 40))),
    LogEntry(6, "Turning left at T-junction", Timestamp(Date(2025 - 1900, 10, 16, 15, 10)))
)

/* -------------------------------------------------------
   MAIN SCREEN
-------------------------------------------------------- */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveLogsScreen(navController: NavController) {

    /* ---------------- FILTER STATE ---------------- */
    var selectedDate: Date? by remember { mutableStateOf(null) }
    var selectedTimeBlock: Int? by remember { mutableStateOf(null) }   // 10-min buckets
    var selectedKeyword: String by remember { mutableStateOf("") }

    /* Bottom sheets visibility */
    var dateSheet by remember { mutableStateOf(false) }
    var timeSheet by remember { mutableStateOf(false) }
    var keywordSheet by remember { mutableStateOf(false) }

    val df = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }

    /* ---------------- FILTER LOGIC ---------------- */
    val filteredLogs = remember(selectedDate, selectedTimeBlock, selectedKeyword) {
        sampleLogs.sortedByDescending { it.timestamp.seconds }.filter { entry ->

            val entryDate = Date(entry.timestamp.seconds * 1000)
            val cal = Calendar.getInstance().apply { time = entryDate }

            val dateMatch = selectedDate?.let {
                val f = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                f.format(entryDate) == f.format(it)
            } ?: true

            val timeMatch = selectedTimeBlock?.let { block ->
                (cal.get(Calendar.MINUTE) / 10) == block
            } ?: true

            val keywordMatch = if (selectedKeyword.isBlank()) true
            else entry.message.contains(selectedKeyword, ignoreCase = true)

            dateMatch && timeMatch && keywordMatch
        }
    }

    /* ---------------- UI LAYOUT ---------------- */
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(paleBackground)
            .padding(12.dp)
    ) {

        Text(
            "Live Logs",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        /* ---------------- FILTER CHIPS ---------------- */
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = dateSheet,
                onClick = { dateSheet = true },
                label = { Text("Date") }
            )

            FilterChip(
                selected = timeSheet,
                onClick = { timeSheet = true },
                label = { Text("Time") }
            )

            FilterChip(
                selected = keywordSheet,
                onClick = { keywordSheet = true },
                label = { Text("Keyword") }
            )
        }

        Spacer(Modifier.height(12.dp))

        /* Reset Button */
        OutlinedButton(
            onClick = {
                selectedDate = null
                selectedTimeBlock = null
                selectedKeyword = ""
            }
        ) { Text("Reset Filters") }

        Spacer(Modifier.height(16.dp))

        /* ---------------- LOG LIST ---------------- */
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredLogs) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = paleCard)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(entry.message, fontWeight = FontWeight.Bold)
                        Text(df.format(Date(entry.timestamp.seconds * 1000L)))
                    }
                }
            }
        }
    }

    /* ---------------- SHEETS ---------------- */

    if (dateSheet) {
        BottomSheetDialog(
            title = "Filter by Date",
            items = sampleLogs
                .map { Date(it.timestamp.seconds * 1000) }
                .distinctBy {
                    SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(it)
                }
                .sortedByDescending { it }
                .map {
                    SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(it) to it
                },
            onSelect = { selectedDate = it },
            onDismiss = { dateSheet = false }
        )
    }

    if (timeSheet) {
        BottomSheetDialog(
            title = "Filter by Time (10-minute blocks)",
            items = (0..5).map { block ->
                val label = "${block * 10}–${block * 10 + 9} min"
                label to block
            },
            onSelect = { selectedTimeBlock = it },
            onDismiss = { timeSheet = false }
        )
    }

    if (keywordSheet) {
        KeywordSheet(
            currentValue = selectedKeyword,
            onApply = { selectedKeyword = it },
            onDismiss = { keywordSheet = false }
        )
    }
}

/* -------------------------------------------------------
   REUSABLE BOTTOM SHEET
-------------------------------------------------------- */
@Composable
fun <T> BottomSheetDialog(
    title: String,
    items: List<Pair<String, T>>,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = paleSecondary
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Text(title, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            items.forEach { (label, value) ->
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelect(value); onDismiss() }
                ) {
                    Text(label)
                }
                Spacer(Modifier.height(8.dp))
            }

            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = onDismiss) {
                Text("Cancel")
            }
        }
    }
}

/* -------------------------------------------------------
   KEYWORD ENTRY SHEET
-------------------------------------------------------- */
@Composable
fun KeywordSheet(
    currentValue: String,
    onApply: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(currentValue) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = paleSecondary
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            Text("Filter by Keyword", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Enter keyword") }
            )

            Spacer(Modifier.height(16.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onApply(text); onDismiss() }
            ) {
                Text("Apply")
            }

            Spacer(Modifier.height(10.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onDismiss
            ) {
                Text("Cancel")
            }
        }
    }
}
