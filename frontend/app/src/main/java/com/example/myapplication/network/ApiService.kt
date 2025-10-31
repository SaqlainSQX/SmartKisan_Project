package com.example.myapplication.network

import com.example.myapplication.model.RegisterResponse
import com.example.myapplication.model.TokenResponse
import com.example.myapplication.model.UserCreate
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface ApiService {

    @POST("register")
    suspend fun register(@Body userCreate: UserCreate): Response<RegisterResponse>

    @FormUrlEncoded // Required for /token endpoint
    @POST("token")
    suspend fun login(
        @Field("username") username: String,
        @Field("password") password: String,
        // --- FIX: Removed default value to make it explicit ---
        @Field("grant_type") grantType: String,
        @Field("scope") scope: String = "",
        @Field("client_id") clientId: String = "",
        @Field("client_secret") clientSecret: String = ""
    ): Response<TokenResponse>
}
