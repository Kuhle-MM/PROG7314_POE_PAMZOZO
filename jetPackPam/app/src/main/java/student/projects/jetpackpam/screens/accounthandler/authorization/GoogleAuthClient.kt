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

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /** Launch One Tap sign-in and return IntentSender, null if unavailable */
    suspend fun signIn(): IntentSender? = try {
        oneTapClient.beginSignIn(buildSignInRequest()).await().pendingIntent.intentSender
    } catch (e: Exception) {
        Log.w(TAG, "One Tap unavailable, fallback will be used", e)
        null
    }

    /** Handle One Tap or classic intent */
    fun signInWithIntent(intent: Intent): SignInResult {
        return try {
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            val googleIdToken = credential.googleIdToken

            if (googleIdToken.isNullOrBlank()) {
                SignInResult(data = null, idToken = null, errorMessage = "No account selected")
            } else {
                SignInResult(
                    data = UserData(
                        userId = "", // Will be filled after Firebase sign-in
                        username = credential.displayName ?: "Unknown User",
                        profilePictureUrl = credential.profilePictureUri?.toString(),
                        email = null // Firebase will provide email after sign-in
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

    /** Currently signed-in Firebase user */
    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(uid, displayName, email, photoUrl?.toString())
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
        } catch (e: Exception) {
            Log.e(TAG, "Sign out failed", e)
        }
    }
}
