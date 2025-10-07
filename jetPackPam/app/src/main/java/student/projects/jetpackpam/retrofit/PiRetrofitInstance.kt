package student.projects.jetpackpam.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PiRetrofitInstance {
    private const val BASE_URL =
        "https://localhost:7298/"

    val api: RobotApi = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RobotApi::class.java)
}