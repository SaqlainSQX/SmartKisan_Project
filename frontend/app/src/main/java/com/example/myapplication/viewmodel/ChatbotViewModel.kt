// G:\SmartKisan_Project\frontend\app\src\main\java\com\example\myapplication\viewmodel\ChatbotViewModel.kt
package com.example.myapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.ChatMessage
import com.example.myapplication.repository.ChatbotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// UI state for the message list
sealed class ChatUiState {
    data object Loading : ChatUiState()
    data class Success(val messages: List<ChatMessage>) : ChatUiState()
    data class Error(val message: String) : ChatUiState()
}

class ChatbotViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ChatbotRepository(application)

    private val _uiState = MutableStateFlow<ChatUiState>(ChatUiState.Loading)
    val uiState = _uiState.asStateFlow()

    // State for the text input field
    private val _isAwaitingResponse = MutableStateFlow(false)
    val isAwaitingResponse = _isAwaitingResponse.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = ChatUiState.Loading
            try {
                val response = repository.getHistory()
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = ChatUiState.Success(response.body()!!)
                } else {
                    _uiState.value = ChatUiState.Error("Failed to load history: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                _uiState.value = ChatUiState.Error("Error: ${e.message}")
            }
        }
    }

    fun sendMessage(messageText: String) {
        if (messageText.isBlank()) return

        // To make the UI feel fast, we add the user's message immediately
        val userMessage = ChatMessage(
            id = -1, // Temporary ID
            user_id = -1,
            role = "user",
            content = messageText,
            timestamp = ""
        )

        // Add the user message and a temporary loading message for the model
        if (_uiState.value is ChatUiState.Success) {
            _uiState.update {
                (it as ChatUiState.Success).copy(
                    messages = it.messages + userMessage
                )
            }
        } else {
            _uiState.value = ChatUiState.Success(listOf(userMessage))
        }

        _isAwaitingResponse.value = true

        // Now, send the message to the backend
        viewModelScope.launch {
            try {
                val response = repository.postMessage(messageText)

                if (response.isSuccessful && response.body() != null) {
                    // We got a real response. Add it to the list.
                    val modelMessage = response.body()!!
                    _uiState.update {
                        (it as ChatUiState.Success).copy(
                            messages = it.messages + modelMessage
                        )
                    }
                } else {
                    val errorMsg = "Error: ${response.errorBody()?.string()}"
                    _uiState.update {
                        (it as ChatUiState.Success).copy(
                            messages = it.messages + ChatMessage(0,0,"model", errorMsg, "")
                        )
                    }
                }
            } catch (e: Exception) {
                val errorMsg = "Error: ${e.message}"
                _uiState.update {
                    (it as ChatUiState.Success).copy(
                        messages = it.messages + ChatMessage(0,0,"model", errorMsg, "")
                    )
                }
            } finally {
                _isAwaitingResponse.value = false
            }
        }
    }
}