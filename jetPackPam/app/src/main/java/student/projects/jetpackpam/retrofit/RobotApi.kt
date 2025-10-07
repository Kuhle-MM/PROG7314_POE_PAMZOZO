package student.projects.jetpackpam.retrofit

import retrofit2.http.Body
import retrofit2.http.POST
import student.projects.jetpackpam.data.CameraRequest
import student.projects.jetpackpam.data.MotorRequest

interface RobotApi {
    @POST("api/Motor/moveMotors")
    suspend fun moveMotors(@Body request: MotorRequest)

    @POST("api/Camera/moveCamera")
    suspend fun moveCamera(@Body request: CameraRequest)
}