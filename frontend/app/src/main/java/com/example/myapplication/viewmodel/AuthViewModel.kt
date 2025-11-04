// G:\SmartKisan_Project\frontend\app\src\main\java\com\example\myapplication\viewmodel\AuthViewModel.kt
package com.example.myapplication.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.datastore.AuthDataStore
import com.example.myapplication.model.UserResponse
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.repository.AuthRepository
import com.example.myapplication.repository.ProfileRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// --- Your AuthEvent and ResetState classes ---
enum class AuthEvent {
    LOGIN_SUCCESS,
    LOGOUT_SUCCESS,
    REGISTER_SUCCESS,
    REQUEST_OTP_SUCCESS,
    RESET_PASSWORD_SUCCESS
}
sealed class ResetState {
    data object Idle : ResetState()
    data object Loading : ResetState()
    data class Error(val message: String) : ResetState()
}
// --- END ---

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(RetrofitClient.instance)
    private val authDataStore = AuthDataStore(application)
    private val profileRepository = ProfileRepository(application)

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _authEvent = MutableSharedFlow<AuthEvent>()
    val authEvent = _authEvent.asSharedFlow()

    val authToken = authDataStore.authToken

    private val _resetState = MutableStateFlow<ResetState>(ResetState.Idle)
    val resetState = _resetState.asStateFlow()

    private val _profileState = MutableStateFlow<UserResponse?>(null)
    val profileState = _profileState.asStateFlow()

    init {
        viewModelScope.launch {
            authToken.collect { token ->
                if (!token.isNullOrEmpty()) {
                    loadProfile()
                }
            }
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            val response = profileRepository.getProfile()
            if (response.isSuccessful) {
                _profileState.value = response.body()
            } else {
                _error.value = "Could not load profile."
            }
        }
    }

    fun updateProfile(name: String, contact: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = profileRepository.updateProfile(name, contact)
            if (response.isSuccessful) {
                _profileState.value = response.body()
            } else {
                _error.value = "Failed to update profile."
            }
            _isLoading.value = false
        }
    }

    fun updateProfilePhoto(uri: Uri) {
        viewModelScope.launch {
            _isLoading.value = true
            val response = profileRepository.updateProfilePhoto(getApplication(), uri)
            if (response.isSuccessful) {
                _profileState.value = response.body()
            } else {
                _error.value = "Failed to update photo."
            }
            _isLoading.value = false
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = authRepository.login(email, password)
                if (response.isSuccessful && response.body() != null) {
                    authDataStore.saveToken(response.body()!!.accessToken)
                    loadProfile()
                    _authEvent.emit(AuthEvent.LOGIN_SUCCESS)
                } else {
                    _error.value = "Login failed: Incorrect email or password"
                }
            } catch (e: Exception) {
                _error.value = "Login failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = authRepository.register(email, password)
                if (response.isSuccessful) {
                    _authEvent.emit(AuthEvent.REGISTER_SUCCESS)
                } else {
                    _error.value = "Registration failed: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _error.value = "Registration failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authDataStore.clearToken()
            _profileState.value = null // Clear the profile
            _authEvent.emit(AuthEvent.LOGOUT_SUCCESS)
        }
    }

    fun requestOtp(email: String) {
        viewModelScope.launch {
            _resetState.value = ResetState.Loading
            try {
                val response = authRepository.forgotPassword(email)
                if (response.isSuccessful) {
                    _authEvent.emit(AuthEvent.REQUEST_OTP_SUCCESS)
                } else {
                    _resetState.value = ResetState.Error(response.errorBody()?.string() ?: "Unknown error")
                }
            } catch (e: Exception) {
                _resetState.value = ResetState.Error(e.message ?: "Network error")
            }
        }
    }

    fun clearResetState() {
        _resetState.value = ResetState.Idle
    }

    // --- THIS IS THE FIX ---
    // This function was missing
    fun clearError() {
        _error.value = null
    }
    // --- END FIX ---

    fun resetPassword(email: String, otp: String, newPassword: String) {
        viewModelScope.launch {
            _resetState.value = ResetState.Loading
            try {
                val response = authRepository.resetPassword(email, otp, newPassword)
                if (response.isSuccessful) {
                    _authEvent.emit(AuthEvent.RESET_PASSWORD_SUCCESS)
                } else {
                    _resetState.value = ResetState.Error(response.errorBody()?.string() ?: "Invalid OTP or request")
                }
            } catch (e: Exception) {
                _resetState.value = ResetState.Error(e.message ?: "Network error")
            }
        }
    }
}