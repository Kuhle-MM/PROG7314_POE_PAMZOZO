package student.projects.jetpackpam.retrofit

import retrofit2.http.Body
import retrofit2.http.POST
import student.projects.jetpackpam.data.CameraRequest
import student.projects.jetpackpam.data.MotorRequest

interface RobotApi {

    // Motor control
    @POST("api/Motor/move")
    suspend fun moveMotors(@Body request: MotorRequest)

    @POST("api/Motor/stop")
    suspend fun stopMotors()

    // Camera absolute movement
    @POST("api/Camera/move")
    suspend fun moveCamera(@Body request: CameraRequest)

    // Camera joystick movement
    @POST("api/Camera/joystick")
    suspend fun moveCameraJoystick(@Body request: CameraRequest)

    // Camera reset
    @POST("api/Camera/reset")
    suspend fun resetCamera()
}
