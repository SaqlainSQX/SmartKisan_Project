// G:\SmartKisan_Project\frontend\app\src\main\java\com\example\myapplication\network\DiseaseApiService.kt
package com.example.myapplication.network

import com.example.myapplication.model.DiseasePredictionResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface DiseaseApiService {
    @Multipart
    @POST("disease/predict") // Note the full path from main.py and disease_router.py
    suspend fun predictDisease(
        @Part image: MultipartBody.Part
    ): Response<DiseasePredictionResponse>
}