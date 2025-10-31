package com.example.myapplication.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.viewmodel.AuthViewModel
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {

    LaunchedEffect(Unit) { // Runs only once when the composable enters the screen
        // Collect the *first* value emitted by the authToken flow.
        // This suspends until DataStore provides its initial value.

        // --- FIX ---
        // Explicitly defined the type as String? to fix
        // "Cannot infer type" compiler error.
        val token: String? = authViewModel.authToken.first()

        // --- FIX ---
        // Replaced `token.isNullOrEmpty()` with an explicit check
        // to resolve the overload resolution ambiguity.
        if (token == null || token.isEmpty()) {
            // No token, go to login
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            // Token exists, go to home
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    // Show a loading spinner while the LaunchedEffect is running
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

