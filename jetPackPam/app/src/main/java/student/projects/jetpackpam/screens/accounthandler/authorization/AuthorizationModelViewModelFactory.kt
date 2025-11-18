package student.projects.jetpackpam.screens.accounthandler.authorization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import student.projects.jetpackpam.models.AuthorizationModelViewModel

class AuthorizationModelViewModelFactory(
    private val googleAuthClient: GoogleAuthClient
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthorizationModelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthorizationModelViewModel(googleAuthClient) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
