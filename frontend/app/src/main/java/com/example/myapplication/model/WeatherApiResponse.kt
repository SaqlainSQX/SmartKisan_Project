package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

// This file contains all the data classes needed to parse the
// 5-day/3-hour forecast response from OpenWeatherMap.

data class WeatherApiResponse(
    val cod: String,
    val message: Int,
    val cnt: Int,
    val list: List<ForecastListItem>,
    val city: City
)

data class ForecastListItem(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val clouds: Clouds,
    val wind: Wind,
    @SerializedName("dt_txt")
    val dtTxt: String,
    val rain: Rain? // Rain data might be null
)

data class Main(
    val temp: Double,
    @SerializedName("feels_like")
    val feelsLike: Double,
    @SerializedName("temp_min")
    val tempMin: Double,
    @SerializedName("temp_max")
    val tempMax: Double,
    val pressure: Int,
    @SerializedName("sea_level")
    val seaLevel: Int,
    @SerializedName("grnd_level")
    val grndLevel: Int,
    val humidity: Int,
    @SerializedName("temp_kf")
    val tempKf: Double
)

data class Weather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class Clouds(
    val all: Int
)

data class Wind(
    val speed: Double,
    val deg: Int,
    val gust: Double
)

data class Rain(
    @SerializedName("3h")
    val threeHour: Double?
)

data class City(
    val id: Int,
    val name: String,
    val coord: Coord,
    val country: String,
    val population: Int,
    val timezone: Int,
    val sunrise: Long,
    val sunset: Long
)

data class Coord(
    val lat: Double,
    val lon: Double
)
