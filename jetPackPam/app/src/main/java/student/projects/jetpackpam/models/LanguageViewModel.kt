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
        private set

    var currentLanguageCode by mutableStateOf("en")
        private set

    var uiTexts by mutableStateOf(
        mapOf(
            //login
            "login_title" to "Log in",
            "login_subtitle" to "Log in to get started",
            "email_label" to "Email",
            "email_hint" to "example@example.com",
            "password_label" to "Password",
            "password_hint" to "Password",
            "login_button" to "Log in",
            "logging_in" to "Logging in...",
            "signup_prompt" to "Don't have an account?",
            "google_login" to "Log in Using Google",
            "login_success_toast" to "Welcome back!",
            "login_failed_toast" to "Login failed: ",
            "google_signin_failed" to "Google Sign-In failed",
            "google_signin_cancelled" to "Google Sign-In cancelled",
            //sign out
            "signedOut" to "Signed out successfully",
            //language screen
            "header" to "Select preferred language",
            "description" to "Choose the language you prefer for the app",
        )
    )
        private set

    // The SharedPreferences instance is safe to create here.
    private val prefs = application.getSharedPreferences("app_prefs", 0)

    fun setLanguage(language: String, code: String, texts: Map<String, String>) {
        selectedLanguage = language
        currentLanguageCode = code
        uiTexts = texts
        save(language, code)
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

    fun translateAll(
        newCode: String,
        onComplete: (Map<String, String>) -> Unit
    ) {
    fun translateAll(newCode: String, onComplete: (Map<String, String>) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val newTexts = mutableMapOf<String, String>()
                for ((key, text) in uiTexts) {
                val translated = mutableMapOf<String, String>()

                for ((key, value) in uiTexts) {
                    if (newCode == "en") {
                        translated[key] = when (key) {
                            //login
                            "login_title" -> "Log in"
                            "login_subtitle" -> "Log in to get started"
                            "email_label" -> "Email"
                            "email_hint" -> "example@example.com"
                            "password_label" -> "Password"
                            "password_hint" -> "Password"
                            "login_button" -> "Log in"
                            "logging_in" -> "Logging in..."
                            "signup_prompt" -> "Don't have an account?"
                            "google_login" -> "Log in Using Google"
                            "login_success_toast" -> "Welcome back!"
                            "login_failed_toast" -> "Login failed: "
                            "google_signin_failed" -> "Google Sign-In failed"
                            "google_signin_cancelled" -> "Google Sign-In cancelled"
                            //sign out
                            "signedOut" -> "Signed out successfully"
                            //language screen
                            "header" -> "Select preferred language"
                            "description" -> "Choose the language you prefer for the app"
                            "buttonNext" -> "Set"
                            "footer" -> "Powered by Pam"
                                //home screen
                            "signOut" -> "Sign out"
                            "welcomeMessage" -> "I’m ready to help you with anything.\nJust type below or say the word"
                            "chatButton" -> "Chat"
                                //Side nav
                            "settings" -> "Settings"
                            "profile" -> "Profile"
                            "language" -> "Language"
                            "fontSize" -> "FontSize"
                            "pamTheme" -> "Theme"
                            "personality" -> "Personality"
                            "logout" -> "Logout"
                            "home" -> "Home"
                            "video" -> "Video"
                            "games" -> "Games"
                            //profile
                            "profile" -> "Profile"
                            "signOut" -> "Sign out"
                                //fontsize screen
                            "fontHeader" -> "Select your font size for our chat"
                            "preview" -> "Preview text"
                                //pamtheme screen
                            "lightMode" -> "Light mode"
                            "darkMode" -> "Dark mode"
                            "themeHeader" -> "Choose the best colour for me"
                                //personality screen
                            "personalityHeader" -> "Choose my personality"
                            "sarcastic" -> "Sarcastic"
                            "friendly" -> "Friendly"
                            "genz" -> "Gen Z"
                            "neverInTheMood" -> "Never in the mood"
                            "motivationalCoach" -> "Motivational Coach"
                            "wiseElder" -> "Wise Elder"
                            "cheerfulOptimist" -> "Cheerful Optimist"
                            "storyTeller" -> "Story Teller"
                            "shakespearean" -> "Shakespearean"
                            "techGeek" -> "Tech Geek"
                                //chat screen
                            "chatMessage" -> "Type a message..."
                            "listening" -> "Listening..."
                            "mic_permission_required" -> "Microphone permission is required for speech input"
                            "no_speech_detected" -> "No speech detected."
                            "did_not_catch_that" -> "Didn’t catch that. Try again."
                            "speech_error" -> "Speech recognition error"
                            "message_label" -> "Message"
                            "message_hint" -> "Type a message..."
                            "open_spotify" -> "Open Spotify"
                            "open_phone" -> "Open Phone"
                            "send" -> "Send"
                            "spotify_error" -> "Could not open Spotify"
                            "mic_hold" -> "Hold to talk"
                            "songs" -> "Songs"
                            "animals" -> "Animals"
                                //video screen
                            "liveFeed" -> "No live feed detected"
                            "cameraControl" -> "Camera Control"
                                //games screen
                                //charades
                            "tapToPlay" -> "TAP TO PLAY"
                            "selectACategory" -> "Select a Category"
                            "actors" -> "Actors"
                            "movies" -> "Movies"
                            "songs" -> "Songs"
                            "animals" -> "Animals"
                            "food" -> "Food"
                            "peopleYouKnow" -> "People You Know"
                            "anime" -> "Anime"
                            "sports" -> "Sports"
                            "left" -> "left"
                            "correctWords" -> "Correct Words"
                            "skippedWords" -> "Skipped Words"
                            "goHome" -> "Go Home"
                            "gameOver" -> "Game Over !"
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
                withContext(Dispatchers.Main) {
                    onComplete(uiTexts)
                }
            }
        }
    }

    fun loadLanguage() {
        selectedLanguage = prefs.getString("languageName", "English") ?: "English"
        currentLanguageCode = prefs.getString("languageCode", "en") ?: "en"
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