package student.projects.jetpackpam.models

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class AuthorizationModelViewModel : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(false)
    // StateFlow for observing sign-up state
    private val _isSignedUp = MutableStateFlow(false)
    val isSignedUp: StateFlow<Boolean> = _isSignedUp.asStateFlow()

    // Compose-friendly state for login
    var isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    var isLoading by mutableStateOf(false)
        private set

    // Dummy login function
    fun login(email: String, password: String) {
        isLoading = true
        _isLoggedIn.value = email == "test@example.com" && password == "1234"
        isLoading = false
    }

    fun logout() {
        _isLoggedIn.value = false
    }

    // Dummy sign-up function
    fun signUp(
        name: String,
        surname: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String
    ) {
        isLoading = true

        // Basic check
        _isSignedUp.value = password == confirmPassword && email.isNotBlank() && password.isNotBlank()

        isLoading = false
    }
}
