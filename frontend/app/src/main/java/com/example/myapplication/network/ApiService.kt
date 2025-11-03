// G:\SmartKisan_Project\frontend\app\src\main\java\com\example\myapplication\network\ApiService.kt
package com.example.myapplication.network

import com.example.myapplication.model.ForgotPasswordRequest
import com.example.myapplication.model.MessageResponse
import com.example.myapplication.model.ResetPasswordRequest
import com.example.myapplication.model.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    @FormUrlEncoded // This is correct (matches main.py)
    @POST("register/")
    suspend fun register(
        @Field("email") email: String,
        @Field("password") password: String
    ): Response<MessageResponse>

    @FormUrlEncoded // This is correct (required by OAuth2)
    @POST("token")
    suspend fun login(
        @Field("username") username: String, // We send email in this field
        @Field("password") password: String,
        @Field("grant_type") grantType: String = "password",
        @Field("scope") scope: String = "",
        @Field("client_id") clientId: String = "",
        @Field("client_secret") clientSecret: String = ""
    ): Response<TokenResponse>

    // --- THIS IS THE FIX ---
    // Removed @FormUrlEncoded, changed param to @Body
    @POST("forgot-password/")
    suspend fun forgotPassword(
        @Body request: ForgotPasswordRequest
    ): Response<MessageResponse>
    // --- END FIX ---

    @POST("reset-password/") // This is correct (matches main.py)
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<MessageResponse>
}