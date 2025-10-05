package student.projects.jetpackpam.models

import android.content.IntentSender
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient

/**
 * ViewModel to handle authentication state for:
 * 1. Email/Password login & sign-up
 * 2. Google One Tap login
 *
 * This separates Google login from email login to prevent auto-login issues.
 */
class AuthorizationModelViewModel(
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    private val _signUpSuccess = MutableStateFlow(false)
    val signUpSuccess: StateFlow<Boolean> = _signUpSuccess
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    // Loading state for UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error messages for UI
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Currently signed-in user
    private val _userData = MutableStateFlow<UserData?>(googleAuthClient.getSignedInUser())
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    // --- EMAIL/PASSWORD LOGIN ---

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email and password cannot be empty"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _isLoading.value = false
                if (task.isSuccessful) {
                    _userData.value = auth.currentUser?.let { firebaseUser ->
                        UserData(
                            userId = firebaseUser.uid,
                            username = firebaseUser.displayName,
                            profilePictureUrl = firebaseUser.photoUrl?.toString()
                        )
                    }
                    onSuccess()
                } else {
                    _errorMessage.value = task.exception?.localizedMessage ?: "Login failed"
                }
            }
    }

    fun signUp(
        name: String,
        surname: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    )
    {
        Log.d("SignUpViewModel", "Starting sign-up for: $email")

        if (name.isBlank() || surname.isBlank() || email.isBlank() ||
            password.isBlank() || confirmPassword.isBlank()
        ) {
            _errorMessage.value = "Please fill in all fields"
            Log.w("SignUpViewModel", "Missing fields detected.")
            return
        }

        if (! Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Invalid email address"
            Log.w("SignUpViewModel", "Invalid email format: $email")
            return
        }

        if (password != confirmPassword) {
            _errorMessage.value = "Passwords do not match"
            Log.w("SignUpViewModel", "Password mismatch for: $email")
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("SignUpViewModel", "Attempting Firebase sign-up...")

                // Example: Firebase sign-up call
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        Log.i("SignUpViewModel", "Sign-up successful for: $email")
                        _isLoading.value = false
                        _signUpSuccess.value = true
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Log.e("SignUpViewModel", "Sign-up failed: ${e.localizedMessage}")
                        _isLoading.value = false
                        _errorMessage.value = e.localizedMessage ?: "Unknown error"
                    }

            } catch (e: Exception) {
                Log.e("SignUpViewModel", "Unexpected error: ${e.localizedMessage}")
                _isLoading.value = false
                _errorMessage.value = "Unexpected error: ${e.localizedMessage}"
            }
        }
    }

    // --- GOOGLE ONE TAP LOGIN ---

    /**
     * Launch One Tap login and return the IntentSender to the UI.
     */
    suspend fun getGoogleSignInIntentSender(): IntentSender? {
        return googleAuthClient.signIn()
    }

    /**
     * Handle result returned from Google One Tap sign-in.
     */
    fun handleGoogleSignInResult(result: SignInResult) {
        viewModelScope.launch {
            _isLoading.value = false
            if (result.data != null) {
                // Save Google account info (user may confirm Firebase login separately)
                _userData.value = result.data
                _errorMessage.value = null
            } else {
                _errorMessage.value = result.errorMessage ?: "Google sign-in canceled"
            }
        }
    }

    /**
     * Handle any errors from Google One Tap login.
     */
    fun handleGoogleSignInError(message: String?) {
        _isLoading.value = false
        _errorMessage.value = message ?: "Google sign-in error"
    }

    // --- SIGN OUT ---
    fun signOut() {
        viewModelScope.launch {
            try {
                auth.signOut()
                _userData.value = null
                googleAuthClient.signOut()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Sign out failed"
            }
        }
    }

    fun resetSignUpState() {
        _signUpSuccess.value = false
    }
}
