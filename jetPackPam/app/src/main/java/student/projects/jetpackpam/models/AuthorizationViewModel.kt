package student.projects.jetpackpam.models

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class AuthorizationModelViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val _isLoggedIn = MutableStateFlow<FirebaseUser?>(null)
    val currentUser: StateFlow<FirebaseUser?> = _isLoggedIn.asStateFlow()

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and password cannot be empty"
            return
        }

        isLoading = true
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    _isLoggedIn.value = auth.currentUser
                    onSuccess()
                } else {
                    errorMessage = task.exception?.message
                }
            }
    }

    fun signUp(
        name: String,
        surname: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        if (password != confirmPassword) {
            errorMessage = "Passwords do not match"
            return
        }

        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Email and Password cannot be empty"
            return
        }

        isLoading = true
        errorMessage = null

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                        .setDisplayName("$name $surname")
                        .build()
                    user?.updateProfile(profileUpdates)

                    _isLoggedIn.value = user
                    onSuccess()
                } else {
                    errorMessage = task.exception?.localizedMessage ?: "Sign-up failed"
                }
            }
    }

    fun logout() {
        auth.signOut()
        _isLoggedIn.value = null
    }
}
