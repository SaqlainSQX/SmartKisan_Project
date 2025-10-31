package com.example.myapplication.repository

import com.example.myapplication.model.UserCreate
import com.example.myapplication.network.ApiService

// The repository abstracts the data source (network)
// The repository abstracts the data source (network)
class AuthRepository(private val apiService: ApiService) {

    suspend fun login(username: String, password: String) =
        // --- FIX: Added grantType = "password" ---
        apiService.login(username = username, password = password, grantType = "password")

    suspend fun register(username: String, password: String) =
        apiService.register(UserCreate(username, password))
}
