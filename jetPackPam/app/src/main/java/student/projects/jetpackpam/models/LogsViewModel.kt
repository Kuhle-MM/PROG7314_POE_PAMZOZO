package student.projects.jetpackpam.models

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LogsViewModel : ViewModel() {

    // Default interval → 10 minutes
    var selectedInterval by mutableStateOf(10)
        private set

    fun updateInterval(newInterval: Int) {
        selectedInterval = newInterval
    }

    fun generateTimeBlocks(): List<Pair<String, Int>> {
        val blocks = mutableListOf<Pair<String, Int>>()
        val totalMinutes = 60
        val blockSize = selectedInterval

        var start = 0
        while (start < totalMinutes) {
            val end = (start + blockSize - 1).coerceAtMost(59)
            blocks.add("${start}–${end} min" to start)
            start += blockSize
        }
        return blocks
    }
}
