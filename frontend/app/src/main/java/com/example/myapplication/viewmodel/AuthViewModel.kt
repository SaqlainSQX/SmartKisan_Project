package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.datastore.AuthDataStore
import com.example.myapplication.network.RetrofitClient
import com.example.myapplication.repository.AuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Enum to track navigation events
enum class AuthEvent {
    LOGIN_SUCCESS,
    LOGOUT_SUCCESS,
    REGISTER_SUCCESS
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository: AuthRepository = AuthRepository(RetrofitClient.instance)
    private val authDataStore: AuthDataStore = AuthDataStore(application)

    // Backing protected properties
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    // Used for one-time navigation events
    private val _authEvent = MutableSharedFlow<AuthEvent>()
    val authEvent = _authEvent.asSharedFlow()

    val authToken = authDataStore.authToken

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = authRepository.login(username, password)
                if (response.isSuccessful && response.body() != null) {
                    authDataStore.saveToken(response.body()!!.accessToken)
                    _authEvent.emit(AuthEvent.LOGIN_SUCCESS)
                } else {
                    _error.value = "Login failed: ${response.errorBody()?.string()}"
                }
            } catch (e: Exception) {
                _error.value = "Login failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(username: String, password: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = authRepository.register(username, password)
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
            _authEvent.emit(AuthEvent.LOGOUT_SUCCESS)
        }
    }
}