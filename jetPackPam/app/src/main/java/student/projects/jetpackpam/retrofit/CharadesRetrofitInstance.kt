package student.projects.jetpackpam.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CharadesRetrofitInstance {
    val api: CharadesApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://prog7314poe20251005203516-fnbwc0d3bthte0h3.southafricanorth-01.azurewebsites.net/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CharadesApi::class.java)
    }
}