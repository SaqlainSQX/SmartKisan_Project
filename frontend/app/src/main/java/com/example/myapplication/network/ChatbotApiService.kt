// G:\SmartKisan_Project\frontend\app\src\main\java\com\example\myapplication\network\ChatbotApiService.kt
package com.example.myapplication.network

import com.example.myapplication.model.ChatMessage
import com.example.myapplication.model.ChatRequest
// --- THIS IS THE FIX (PART 2) ---
import com.example.myapplication.model.ProfileUpdate
import com.example.myapplication.model.UserResponse
import okhttp3.MultipartBody
// --- END FIX ---
import retrofit2.Response
import retrofit2.http.*

interface ChatbotApiService {

    // --- Chatbot Endpoints (Existing) ---
    @GET("chat/")
    suspend fun getChatHistory(): Response<List<ChatMessage>>

    @POST("chat/")
    suspend fun postChatMessage(@Body request: ChatRequest): Response<ChatMessage>

    // --- FIX: ADDED NEW Profile Endpoints ---
    @GET("profile/")
    suspend fun getProfile(): Response<UserResponse>

    @PUT("profile/")
    suspend fun updateProfile(@Body request: ProfileUpdate): Response<UserResponse>

    @Multipart
    @POST("profile/photo")
    suspend fun updateProfilePhoto(
        @Part image: MultipartBody.Part
    ): Response<UserResponse>
    // --- END FIX ---
}