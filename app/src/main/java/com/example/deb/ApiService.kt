package com.example.deb

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("chat/study")
    fun sendNewsToStudyAI(@Body request: StudyRequest): Call<ChatResponse>

    @Headers("Content-Type: application/json")
    @POST("chat/answer")
    fun sendAnswerToStudyAI(@Body request: FeedbackRequest): Call<ChatResponse>
}
