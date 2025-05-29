package com.gamepackage.codereview.logic

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class GeminiRequest(
    val contents: List<Content>
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GeminiResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content
)


interface GeminiService {
    @POST("v1beta/models/gemini-pro:generateContent")
    suspend fun getExplanation(
        @Header("Authorization") authHeader: String,
        @Body request: GeminiRequest
    ): GeminiResponse

    companion object {
        fun create(): GeminiService {
            val retrofit = Retrofit.Builder()
                .baseUrl("https://generativelanguage.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            return retrofit.create(GeminiService::class.java)
        }
    }
}