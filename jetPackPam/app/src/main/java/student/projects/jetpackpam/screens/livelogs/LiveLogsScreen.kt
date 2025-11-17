package student.projects.jetpackpam.screens.livelogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import student.projects.jetpackpam.data.LogEntry
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ---------------------------
// Hardcoded LOGS LIST
// ---------------------------
val sampleLogs = listOf(
    // Same date, different times
    LogEntry(10, "turning", LocalDateTime.parse("2025-01-21T09:15")),
    LogEntry(9, "turning", LocalDateTime.parse("2025-01-21T09:16")),
    LogEntry(45, "going straight", LocalDateTime.parse("2025-01-21T10:00")),
    LogEntry(42, "turning", LocalDateTime.parse("2025-01-21T12:45")),
    LogEntry(45, "going straight", LocalDateTime.parse("2025-01-21T12:46")),
    LogEntry(80, "going straight", LocalDateTime.parse("2025-01-20T18:00")),
    LogEntry(30, "turning", LocalDateTime.parse("2025-01-19T16:20")),
    LogEntry(95, "going straight", LocalDateTime.parse("2025-01-18T12:12")),
    LogEntry(60, "turning", LocalDateTime.parse("2025-01-17T08:05"))
)

@Composable
fun LiveLogsScreen(navController: NavController) {

    // User filters
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }

    // Formatters
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    // Filtering logic
    val filteredLogs = remember(startDate, endDate, startTime, endTime) {

        val sorted = sampleLogs.sortedByDescending { it.dateTime } // newest first

        runCatching {

            // Parse dates
            val startDateParsed = if (startDate.isNotBlank()) LocalDate.parse(startDate, dateFormatter) else null
            val endDateParsed = if (endDate.isNotBlank()) LocalDate.parse(endDate, dateFormatter) else null

            // Parse times
            val startTimeParsed = if (startTime.isNotBlank()) LocalTime.parse(startTime, timeFormatter) else null
            val endTimeParsed = if (endTime.isNotBlank()) LocalTime.parse(endTime, timeFormatter) else null

            sorted.filter { log ->

                val logDate = log.dateTime.toLocalDate()
                val logTime = log.dateTime.toLocalTime()

                val dateMatch =
                    (startDateParsed == null || !logDate.isBefore(startDateParsed)) &&
                            (endDateParsed == null || !logDate.isAfter(endDateParsed))

                val timeMatch =
                    (startTimeParsed == null || !logTime.isBefore(startTimeParsed)) &&
                            (endTimeParsed == null || !logTime.isAfter(endTimeParsed))

                dateMatch && timeMatch
            }
        }.getOrElse { sorted }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Live Logs",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // -------------------------
        // DATE FILTERS
        // -------------------------
        OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("Start Date (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = endDate,
            onValueChange = { endDate = it },
            label = { Text("End Date (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // -------------------------
        // TIME FILTERS
        // -------------------------
        OutlinedTextField(
            value = startTime,
            onValueChange = { startTime = it },
            label = { Text("Start Time (HH:mm)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = endTime,
            onValueChange = { endTime = it },
            label = { Text("End Time (HH:mm)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        // -------------------------
        // LOG LIST
        // -------------------------
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(filteredLogs) { log ->
                LogCard(log)
            }
        }
    }
}

@Composable
fun LogCard(log: LogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Distance: ${log.distanceCm} cm", fontWeight = FontWeight.Bold)
            Text("State: ${log.state}")
            Text("DateTime: ${log.dateTime}")
        }
    }
}
