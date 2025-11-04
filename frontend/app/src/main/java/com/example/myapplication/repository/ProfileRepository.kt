// G:\SmartKisan_Project\frontend\app\src\main\java\com\example\myapplication\repository\ProfileRepository.kt
package com.example.myapplication.repository

import android.content.Context
import android.net.Uri
import com.example.myapplication.model.ProfileUpdate
import com.example.myapplication.model.UserResponse
import com.example.myapplication.network.ChatbotApiClient // <-- Uses your existing client
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class ProfileRepository(context: Context) {

    // Uses the same secure client as your chatbot
    private val secureService = ChatbotApiClient.getInstance(context)

    // This will now be resolved
    suspend fun getProfile() = secureService.getProfile()

    // This will now be resolved
    suspend fun updateProfile(name: String, contact: String) =
        secureService.updateProfile(ProfileUpdate(name = name, contact_number = contact))

    // This will now be resolved
    suspend fun updateProfilePhoto(context: Context, imageUri: Uri): Response<UserResponse> {

        val inputStream = context.contentResolver.openInputStream(imageUri)!!
        val imageBytes = inputStream.readBytes()
        inputStream.close()

        val mimeType = context.contentResolver.getType(imageUri)
        val requestFile = imageBytes.toRequestBody(mimeType?.toMediaTypeOrNull())

        val body = MultipartBody.Part.createFormData(
            "file",
            "profile_photo.jpg",
            requestFile
        )

        return secureService.updateProfilePhoto(body)
    }
}