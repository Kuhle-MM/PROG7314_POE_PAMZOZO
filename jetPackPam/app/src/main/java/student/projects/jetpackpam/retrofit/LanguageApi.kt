package student.projects.jetpackpam.retrofit

import student.projects.jetpackpam.data.LanguageRequest
import student.projects.jetpackpam.data.LanguageResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface LanguageApi {
    @POST("api/Translation/convert")
    suspend fun translate(@Body request: LanguageRequest): LanguageResponse
}