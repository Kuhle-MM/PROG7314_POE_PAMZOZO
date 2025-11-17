package student.projects.jetpackpam.data

data class MotorRequest(
    val x: Float,  // -1 to 1
    val y: Float,  // -1 to 1
    val speed: Int = 50
)