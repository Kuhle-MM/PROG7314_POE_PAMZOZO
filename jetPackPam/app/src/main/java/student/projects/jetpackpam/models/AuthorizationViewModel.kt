package student.projects.jetpackpam.models

import android.content.Context
import android.widget.Toast
import androidx.biometric.BiometricManager
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
import student.projects.jetpackpam.screens.accounthandler.authorization.BiometricPrefs
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient
import student.projects.jetpackpam.screens.accounthandler.authorization.LocalUserData
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

/**
 * AuthorizationModelViewModel
 *
 * Responsibilities:
 *  - Email/password login & sign-up
 *  - Google One Tap login
 *  - Biometric preference signalling + secure credential persistence for biometric sign-in
 *
 * Notes:
 *  - EncryptedSharedPreferences used to store credentials when user opts in.
 *  - signOutSuspend() is a suspending sign-out that signOutSafely() will await before navigating.
 */
class AuthorizationModelViewModel(
    val googleAuthClient: GoogleAuthClient
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance().reference

    // --- UI state flows ---
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _signUpSuccess = MutableStateFlow(false)
    val signUpSuccess: StateFlow<Boolean> = _signUpSuccess.asStateFlow()

    private val _userData = MutableStateFlow<UserData?>(googleAuthClient.getSignedInUser())
    val userData: StateFlow<UserData?> = _userData.asStateFlow()

    // --- Biometric state exposed to UI ---
    private val _biometricEnabled = MutableStateFlow(false)
    val biometricEnabled: StateFlow<Boolean> = _biometricEnabled.asStateFlow()

    // When true UI should show a BiometricPrompt; UI must call onBiometricAuthenticated() on success
    private val _requestBiometricPrompt = MutableStateFlow(false)
    val requestBiometricPrompt: StateFlow<Boolean> = _requestBiometricPrompt.asStateFlow()

    // Keep last-saved email in memory
    private val _lastSavedEmail = MutableStateFlow<String?>(null)
    val lastSavedEmail: StateFlow<String?> = _lastSavedEmail.asStateFlow()

    // Offline / Sync helpers (optional)
    lateinit var offlineRepo: OfflineRepository
    lateinit var syncManager: FirebaseSyncManager

    fun setupOfflineSupport(repo: OfflineRepository, sync: FirebaseSyncManager) {
        offlineRepo = repo
        syncManager = sync
    }

    // ----------------------- LOGIN -----------------------
    fun login(email: String, password: String, onSuccess: () -> Unit = {}) {
        if (email.isBlank() || password.isBlank()) {
            _errorMessage.value = "Email and password cannot be empty"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                val firebaseUser = result.user
                if (firebaseUser == null) {
                    _errorMessage.value = "Login failed: no user returned"
                    return@launch
                }

                // update in-memory user
                _userData.value = UserData(
                    userId = firebaseUser.uid,
                    username = firebaseUser.displayName,
                    email = firebaseUser.email,
                    profilePictureUrl = firebaseUser.photoUrl?.toString()
                )

                // keep last email in memory for UI convenience
                _lastSavedEmail.value = email

                // attempt to sync in background if available
                if (this@AuthorizationModelViewModel::syncManager.isInitialized) {
                    launch(Dispatchers.IO) {
                        try {
                            syncManager.sync(firebaseUser.uid)
                        } catch (_: Exception) { /* ignore */ }
                    }
                }

                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Login failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // ----------------------- SIGN UP -----------------------
    fun signUp(
        name: String,
        surname: String,
        email: String,
        password: String,
        confirmPassword: String,
        onSuccess: () -> Unit = {}
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
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user ?: run {
                    _errorMessage.value = "Sign up failed: no user returned"
                    _isLoading.value = false
                    return@launch
                }

                // update profile display name
                val profileUpdate = UserProfileChangeRequest.Builder()
                    .setDisplayName("$name $surname")
                    .build()
                firebaseUser.updateProfile(profileUpdate).await()

                // create safe default structure in Realtime DB (DO NOT store password)
                createUserStructure(firebaseUser.uid, name, surname, email)

                // save minimal local profile (offline repo), non-blocking
                if (this@AuthorizationModelViewModel::offlineRepo.isInitialized) {
                    launch(Dispatchers.IO) {
                        try {
                            offlineRepo.saveOffline(
                                uid = firebaseUser.uid,
                                key = "profile",
                                data = LocalUserData(name = name, surname = surname, email = email, phone = "")
                            )
                        } catch (_: Exception) { /* ignore */ }
                    }
                }

                // attempt immediate sync (fire-and-forget)
                if (this@AuthorizationModelViewModel::syncManager.isInitialized) {
                    launch(Dispatchers.IO) {
                        try {
                            syncManager.sync(firebaseUser.uid)
                        } catch (_: Exception) { /* ignore */ }
                    }
                }

                // update state
                _userData.value = UserData(
                    userId = firebaseUser.uid,
                    username = firebaseUser.displayName,
                    email = firebaseUser.email,
                    profilePictureUrl = firebaseUser.photoUrl?.toString()
                )

                _signUpSuccess.value = true
                // keep last saved email in memory
                _lastSavedEmail.value = email

                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Sign up failed"
            } finally {
                _isLoading.value = false
            }
        }
    }

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
            "mapping" to mapOf("mappingid" to "", "image" to ""),
            "preference" to mapOf("preferenceid" to "", "preferenceName" to ""),
            "profile" to mapOf("name" to name, "surname" to surname, "email" to email, "phonenumber" to "")
        )

        userRef.setValue(userData)
            .addOnSuccessListener { /* ignore */ }
            .addOnFailureListener { /* ignore or log */ }
    }

    // ----------------------- GOOGLE ONE TAP -----------------------
    suspend fun getGoogleSignInIntentSender() = googleAuthClient.signIn()

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
                val firebaseUser = authResult.user ?: run {
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

                // keep last email in memory for UI
                firebaseUser.email?.let { _lastSavedEmail.value = it }

                // ensure offline profile exists (non-blocking)
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

                // trigger sync in background
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

    // ----------------------- SIGN OUT -----------------------
    /**
     * Non-suspending signOut helper retained for simple use.
     * Prefer signOutSuspend() + signOutSafely() for deterministic behavior.
     */
    fun signOut(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                try {
                    googleAuthClient.signOut()
                } catch (_: Exception) { /* ignore sign-out errors from Google client */ }

                auth.signOut()
                _userData.value = null
                onComplete?.invoke()
            } catch (e: Exception) {
                _errorMessage.value = e.localizedMessage ?: "Sign out failed"
            }
        }
    }

    /**
     * Suspends until sign-out finishes (awaits Google sign-out then Firebase sign-out).
     * Use this in flows that must wait for sign-out to complete before navigating.
     */
    suspend fun signOutSuspend() {
        try {
            try {
                googleAuthClient.signOut()
            } catch (_: Exception) { /* ignore */ }

            auth.signOut()
            _userData.value = null
        } catch (e: Exception) {
            _errorMessage.value = e.localizedMessage ?: "Sign out failed"
        }
    }

    /**
     * Convenience: sign out and navigate to login while showing a Toast.
     * `clearBiometric` optional — set true to clear saved biometric prefs & credentials.
     */
    fun signOutSafely(
        context: Context,
        navController: androidx.navigation.NavHostController,
        clearBiometric: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                // Wait for sign-out to finish
                signOutSuspend()

                if (clearBiometric) {
                    BiometricPrefs.clearAll(context)
                    _lastSavedEmail.value = null
                    _biometricEnabled.value = false
                    // remove encrypted credentials as well
                    clearSavedCredentials(context)
                }

                Toast.makeText(context, "Signed out successfully", Toast.LENGTH_SHORT).show()
                navController.navigate("login") {
                    popUpTo("main") { inclusive = true }
                    launchSingleTop = true
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Sign-out failed: ${e.localizedMessage ?: e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // ----------------------- SECURE CREDENTIAL STORAGE (FOR BIOMETRIC SIGN-IN) -----------------------
    private fun getSecurePrefs(context: Context) =
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                "pam_secure_prefs",
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // fallback to normal SharedPrefs if EncryptedSharedPreferences creation fails
            context.getSharedPreferences("pam_secure_prefs_fallback", Context.MODE_PRIVATE)
        }

    /**
     * Persist credentials encrypted on-device. Call this only when user explicitly opts-in to biometric sign-in.
     * Note: Storing credentials increases risk; prefer tokens where possible.
     */
    fun persistCredentialsEncrypted(context: Context, email: String, password: String) {
        try {
            val prefs = getSecurePrefs(context)
            prefs.edit().putString("saved_email", email).putString("saved_password", password).apply()
        } catch (e: Exception) {
            // log / surface if needed
            _errorMessage.value = "Failed to persist credentials"
        }
    }

    private fun clearSavedCredentials(context: Context) {
        try {
            val prefs = getSecurePrefs(context)
            prefs.edit().remove("saved_email").remove("saved_password").apply()
        } catch (_: Exception) { /* ignore */ }
    }

    private fun getSavedCredentials(context: Context): Pair<String, String>? {
        return try {
            val prefs = getSecurePrefs(context)
            val email = prefs.getString("saved_email", null)
            val pw = prefs.getString("saved_password", null)
            if (!email.isNullOrBlank() && !pw.isNullOrBlank()) Pair(email, pw) else null
        } catch (e: Exception) {
            null
        }
    }

    // ----------------------- BIOMETRICS HELPERS -----------------------
    fun loadBiometricPreference(context: Context) {
        val enabled = BiometricPrefs.isBiometricEnabled(context)
        _biometricEnabled.value = enabled
        _lastSavedEmail.value = BiometricPrefs.getLastEmail(context)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        BiometricPrefs.setBiometricEnabled(context, enabled)
        _biometricEnabled.value = enabled
    }

    fun persistLastSavedEmail(context: Context) {
        val email = _lastSavedEmail.value ?: return
        BiometricPrefs.saveLastEmail(context, email)
    }

    fun getLastSavedEmailFromPrefs(context: Context): String? {
        return BiometricPrefs.getLastEmail(context)
    }

    fun isBiometricAvailable(context: Context): Boolean {
        val biometricManager = BiometricManager.from(context)
        return biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS
    }

    fun requestShowBiometricPrompt() {
        _requestBiometricPrompt.value = true
    }

    fun clearBiometricPromptRequest() {
        _requestBiometricPrompt.value = false
    }

    /**
     * Call when BiometricPrompt reports success.
     * - If a Firebase session exists, restore userData.
     * - Otherwise try to sign-in using stored encrypted credentials (if available).
     */
    fun onBiometricAuthenticatedWithSignIn(context: Context, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                    // session exists — restore state
                    _userData.value = UserData(
                        userId = firebaseUser.uid,
                        username = firebaseUser.displayName,
                        email = firebaseUser.email,
                        profilePictureUrl = firebaseUser.photoUrl?.toString()
                    )
                    _errorMessage.value = null
                    onSuccess()
                    clearBiometricPromptRequest()
                    return@launch
                }

                // Try saved encrypted credentials
                val creds = getSavedCredentials(context)
                if (creds == null) {
                    _errorMessage.value = "No saved credentials. Please sign in manually."
                    clearBiometricPromptRequest()
                    return@launch
                }

                val (email, password) = creds
                try {
                    val result = auth.signInWithEmailAndPassword(email, password).await()
                    val user = result.user ?: run {
                        _errorMessage.value = "Biometric sign-in failed: no user returned"
                        return@launch
                    }

                    _userData.value = UserData(
                        userId = user.uid,
                        username = user.displayName,
                        email = user.email,
                        profilePictureUrl = user.photoUrl?.toString()
                    )

                    _lastSavedEmail.value = email
                    _errorMessage.value = null
                    onSuccess()
                } catch (e: Exception) {
                    _errorMessage.value = e.localizedMessage ?: "Biometric sign-in failed"
                } finally {
                    clearBiometricPromptRequest()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Legacy method — restores user from any active Firebase session (does not attempt credential sign-in).
     * Kept for compatibility.
     */
    fun onBiometricAuthenticated(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val firebaseUser = auth.currentUser
            if (firebaseUser != null) {
                _userData.value = UserData(
                    userId = firebaseUser.uid,
                    username = firebaseUser.displayName,
                    email = firebaseUser.email,
                    profilePictureUrl = firebaseUser.photoUrl?.toString()
                )
                _errorMessage.value = null
                onSuccess()
            } else {
                _errorMessage.value = "No active session. Please sign in using email/password or Google."
            }
            clearBiometricPromptRequest()
        }
    }

    fun resetSignUpState() {
        _signUpSuccess.value = false
    }
}
