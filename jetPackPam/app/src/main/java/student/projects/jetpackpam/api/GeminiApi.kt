package student.projects.jetpackpam.api

import retrofit2.http.Body
import retrofit2.http.POST

data class GeminiQuery(val question: String)
data class GeminiResponse(val answer: String)

interface GeminiApi {
    @POST("api/gemini/ask")
    suspend fun askGemini(@Body query: GeminiQuery): GeminiResponse
}
