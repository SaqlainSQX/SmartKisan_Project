package com.example.myapplication.network

import com.example.myapplication.model.ChatMessage
import com.example.myapplication.model.ChatRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ChatbotApiService {

    @GET("chat/")
    suspend fun getChatHistory(): Response<List<ChatMessage>>

    @POST("chat/")
    suspend fun postChatMessage(@Body request: ChatRequest): Response<ChatMessage>
}