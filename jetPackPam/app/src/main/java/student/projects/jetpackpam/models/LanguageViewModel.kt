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
            "heading" to "Language",
            "description" to "Choose the language you prefer for the app",
            "buttonNext" to "Set",
            "footer" to "Powered by Pam",
            //home screen
            "signOut" to "Sign out",
            "welcomeMessage" to "I’m ready to help you with anything.\nJust type below or say the word",
            "chatButton" to "Chat",
            //Side nav
            "settings" to "Settings",
            "profile" to "Profile",
            "language" to "Language",
            "fontSize" to "Font Size",
            "pamTheme" to "Theme",
            "personality" to "Personality",
            "logout" to "Logout",
            //bottom nav
            "home" to "Home",
            "video" to "Video",
            "games" to "Games",
            //profile
            "profile" to "Profile",
            "signOut" to "Sign out",
            //fontsize screen
            "fontHeader" to "Select your font size for our chat",
            "preview" to "Preview text",
            //pamtheme screen
            "lightMode" to "Light mode",
            "darkMode" to "Dark mode",
            "themeHeader" to "Choose the best colour for me",
            //personality screen
            "personalityHeader" to "Choose my personality",
            "sarcastic" to "Sarcastic",
            "friendly" to "Friendly",
            "genz" to "Gen Z",
            "neverInTheMood" to "Never in the mood",
            "motivationalCoach" to "Motivational Coach",
            "wiseElder" to "Wise Elder",
            "cheerfulOptimist" to "Cheerful Optimist",
            "storyTeller" to "Story Teller",
            "shakespearean" to "Shakespearean",
            "techGeek" to "Tech Geek",
            //chat screen
            "chatMessage" to "Type a message...",
            "listening" to "Listening...",
            "mic_permission_required" to "Microphone permission is required for speech input",
            "no_speech_detected" to "No speech detected.",
            "did_not_catch_that" to "Didn’t catch that. Try again.",
            "speech_error" to "Speech recognition error",
            "message_label" to "Message",
            "message_hint" to "Type a message...",
            "open_spotify" to "Open Spotify",
            "open_phone" to "Open Phone",
            "send" to "Send",
            "spotify_error" to "Could not open Spotify",
            "mic_hold" to "Hold to talk",
            "songs" to "Songs",
            "animals" to "Animals",
            //video screen
            "liveFeed" to "No live feed detected",
            "cameraControl" to "Camera Control",
            //games screen
            //charades
            "tapToPlay" to "TAP TO PLAY",
            "selectACategory" to "Select a Category",
            "actors" to "Actors",
            "movies" to "Movies",
            "songs" to "Songs",
            "animals" to "Animals",
            "food" to "Food",
            "peopleYouKnow" to "People You Know",
            "anime" to "Anime",
            "sports" to "Sports",
            "left" to "left",
            "correctWords" to "Correct Words",
            "skippedWords" to "Skipped Words",
            "goHome" to "Go Home",
            "gameOver" to "Game Over !"
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
        }
    }

    fun loadLanguage() {
        selectedLanguage = prefs.getString("languageName", "English") ?: "English"
        currentLanguageCode = prefs.getString("languageCode", "en") ?: "en"
    }
}