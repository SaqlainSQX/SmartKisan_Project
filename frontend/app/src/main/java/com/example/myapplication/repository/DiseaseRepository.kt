// G:\SmartKisan_Project\frontend\app\src\main\java\com\example\myapplication\repository\DiseaseRepository.kt
package com.example.myapplication.repository

import android.content.Context
import android.net.Uri
import com.example.myapplication.network.DiseaseApiClient
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.InputStream

class DiseaseRepository {

    private val diseaseService = DiseaseApiClient.instance

    suspend fun predictDisease(context: Context, imageUri: Uri): Response<com.example.myapplication.model.DiseasePredictionResponse> {

        // Get an InputStream from the Uri
        val inputStream = context.contentResolver.openInputStream(imageUri)!!

        // Read the bytes from the InputStream
        val imageBytes = inputStream.readBytes()
        inputStream.close()

        // Get the Mime Type (e.g., "image/jpeg")
        val mimeType = context.contentResolver.getType(imageUri)

        // Create the RequestBody
        val requestFile = imageBytes.toRequestBody(
            mimeType?.toMediaTypeOrNull()
        )

        // Create the MultipartBody.Part
        // The name "file" MUST match the argument name in your FastAPI endpoint
        val body = MultipartBody.Part.createFormData(
            "file",
            "leaf_image.jpg", // You can give it a generic name
            requestFile
        )

        return diseaseService.predictDisease(body)
    }
}