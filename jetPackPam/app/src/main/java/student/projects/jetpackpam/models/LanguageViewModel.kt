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

    private val prefs = application.getSharedPreferences("app_prefs", 0)

    init {
        // Load saved language from SharedPreferences
        val savedCode = prefs.getString("languageCode", "en") ?: "en"
        currentLanguageCode = savedCode
    }

    fun setLanguage(languageCode: String) {
        currentLanguageCode = languageCode
        prefs.edit().putString("languageCode", languageCode).apply()
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
                onComplete(newTexts)
            } catch (e: Exception) {
                onComplete(uiTexts)
            }
        }
    }

    fun loadLanguage() {
        val savedName = prefs.getString("languageName", "English")
        val savedCode = prefs.getString("languageCode", "en")

        if (savedName != null && savedCode != null) {
            selectedLanguage = savedName
            currentLanguageCode = savedCode
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

            // Update UI texts
            translateAll(code) { newTexts ->
                updateTexts(newTexts)
            }
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
        uiTexts = newTexts as MutableMap<String, String>
    }


}
