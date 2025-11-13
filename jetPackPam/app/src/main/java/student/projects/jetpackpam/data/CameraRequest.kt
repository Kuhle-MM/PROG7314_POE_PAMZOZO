package student.projects.jetpackpam.data

data class CameraRequest(
    val pan: Float = 90f,   // Absolute position 0-180
    val tilt: Float = 45f,  // Absolute position 0-90
    val dx: Float = 0f,     // Joystick delta
    val dy: Float = 0f,
    val speed: Float = 1f
)