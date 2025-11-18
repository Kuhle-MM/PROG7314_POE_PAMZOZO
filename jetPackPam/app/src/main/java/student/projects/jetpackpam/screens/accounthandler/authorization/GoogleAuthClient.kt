package student.projects.jetpackpam.screens.accounthandler.authorization

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.util.Log
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import student.projects.jetpackpam.R
import student.projects.jetpackpam.models.SignInResult
import student.projects.jetpackpam.models.UserData

private const val TAG = "GoogleAuthClient"

class GoogleAuthClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    // Lazy to avoid any main-thread heavy work at construction time
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    /** Launch One Tap sign-in and return IntentSender, null if unavailable */
    suspend fun signIn(): IntentSender? = try {
        oneTapClient.beginSignIn(buildSignInRequest()).await().pendingIntent.intentSender
    } catch (e: Exception) {
        Log.w(TAG, "One Tap unavailable, fallback will be used", e)
        null
    }

    /**
     * Convert the One Tap Intent result into a SignInResult (idToken + basic profile info).
     * This does NOT sign in to Firebase — that's handled in the ViewModel so that
     * all Firebase authentication lives in one place.
     */
    fun signInWithIntent(intent: Intent): SignInResult {
        return try {
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            val googleIdToken = credential.googleIdToken
            val displayName = credential.displayName
            val photoUri = credential.profilePictureUri?.toString()
            val id = credential.id

            if (googleIdToken.isNullOrBlank()) {
                SignInResult(data = null, idToken = null, errorMessage = "No account selected")
            } else {
                // Provide best-available profile info. Firebase will supply authoritative data after sign-in.
                SignInResult(
                    data = UserData(
                        userId = id ?: "",
                        username = displayName,
                        profilePictureUrl = photoUri,
                        email = null // will be provided by Firebase after credential sign-in
                    ),
                    idToken = googleIdToken,
                    errorMessage = null
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling One Tap intent", e)
            SignInResult(data = null, idToken = null, errorMessage = e.message ?: "Google sign-in failed")
        }
    }

    /** Classic Google Sign-In fallback intent */
    fun getSignInIntent(): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.google_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(context, gso).signInIntent
    }

    /** Currently signed-in Firebase user — CORRECT FIELD ORDER */
    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(
            userId = uid,
            username = displayName,
            profilePictureUrl = photoUrl?.toString(),
            email = email
        )
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(context.getString(R.string.google_web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(false)
            .build()
    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
            Log.d(TAG, "Successfully signed out from OneTap and Firebase")
        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed", e)
        }
    }
}
