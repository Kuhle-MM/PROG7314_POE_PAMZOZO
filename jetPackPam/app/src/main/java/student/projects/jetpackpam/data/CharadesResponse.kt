package student.projects.jetpackpam.data


data class CharadesResponse(
    val sessionId: String,
    val category: String,
    val roundSeconds: Int,
    val roundEndsAt: String,
    val isActive: Boolean
)