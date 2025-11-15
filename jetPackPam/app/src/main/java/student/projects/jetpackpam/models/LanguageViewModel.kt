package student.projects.jetpackpam.models

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext // <-- Added for ANR fix
import student.projects.jetpackpam.data.LanguageRequest
import student.projects.jetpackpam.retrofit.languageApi

class LanguageViewModel(application: Application) : AndroidViewModel(application) {

    var selectedLanguage by mutableStateOf("English")
    var currentLanguageCode by mutableStateOf("en")
        private set

    var uiTexts by mutableStateOf(
        mutableMapOf(
            "header" to "Select preferred language",
            "description" to "Choose the language you prefer for the app",
            "buttonNext" to "Next",
            "footer" to "Powered by Pam"
        )
    )
        private set

    // The SharedPreferences instance is safe to create here.
    private val prefs = application.getSharedPreferences("app_prefs", 0)

    init {
        // 🔴 ANR FIX: The synchronous I/O call from the init block has been removed.
        // The language will now be loaded safely when loadLanguage() is called from MainActivity.
    }

    /**
     * Updates the language settings and saves them to SharedPreferences.
     *
     * ANR FIX: Saving to SharedPreferences is blocking I/O, so it must be offloaded to Dispatchers.IO.
     */
    fun setLanguage(languageCode: String, languageName: String) {
        currentLanguageCode = languageCode
        selectedLanguage = languageName

        viewModelScope.launch(Dispatchers.IO) {
            // Saving to SharedPreferences is blocking I/O, so it must be on Dispatchers.IO
            prefs.edit().putString("languageCode", languageCode)
                .putString("languageName", languageName)
                .apply()
        }
    }

    fun updateTexts(newTexts: Map<String, String>) {
        uiTexts = newTexts.toMutableMap()
    }

    fun translateAll(newCode: String, onComplete: (Map<String, String>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newTexts = mutableMapOf<String, String>()
                for ((key, text) in uiTexts) {
                    if (newCode == "en") {
                        newTexts[key] = when (key) {
                            "header" -> "Select preferred language"
                            "description" -> "Choose the language you prefer for the app"
                            "buttonNext" -> "Next"
                            "footer" -> "Powered by Pam"
                            else -> text
                        }
                        continue
                    }

                    val response = languageApi.translate(
                        LanguageRequest(
                            text = text,
                            from = currentLanguageCode,
                            to = newCode,
                            userId = "2"
                        )
                    )
                    newTexts[key] = response.translated?.ifBlank { text } ?: text
                }
                // Update the UI state and call the completion callback on the Main thread
                withContext(Dispatchers.Main) {
                    updateUiTexts(newTexts) // Update the ViewModel's state
                    onComplete(newTexts)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onComplete(uiTexts)
                }
            }
        }
    }

    /**
     * Loads the language setting from persistent storage (SharedPreferences).
     *
     * ANR FIX: This entire function now runs on Dispatchers.IO to prevent blocking
     * the main thread during app startup.
     */
    fun loadLanguage() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // All SharedPreferences I/O is now safely backgrounded
                val savedName = prefs.getString("languageName", "English")
                val savedCode = prefs.getString("languageCode", "en")

                // Update state on the main thread (done automatically by the outer launch)
                if (savedName != null && savedCode != null) {
                    selectedLanguage = savedName
                    currentLanguageCode = savedCode
                }
            }
        }
    }

    fun fetchLanguageFromFirebase() {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("language")

        ref.get().addOnSuccessListener { snapshot ->
            val name = snapshot.child("name").getValue(String::class.java) ?: "English"
            val code = snapshot.child("code").getValue(String::class.java) ?: "en"

            selectedLanguage = name
            currentLanguageCode = code

            // Update UI texts (translateAll handles Dispatcher switching internally)
            translateAll(code) { /* updated via updateUiTexts call inside translateAll */ }
        }
    }

    fun saveLanguageToFirebase(name: String, code: String) {
        val user = FirebaseAuth.getInstance().currentUser ?: return
        val uid = user.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("language")
        ref.setValue(mapOf("name" to name, "code" to code))
    }

    // 🔹 New: global font size in sp
    var fontSize by mutableFloatStateOf(20f)
        private set

    // Function to update font size
    fun updateFontSize(newSize: Float) {
        fontSize = newSize.coerceIn(16f, 48f) // keep in valid range
    }

    // Function to update translations (if needed)
    fun updateUiTexts(newTexts: Map<String, String>) {
        uiTexts = newTexts.toMutableMap()
    }
}