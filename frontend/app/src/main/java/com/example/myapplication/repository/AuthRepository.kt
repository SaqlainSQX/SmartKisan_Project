// G:\SmartKisan_Project\frontend\app\src\main\java\com\example\myapplication\repository\AuthRepository.kt
package com.example.myapplication.repository

import com.example.myapplication.model.ForgotPasswordRequest
import com.example.myapplication.model.ResetPasswordRequest
import com.example.myapplication.network.ApiService

// The repository abstracts the data source (network)
class AuthRepository(private val apiService: ApiService) {

    suspend fun login(email: String, password: String) =
        // Send email in the "username" field as required by OAuth2PasswordRequestForm
        apiService.login(username = email, password = password, grantType = "password")

    suspend fun register(email: String, password: String) =
        apiService.register(email = email, password = password)

    // --- THIS IS THE FIX ---
    // Now creates the data class and sends it
    suspend fun forgotPassword(email: String) =
        apiService.forgotPassword(ForgotPasswordRequest(email = email))
    // --- END FIX ---

    suspend fun resetPassword(email: String, otp: String, newPassword: String) =
        apiService.resetPassword(
            ResetPasswordRequest(
                email = email,
                otp = otp,
                new_password = newPassword
            )
        )
}