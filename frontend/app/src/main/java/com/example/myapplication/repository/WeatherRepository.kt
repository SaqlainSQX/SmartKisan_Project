package com.example.myapplication.repository

import com.example.myapplication.network.WeatherApiClient

class WeatherRepository {

    private val weatherService = WeatherApiClient.instance

    suspend fun getWeather(lat: Double, lon: Double, apiKey: String) =
        weatherService.getWeatherForecast(lat, lon, apiKey)
}
