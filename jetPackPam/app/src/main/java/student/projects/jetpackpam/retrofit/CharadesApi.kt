package student.projects.jetpackpam.retrofit

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import student.projects.jetpackpam.data.CharadesRequest
import student.projects.jetpackpam.data.CharadesResponse

interface CharadesApi {
    @POST("api/Game/start")
    suspend fun startGame(@Body request: CharadesRequest): CharadesResponse

    @GET("api/Game/categories")
    suspend fun getCategories(): List<String>
}

