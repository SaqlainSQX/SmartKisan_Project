
package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

// Response from the /token endpoint
data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String
)

// Response from the /register endpoint
data class RegisterResponse(
    val message: String
)

// Data class to send as the body for /register
data class UserCreate(
    val username: String,
    val password: String
)

// Add this data class to your existing ApiModels.kt file

data class DiseasePredictionResponse(
    @SerializedName("disease_name")
    val diseaseName: String,
    val confidence: Double,
    val description: String,
    val solution: String
)


// Add these to your ApiModels.kt file

data class ChatMessage(
    val id: Int,
    val user_id: Int,
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: String
)

data class ChatRequest(
    val message: String
)