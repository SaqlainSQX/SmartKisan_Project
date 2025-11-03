// G:\SmartKisan_Project\frontend\app\src\main\java\com\example\myapplication\model\ApiModels.kt
package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

// Response from the /token endpoint
data class TokenResponse(
    @SerializedName("access_token")
    val accessToken: String,
    @SerializedName("token_type")
    val tokenType: String
)

// Response from /disease/predict endpoint
data class DiseasePredictionResponse(
    @SerializedName("disease_name")
    val diseaseName: String,
    val confidence: Double,
    val description: String,
    val solution: String
)

// Data class for Chat History
data class ChatMessage(
    val id: Int,
    val user_id: Int,
    val role: String, // "user" or "model"
    val content: String,
    val timestamp: String
)

// Data class for sending a chat message
data class ChatRequest(
    val message: String
)

// --- DATA CLASSES FOR FORGOT PASSWORD ---

// --- THIS IS THE NEW CLASS ---
// Data class for sending the forgot password request
data class ForgotPasswordRequest(
    val email: String
)

// Data class for sending the reset password request
data class ResetPasswordRequest(
    val email: String,
    val otp: String,
    val new_password: String
)

// Data class for a generic success/error message
data class MessageResponse(
    val message: String
)