package student.projects.jetpackpam.retrofit

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import student.projects.jetpackpam.data.CharadesRequest
import student.projects.jetpackpam.data.CharadesResponse
import student.projects.jetpackpam.data.GuessItem
import student.projects.jetpackpam.data.GuessRequest
import student.projects.jetpackpam.data.SkipResponse

interface CharadesApi {

    // Start a new game session
    @POST("api/Game/start")
    suspend fun startGame(@Body request: CharadesRequest): CharadesResponse

    // Get all available categories
    @GET("api/Game/categories")
    suspend fun getCategories(): List<String>

    //  Get all guesses for a category
    @GET("api/Game/categories/{category}/items")
    suspend fun getAllGuesses(@Path("category") category: String): List<GuessItem>

    // Submit whether the guess was correct
    @POST("api/Game/{sessionId}/guess")
    suspend fun submitGuess(
        @Path("sessionId") sessionId: String,
        @Body request: GuessRequest
    ): SkipResponse

    // Skip to the next word
    @POST("api/Game/{sessionId}/skip")
    suspend fun skip(@Path("sessionId") sessionId: String): SkipResponse
}
