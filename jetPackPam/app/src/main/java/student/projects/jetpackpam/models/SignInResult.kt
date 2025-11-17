package student.projects.jetpackpam.models

data class SignInResult(
    val data: UserData?,
    val idToken: String? = null,
    val errorMessage: String? = null
)

data class UserData(
    val userId: String,
    val username: String?,
    val email: String?,
    val profilePictureUrl: String?
)
