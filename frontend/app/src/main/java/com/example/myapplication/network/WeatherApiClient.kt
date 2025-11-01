package com.example.myapplication.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherApiClient {

    // Base URL for OpenWeatherMap API
    private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"

    val instance: WeatherApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(WeatherApiService::class.java)
    }
}
