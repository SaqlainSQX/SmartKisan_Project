// G:\SmartKisan_Project\frontend\app\src\main\java\com\example\myapplication\viewmodel\CropDiseaseViewModel.kt
package com.example.myapplication.viewmodel

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.BuildConfig
import com.example.myapplication.model.DiseasePredictionResponse
import com.example.myapplication.repository.DiseaseRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

// UI State for the prediction screen
sealed class PredictionUiState {
    data object Idle : PredictionUiState()
    data object Loading : PredictionUiState()
    data class Success(val response: DiseasePredictionResponse) : PredictionUiState()
    data class Error(val message: String) : PredictionUiState()
}

class CropDiseaseViewModel : ViewModel() {

    private val repository = DiseaseRepository()

    private val _uiState = MutableStateFlow<PredictionUiState>(PredictionUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _imageUri = MutableStateFlow<Uri?>(null)
    val imageUri = _imageUri.asStateFlow()

    fun onImageSelected(uri: Uri) {
        _imageUri.value = uri
        // Reset state if a new image is selected
        _uiState.value = PredictionUiState.Idle
    }

    fun getTmpFileUri(context: Context): Uri {
        val tmpFile = File.createTempFile("temp_image", ".jpg", context.cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${BuildConfig.APPLICATION_ID}.provider", // Must match AndroidManifest
            tmpFile
        )
        onImageSelected(uri)
        return uri
    }

    fun predictDisease(context: Context) {
        val uri = _imageUri.value
        if (uri == null) {
            _uiState.value = PredictionUiState.Error("No image selected.")
            return
        }

        viewModelScope.launch {
            _uiState.value = PredictionUiState.Loading
            try {
                val response = repository.predictDisease(context, uri)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = PredictionUiState.Success(response.body()!!)
                } else {
                    _uiState.value = PredictionUiState.Error("Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _uiState.value = PredictionUiState.Error("Prediction failed: ${e.message}")
            }
        }
    }
}