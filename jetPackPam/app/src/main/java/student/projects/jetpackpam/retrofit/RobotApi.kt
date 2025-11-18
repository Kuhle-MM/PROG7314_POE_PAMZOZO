package student.projects.jetpackpam.retrofit


import retrofit2.http.Body
import retrofit2.http.POST
import student.projects.jetpackpam.data.CameraRequest
import student.projects.jetpackpam.data.MotorRequest


interface RobotApi {


    // Matches Python: @post('/api/joystick')
    // Payload: { "x": float, "y": float, "speed": int }
    @POST("/api/joystick")
    suspend fun moveMotors(@Body request: MotorRequest)


    // Matches Python: @post('/api/command')
    // Payload: { "cmd": "stop" }
    @POST("/api/command")
    suspend fun stopMotors(@Body request: Map<String, String>)


    // Matches Python: @post('/camera/move')
    // Payload: { "pan": float, "tilt": float }
    @POST("/camera/move")
    suspend fun moveCamera(@Body request: CameraRequest)


    // Matches Python: @post('/camera/joystick')
    // Payload: { "dx": float, "dy": float, "speed": float }
    @POST("/camera/joystick")
    suspend fun moveCameraJoystick(@Body request: Map<String, Any>)


    // Matches Python: @post('/camera/reset')
    // Payload: {} (Empty)
    @POST("/camera/reset")
    suspend fun resetCamera()
}
