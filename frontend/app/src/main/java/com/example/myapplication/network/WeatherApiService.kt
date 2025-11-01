package com.example.myapplication.network

import com.example.myapplication.model.WeatherApiResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("forecast")
    suspend fun getWeatherForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric" // Use "metric" for Celsius
    ): Response<WeatherApiResponse>
}
