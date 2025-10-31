package com.example.myapplication.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.viewmodel.AuthEvent
import com.example.myapplication.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {

    // Listen for logout event to navigate
    LaunchedEffect(Unit) {
        authViewModel.authEvent.collectLatest { event ->
            if (event == AuthEvent.LOGOUT_SUCCESS) {
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Welcome!", style = MaterialTheme.typography.headlineMedium)
            Text("You are logged in.")
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { authViewModel.logout() }) {
                Text("Log Out")
            }
        }
    }
}