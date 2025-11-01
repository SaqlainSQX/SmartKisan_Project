package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.R
import com.example.myapplication.model.WeatherApiResponse
import com.example.myapplication.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

// --- UI Data Classes ---
// These are the simple data classes our UI will use,
// mapped from the complex API response.

data class WeatherData(
    val location: String,
    val temperature: String,
    val condition: String,
    val icon: Int // A drawable resource ID
)

data class ForecastItem(
    val day: String,
    val icon: Int, // A drawable resource ID
    val temp: String
)

data class RoughWeatherWarning(
    val hasWarning: Boolean,
    val title: String,
    val details: String
)

// --- UI State ---
// Represents the different states our UI can be in
sealed class WeatherUiState {
    data object Loading : WeatherUiState()
    data class Success(
        val weatherData: WeatherData,
        val forecastList: List<ForecastItem>,
        val warning: RoughWeatherWarning
    ) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}


class WeatherViewModel : ViewModel() {

    // --- PASTE YOUR API KEY HERE ---
    // (Get one for free from OpenWeatherMap.org)
    private val API_KEY = "15ae1f356f0dc3904dd1baf9f3d02a1a" // You can keep your key here for now

    // Location for Saha Urf Pipalgaon, Uttar Pradesh
    private val LATITUDE = 26.9632
    private val LONGITUDE = 81.3302

    private val repository = WeatherRepository()

    // --- FIX: Renamed from _UiState to _uiState ---
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadWeather()
    }

    private fun loadWeather() {
        // Handle API key placeholder
        if (API_KEY == "YOUR_API_KEY_HERE") {
            _uiState.value = WeatherUiState.Error("Please add your OpenWeatherMap API key in WeatherViewModel.kt")
            return
        }

        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            try {
                val response = repository.getWeather(LATITUDE, LONGITUDE, API_KEY)
                if (response.isSuccessful && response.body() != null) {
                    val apiData = response.body()!!
                    // Map API data to our simple UI data classes
                    val weatherData = mapToWeatherData(apiData)
                    val forecastList = mapToForecastList(apiData)
                    val warning = checkForRoughWeather(apiData)

                    _uiState.value = WeatherUiState.Success(weatherData, forecastList, warning)
                } else {
                    _uiState.value = WeatherUiState.Error("Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _uiState.value = WeatherUiState.Error("Failed to fetch weather: ${e.message}")
            }
        }
    }

    // --- Data Mapping Functions ---

    private fun mapToWeatherData(apiData: WeatherApiResponse): WeatherData {
        val currentData = apiData.list.first()
        return WeatherData(
            location = apiData.city.name,
            temperature = "${currentData.main.temp.toInt()}°C",
            condition = currentData.weather.firstOrNull()?.main ?: "Clear",
            icon = mapIconToDrawable(currentData.weather.firstOrNull()?.icon)
        )
    }

    private fun mapToForecastList(apiData: WeatherApiResponse): List<ForecastItem> {
        // Get the forecast for roughly the same time (e.g., noon) for the next 5 days
        val dailyForecasts = apiData.list
            .filter { it.dtTxt.endsWith("12:00:00") } // Get noon forecast for each day
            .take(5) // Take the next 5 days

        val dayFormatter = SimpleDateFormat("E", Locale.getDefault()) // "E" gives day of week (e.g., "Mon")

        return dailyForecasts.map { forecastItem ->
            ForecastItem(
                day = dayFormatter.format(Date(forecastItem.dt * 1000L)),
                icon = mapIconToDrawable(forecastItem.weather.firstOrNull()?.icon),
                temp = "${forecastItem.main.temp.toInt()}°"
            )
        }
    }

    private fun checkForRoughWeather(apiData: WeatherApiResponse): RoughWeatherWarning {
        // Check for any "Storm" or "Rain" in the next 7 days (API gives 5 days)
        val upcomingWeather = apiData.list.filter {
            // Check for timestamps within the next 7 days
            val itemDate = Date(it.dt * 1000L)
            val now = Date()
            val diffInMillis = itemDate.time - now.time
            val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
            diffInDays in 0..6
        }

        val roughWeather = upcomingWeather.firstOrNull {
            val weatherMain = it.weather.firstOrNull()?.main
            weatherMain in listOf("Thunderstorm", "Rain", "Squall")
        }

        return if (roughWeather != null) {
            val dayFormatter = SimpleDateFormat("EEEE", Locale.getDefault()) // "EEEE" gives full day name
            val day = dayFormatter.format(Date(roughWeather.dt * 1000L))
            val details = roughWeather.weather.firstOrNull()?.description ?: "heavy rain"
            RoughWeatherWarning(
                hasWarning = true,
                title = "Rough Weather Warning",
                details = "Warning: $details expected on $day."
            )
        } else {
            RoughWeatherWarning(
                hasWarning = false,
                title = "",
                details = ""
            )
        }
    }

    private fun mapIconToDrawable(iconCode: String?): Int {
        // Map OpenWeatherMap icon codes to our drawable resources
        // YOU MUST ADD THESE ICONS TO YOUR res/drawable folder
        return when (iconCode) {
            "01d", "01n" -> R.drawable.ic_sunny // Clear
            "02d", "02n" -> R.drawable.ic_cloudy // Few clouds
            "03d", "03n" -> R.drawable.ic_cloudy // Scattered clouds
            "04d", "04n" -> R.drawable.ic_cloudy // Broken clouds
            "09d", "09n" -> R.drawable.ic_rain // Shower rain
            "10d", "10n" -> R.drawable.ic_rain // Rain
            "11d", "11n" -> R.drawable.ic_storm // Thunderstorm
            "13d", "13n" -> R.drawable.ic_snow // Snow (You'll need ic_snow.xml)
            "50d", "50n" -> R.drawable.ic_fog // Mist (You'll need ic_fog.xml)
            else -> R.drawable.ic_sunny
        }
    }
}