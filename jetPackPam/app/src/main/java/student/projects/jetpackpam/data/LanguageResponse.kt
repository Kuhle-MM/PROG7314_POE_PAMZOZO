package student.projects.jetpackpam.data

data class LanguageResponse(
    val original: String?,
    val detected: String?,
    val translated: String?,
    val from: String?,
    val to: String?
)
