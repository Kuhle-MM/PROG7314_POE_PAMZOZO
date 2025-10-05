package student.projects.jetpackpam.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val retrofit = Retrofit.Builder()
    .baseUrl("https://pamapi-bdguhpdzbahca0f9.eastasia-01.azurewebsites.net/") // correct!
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val languageApi = retrofit.create(LanguageApi::class.java)