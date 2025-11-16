package student.projects.jetpackpam.util

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.onesignal.OneSignal
import com.onesignal.debug.LogLevel

// Rename the class to just Application for clarity in the factory setup
class JetPackPamApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Enable verbose logging for debugging
        OneSignal.Debug.logLevel = LogLevel.VERBOSE

        // Initialize OneSignal with your App ID
        OneSignal.initWithContext(this, "ec28e607-1daa-4d07-b387-aab2a7be53ee")

        // Request notification permission (for Android 13+)
        CoroutineScope(Dispatchers.Main).launch {
            OneSignal.Notifications.requestPermission(true)
        }
    }
}