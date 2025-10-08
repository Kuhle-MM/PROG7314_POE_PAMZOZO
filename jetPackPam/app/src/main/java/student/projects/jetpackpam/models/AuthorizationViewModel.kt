package student.projects.jetpackpam.models

import android.content.IntentSender
import android.util.Log
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.widget.Toast
import androidx.navigation.NavHostController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient

/**
 * ViewModel to handle authentication state for:
 * 1. Email/Password login & sign-up
 * 2. Google One Tap login
 *
 * Automatically creates Realtime Database structure after successful sign-up.
 */
class AuthorizationModelViewModel(
    private val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    private val _signUpSuccess = MutableStateFlow(false)
    val signUpSuccess: StateFlow<Boolean> = _signUpSuccess
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference //Added Realtime DB reference

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
                            email = firebaseUser.email,
                            profilePictureUrl = firebaseUser.photoUrl?.toString()

                        )
                    }
                    onSuccess()
                } else {
                    _errorMessage.value = task.exception?.localizedMessage ?: "Login failed"
                }
            }
    }

    // --- EMAIL/PASSWORD SIGN-UP ---
    fun signUp(
        name: String,
        surname: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit
    ) {
        Log.d("SignUpViewModel", "Starting sign-up for: $email")

        if (name.isBlank() || surname.isBlank() || email.isBlank() ||
            password.isBlank() || confirmPassword.isBlank()
        ) {
            _errorMessage.value = "Please fill in all fields"
            Log.w("SignUpViewModel", "Missing fields detected.")
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
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

                auth.createUserWithEmailAndPassword(email, password)
                    .addOnSuccessListener {
                        val profileUpdate = UserProfileChangeRequest.Builder()
                            .setDisplayName("$name $surname")
                            .build()
                        val user = auth.currentUser
                        if (user != null) {
                            Log.i("SignUpViewModel", "Sign-up successful for: $email")

                            // ✅ Create default user structure in Realtime Database
                            createUserStructure(
                                uid = user.uid,
                                name = name,
                                surname = surname,
                                email = email,
                                password = password
                            )

                            _isLoading.value = false
                            _signUpSuccess.value = true
                            onSuccess()
                        }
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

    // ✅ --- NEW: Create Realtime Database structure ---
    private fun createUserStructure(uid: String, name: String, surname: String, email: String, password: String) {
        val userRef = db.child("User").child(uid)

        val userData = mapOf(
            "command" to "",
            "pi" to mapOf(
                "piid" to "",
                "picolour" to "",
                "camerapan" to 0,
                "cameratilt" to 0,
                "motorspeed" to 0,
                "motorx" to 0,
                "motory" to 0
            ),
            "mapping" to mapOf(
                "mappingid" to "",
                "image" to ""
            ),
            "preference" to mapOf(
                "preferenceid" to "",
                "preferenceName" to ""
            ),
            "profile" to mapOf(
                "name" to name,
                "surname" to surname,
                "email" to email,
                "password" to password,
                "phonenumber" to ""
            )
        )

        userRef.setValue(userData)
            .addOnSuccessListener {
                Log.d("FirebaseDB", "User data successfully created for $uid")
            }
            .addOnFailureListener { e ->
                Log.e("FirebaseDB", "Failed to create user structure", e)
            }
    }

    // --- GOOGLE ONE TAP LOGIN ---
    suspend fun getGoogleSignInIntentSender(): IntentSender? {
        return googleAuthClient.signIn()
    }

    fun handleGoogleSignInResult(result: SignInResult) {
        viewModelScope.launch {
            _isLoading.value = true

            val idToken = result.idToken
            if (idToken.isNullOrBlank()) {
                _errorMessage.value = result.errorMessage ?: "Google sign-in failed"
                _isLoading.value = false
                return@launch
            }

            // Create Firebase credential
            val googleCredential = GoogleAuthProvider.getCredential(idToken, null)

            // Sign in with Firebase
            auth.signInWithCredential(googleCredential)
                .addOnSuccessListener { authResult ->
                    val firebaseUser = authResult.user
                    val email = firebaseUser?.email
                    val username = firebaseUser?.displayName
                    val photo = firebaseUser?.photoUrl?.toString()

                    if (firebaseUser == null || email.isNullOrBlank()) {
                        _errorMessage.value = "Failed to get email from Firebase"
                        _isLoading.value = false
                        return@addOnSuccessListener
                    }

                    // Check if email already has sign-in methods
                    auth.fetchSignInMethodsForEmail(email)
                        .addOnSuccessListener { signInMethods ->
                            when {
                                // Already linked with Google
                                signInMethods.signInMethods?.contains(GoogleAuthProvider.GOOGLE_SIGN_IN_METHOD) == true -> {
                                    _userData.value = UserData(
                                        userId = firebaseUser.uid,
                                        username = username,
                                        email = email,
                                        profilePictureUrl = photo
                                    )
                                    _errorMessage.value = null
                                    Log.d("GoogleSSO", "Signed in with Google: $email")
                                }

                                // Account exists but not linked
                                !signInMethods.signInMethods.isNullOrEmpty() -> {
                                    _errorMessage.value =
                                        "This email is already registered. Please log in with your password first to link Google."
                                    Log.w("GoogleSSO", "Account exists but not linked: $email")
                                }

                                // No account found → auto-register with Google
                                else -> {
                                    _userData.value = UserData(
                                        userId = firebaseUser.uid,
                                        username = username,
                                        email = email,
                                        profilePictureUrl = photo
                                    )
                                    _signUpSuccess.value = true
                                    Log.i("GoogleSSO", "New Google user created: $email")
                                }
                            }
                            _isLoading.value = false
                        }
                        .addOnFailureListener { e ->
                            _errorMessage.value = "Email verification failed: ${e.localizedMessage}"
                            _isLoading.value = false
                        }
                }
                .addOnFailureListener { e ->
                    _errorMessage.value = "Google sign-in failed: ${e.localizedMessage}"
                    _isLoading.value = false
                }
        }
    }


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



    fun signOutSafely(
        context: Context,
        navController: NavHostController,
        authViewModel: AuthorizationModelViewModel
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                authViewModel.signOut()
                Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                navController.navigate("login") {
                    popUpTo("main") { inclusive = true }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Sign-out failed: ${e.localizedMessage ?: "Unknown error"}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}
