package student.projects.jetpackpam.services

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import student.projects.jetpackpam.api.GeminiApi

object ApiClient {
    val geminiApi: GeminiApi by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:5000/") // emulator -> backend on localhost
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GeminiApi::class.java)
    }
}
