// G:\SmartKisan_Project\frontend\app\src\main\java\com\example\myapplication\repository\ChatbotRepository.kt
package com.example.myapplication.repository

import android.content.Context
import com.example.myapplication.model.ChatRequest
import com.example.myapplication.network.ChatbotApiClient

class ChatbotRepository(context: Context) {

    private val chatbotService = ChatbotApiClient.getInstance(context)

    suspend fun getHistory() = chatbotService.getChatHistory()

    suspend fun postMessage(message: String) =
        chatbotService.postChatMessage(ChatRequest(message = message))
}