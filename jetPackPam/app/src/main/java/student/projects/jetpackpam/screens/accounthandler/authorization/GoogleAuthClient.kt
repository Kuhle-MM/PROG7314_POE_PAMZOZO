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
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await
import student.projects.jetpackpam.R
import student.projects.jetpackpam.models.SignInResult
import student.projects.jetpackpam.models.UserData

private const val TAG = "GoogleAuthClient"

class GoogleAuthClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Launch One Tap sign-in and return IntentSender.
     * Returns null if One Tap fails.
     */
    suspend fun signIn(): IntentSender? = try {
        oneTapClient.beginSignIn(buildSignInRequest()).await().pendingIntent.intentSender
    } catch (e: Exception) {
        Log.w(TAG, "One Tap failed, fallback will be used", e)
        null
    }

    /**
     * Handles One Tap result intent and extracts Google account info.
     */
    fun signInWithIntent(intent: Intent): SignInResult {
        return try {
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            val googleIdToken = credential.googleIdToken

            if (googleIdToken == null) {
                SignInResult(data = null, errorMessage = "No account selected")
            } else {
                SignInResult(
                    data = UserData(
                        userId = "", // assign after Firebase login
                        username = credential.displayName ?: "Unknown User",
                        profilePictureUrl = credential.profilePictureUri?.toString()
                    ),
                    errorMessage = null
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling One Tap intent", e)
            SignInResult(data = null, errorMessage = e.message ?: "Google sign-in failed")
        }
    }

    /**
     * Creates a Firebase session from Google ID token.
     */
    suspend fun confirmFirebaseLogin(googleIdToken: String): UserData? {
        return try {
            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val user = auth.signInWithCredential(credential).await().user
            user?.run {
                UserData(uid, displayName, photoUrl?.toString())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase login failed", e)
            if (e is CancellationException) throw e
            null
        }
    }

    /**
     * Signs out from both Firebase and One Tap.
     */
    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()
        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed", e)
            if (e is CancellationException) throw e
        }
    }

    /**
     * Returns the current Firebase user, if any.
     */
    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(uid, displayName, photoUrl?.toString())
    }

    /**
     * Build One Tap sign-in request.
     */
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

    /**
     * Returns a classic Google Sign-In Intent for fallback.
     */
    fun getSignInIntent(): Intent {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.google_web_client_id))
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(context, gso)
        return googleSignInClient.signInIntent
    }
}
