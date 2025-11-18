package student.projects.jetpackpam.models

import android.content.Context
import android.content.IntentSender
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import student.projects.jetpackpam.data.local.OfflineRepository
import student.projects.jetpackpam.data.sync.FirebaseSyncManager
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient
import student.projects.jetpackpam.screens.accounthandler.authorization.LocalUserData

/**
 * ViewModel to handle authentication state for:
 * 1. Email/Password login & sign-up
 * 2. Google One Tap login
 *
 * Automatically creates Realtime Database structure after successful sign-up.
 */
class AuthorizationModelViewModel(
    public val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    private val _signUpSuccess = MutableStateFlow(false)
    val signUpSuccess: StateFlow<Boolean> = _signUpSuccess
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference // Realtime DB reference

    // Loading state for UI
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Error messages for UI
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // Currently signed-in user
    private val _userData = MutableStateFlow<UserData?>(googleAuthClient.getSignedInUser())
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    // Offline / Sync helpers (must be initialized by host)
    lateinit var offlineRepo: OfflineRepository
    lateinit var syncManager: FirebaseSyncManager

    fun setupOfflineSupport(repo: OfflineRepository, sync: FirebaseSyncManager) {
        this.offlineRepo = repo
        this.syncManager = sync
    }

    // --- EMAIL/PASSWORD LOGIN ---
    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email and password cannot be empty"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        // Use coroutine + await to avoid nested callbacks and to handle exceptions properly
        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user
                if (firebaseUser != null) {
                    _userData.value = UserData(
                        userId = firebaseUser.uid,
                        username = firebaseUser.displayName,
                        email = firebaseUser.email,
                        profilePictureUrl = firebaseUser.photoUrl?.toString()
                    )
                    // attempt to sync any unsynced data for this user (fire-and-forget)
                    if (this@AuthorizationModelViewModel::syncManager.isInitialized) {
                        launch(Dispatchers.IO) {
                            try {
                                syncManager.sync(firebaseUser.uid)
                            } catch (_: Exception) { /* sync failure should not block login */ }
                        }
                    }
                    onSuccess()
                } else {
                    _errorMessage.value = "Login failed: no user returned"
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Login failed"
            } finally {
                _isLoading.value = false
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
        if (name.isBlank() || surname.isBlank() || email.isBlank() ||
            password.isBlank() || confirmPassword.isBlank()
        ) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _errorMessage.value = "Invalid email address"
            return
        }

        if (password != confirmPassword) {
            _errorMessage.value = "Passwords do not match"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                // create user (suspend until result)
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user
                if (firebaseUser == null) {
                    _errorMessage.value = "Sign up failed: no user returned"
                    _isLoading.value = false
                    return@launch
                }

                // update display name
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName("$name $surname")
                    .build()
                firebaseUser.updateProfile(profileUpdate).await()

                // create default structure in Realtime Database (DO NOT STORE PASSWORD)
                createUserStructure(
                    uid = firebaseUser.uid,
                    name = name,
                    surname = surname,
                    email = email
                )

                // Save a minimal local user profile for offline use (DO NOT STORE PASSWORD)
                if (this@AuthorizationModelViewModel::offlineRepo.isInitialized) {
                    launch(Dispatchers.IO) {
                        try {
                            offlineRepo.saveOffline(
                                uid = firebaseUser.uid,
                                key = "profile",
                                data = LocalUserData(
                                    name = name,
                                    surname = surname,
                                    email = email,
                                    phone = "" // left blank intentionally; never store password
                                )
                            )
                        } catch (e: Exception) {
                            // swallow so signup continues; optionally log
                        }
                    }
                }

                // Attempt to sync immediately if possible (fire-and-forget)
                if (this@AuthorizationModelViewModel::syncManager.isInitialized) {
                    launch(Dispatchers.IO) {
                        try {
                            syncManager.sync(firebaseUser.uid)
                        } catch (_: Exception) { /* ignore sync failures */ }
                    }
                }

                // update view state
                _userData.value = UserData(
                    userId = firebaseUser.uid,
                    username = firebaseUser.displayName,
                    email = firebaseUser.email,
                    profilePictureUrl = firebaseUser.photoUrl?.toString()
                )

                _signUpSuccess.value = true
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Sign up failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- NEW: Create Realtime Database structure (safe: no password) ---
    private fun createUserStructure(uid: String, name: String, surname: String, email: String) {
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
                "phonenumber" to ""
            )
        )

        // use tasks with listeners but don't block the UI; log failures
        userRef.setValue(userData)
            .addOnSuccessListener {
                // created
            }
            .addOnFailureListener { e ->
                // optionally log
            }
    }

    // --- GOOGLE ONE TAP & Firebase sign-in flow ---
    suspend fun getGoogleSignInIntentSender(): IntentSender? {
        return googleAuthClient.signIn()
    }

    /**
     * Accepts SignInResult from GoogleAuthClient.signInWithIntent() and
     * signs into Firebase using the idToken. The ViewModel is responsible
     * for merging state and updating local offline caches.
     */
    fun handleGoogleSignInResult(result: SignInResult) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val idToken = result.idToken
                if (idToken.isNullOrBlank()) {
                    _errorMessage.value = result.errorMessage ?: "Google sign-in failed"
                    return@launch
                }

                val googleCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(googleCredential).await()
                val firebaseUser = authResult.user

                if (firebaseUser == null) {
                    _errorMessage.value = "Google sign-in failed: no Firebase user"
                    return@launch
                }

                // update view state
                _userData.value = UserData(
                    userId = firebaseUser.uid,
                    username = firebaseUser.displayName,
                    email = firebaseUser.email,
                    profilePictureUrl = firebaseUser.photoUrl?.toString()
                )

                // ensure offline profile exists
                if (this@AuthorizationModelViewModel::offlineRepo.isInitialized) {
                    launch(Dispatchers.IO) {
                        try {
                            offlineRepo.saveOffline(
                                uid = firebaseUser.uid,
                                key = "profile",
                                data = LocalUserData(
                                    name = firebaseUser.displayName ?: "",
                                    surname = "",
                                    email = firebaseUser.email ?: "",
                                    phone = ""
                                )
                            )
                        } catch (_: Exception) { /* ignore */ }
                    }
                }

                // trigger sync
                if (this@AuthorizationModelViewModel::syncManager.isInitialized) {
                    launch(Dispatchers.IO) {
                        try {
                            syncManager.sync(firebaseUser.uid)
                        } catch (_: Exception) { /* ignore */ }
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Google sign-in failed"
            } finally {
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
        navController: androidx.navigation.NavHostController
    ) {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                signOut()
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
