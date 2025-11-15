package student.projects.jetpackpam.models

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import student.projects.jetpackpam.data.LanguageRequest
import student.projects.jetpackpam.retrofit.languageApi

class LanguageViewModel(application: Application) : AndroidViewModel(application) {

    var selectedLanguage by mutableStateOf("English")
        private set

    var currentLanguageCode by mutableStateOf("en")
        private set

    var uiTexts by mutableStateOf(
        mapOf(
            "header" to "Select preferred language",
            "description" to "Choose the language you prefer for the app",
            "buttonNext" to "Set",
            "footer" to "Powered by Pam",
            "signOut" to "Sign out",
            "welcomeMessage" to "I’m ready to help you with anything.\nJust type below or say the word",
            "chatButton" to "Chat"
        )
    )
        private set

    private val prefs = application.getSharedPreferences("app_prefs", 0)

    fun setLanguage(language: String, code: String, texts: Map<String, String>) {
        selectedLanguage = language
        currentLanguageCode = code
        uiTexts = texts
        save(language, code)
    }

    fun updateTexts(newTexts: Map<String, String>) {
        uiTexts = newTexts
    }

    fun translateAll(
        newCode: String,
        onComplete: (Map<String, String>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val translated = mutableMapOf<String, String>()

                for ((key, value) in uiTexts) {
                    if (newCode == "en") {
                        translated[key] = when (key) {
                            "header" -> "Select preferred language"
                            "description" -> "Choose the language you prefer for the app"
                            "buttonNext" -> "Set"
                            "footer" -> "Powered by Pam"
                            "signOut" -> "Sign out"
                            "welcomeMessage" -> "I’m ready to help you with anything.\nJust type below or say the word"
                            "chatButton" -> "Chat"
                            else -> value
                        }
                        continue
                    }

                    val response = languageApi.translate(
                        LanguageRequest(
                            text = value,
                            from = currentLanguageCode,
                            to = newCode,
                            userId = "2"
                        )
                    )

                    translated[key] = response.translated ?: value
                }

                onComplete(translated)

            } catch (e: Exception) {
                onComplete(uiTexts)
            }
        }
    }

    private fun save(name: String, code: String) {
        prefs.edit().apply {
            putString("languageName", name)
            putString("languageCode", code)
            apply()
        }
    }

    fun loadLanguage() {
        selectedLanguage = prefs.getString("languageName", "English") ?: "English"
        currentLanguageCode = prefs.getString("languageCode", "en") ?: "en"
    }
}
