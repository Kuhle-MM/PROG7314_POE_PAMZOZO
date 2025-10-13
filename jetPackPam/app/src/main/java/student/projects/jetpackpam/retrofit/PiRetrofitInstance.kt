package student.projects.jetpackpam.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PiRetrofitInstance {
    private const val BASE_URL = "http://192.168.137.1:7298/"

    val api: RobotApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RobotApi::class.java)
    }
}