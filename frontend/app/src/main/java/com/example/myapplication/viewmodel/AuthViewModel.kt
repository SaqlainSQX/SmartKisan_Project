package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.datastore.AuthDataStore
import com.example.myapplication.repository.AuthRepository
// --- ADD THIS IMPORT ---
import com.example.myapplication.network.RetrofitClient
// --- END ADD ---
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AuthEvent {
    LOGIN_SUCCESS,
    LOGOUT_SUCCESS,
    REGISTER_SUCCESS,
    RESET_PASSWORD_SUCCESS
}

sealed class ResetState {
    data object Idle : ResetState()
    data object Loading : ResetState()
    data class Error(val message: String) : ResetState()
    data class Success(val message: String) : ResetState()
}


class AuthViewModel(application: Application) : AndroidViewModel(application) {

    // --- THIS IS THE FIX ---
    // We now pass the ApiService instance to the repository
    private val authRepository: AuthRepository = AuthRepository(RetrofitClient.instance)
    // --- END FIX ---
    private val authDataStore: AuthDataStore = AuthDataStore(application)

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _authEvent = MutableSharedFlow<AuthEvent>()
    val authEvent = _authEvent.asSharedFlow()

    val authToken = authDataStore.authToken

    private val _resetState = MutableStateFlow<ResetState>(ResetState.Idle)
    val resetState = _resetState.asStateFlow()


    fun login(email: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = authRepository.login(email, password)
                if (response.isSuccessful && response.body() != null) {
                    authDataStore.saveToken(response.body()!!.accessToken)
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
        // Client-side validation
        if (password.length < 8) {
            _error.value = "Password must be at least 8 characters"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = authRepository.register(email, password)
                if (response.isSuccessful) {
                    _authEvent.emit(AuthEvent.REGISTER_SUCCESS)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val detail = errorBody?.let {
                        it.split("\"detail\":\"")[1].split("\"")[0]
                    } ?: "Registration failed"
                    _error.value = "Registration failed: $detail"
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
            _authEvent.emit(AuthEvent.LOGOUT_SUCCESS)
        }
    }

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            _resetState.value = ResetState.Loading
            try {
                val response = authRepository.forgotPassword(email)
                if (response.isSuccessful && response.body() != null) {
                    _resetState.value = ResetState.Success(response.body()!!.message)
                } else {
                    _resetState.value = ResetState.Error("Failed to send OTP.")
                }
            } catch (e: Exception) {
                _resetState.value = ResetState.Error("Error: ${e.message}")
            }
        }
    }

    fun resetPassword(email: String, otp: String, newPassword: String) {
        if (newPassword.length < 8) {
            _resetState.value = ResetState.Error("Password must be at least 8 characters")
            return
        }

        viewModelScope.launch {
            _resetState.value = ResetState.Loading
            try {
                val response = authRepository.resetPassword(email, otp, newPassword)
                if (response.isSuccessful && response.body() != null) {
                    _resetState.value = ResetState.Success(response.body()!!.message)
                    _authEvent.emit(AuthEvent.RESET_PASSWORD_SUCCESS)
                } else {
                    val errorBody = response.errorBody()?.string()
                    val detail = errorBody?.let {
                        it.split("\"detail\":\"")[1].split("\"")[0]
                    } ?: "Invalid or expired OTP"
                    _resetState.value = ResetState.Error(detail)
                }
            } catch (e: Exception) {
                _resetState.value = ResetState.Error("Error: ${e.message}")
            }
        }
    }

    fun clearResetState() {
        _resetState.value = ResetState.Idle
    }

    fun clearError() {
        _error.value = null
    }
}


