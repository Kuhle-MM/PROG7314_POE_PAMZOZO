package student.projects.jetpackpam.screens.accounthandler.authorization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import student.projects.jetpackpam.models.AuthorizationModelViewModel
import student.projects.jetpackpam.screens.accounthandler.authorization.GoogleAuthClient

class AuthorizationModelViewModelFactory(
    private val googleAuthUiClient: GoogleAuthClient
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthorizationModelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthorizationModelViewModel(googleAuthUiClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}