package student.projects.jetpackpam.data

import com.google.firebase.Timestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

data class LogEntry(
    val id: Int,
    val message: String,
    val timestamp: Timestamp
)
