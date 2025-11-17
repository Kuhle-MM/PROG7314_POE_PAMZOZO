package student.projects.jetpackpam.data

import java.time.LocalDateTime

data class LogEntry(
    val distanceCm: Int,
    val state: String,
    val dateTime: LocalDateTime
)
