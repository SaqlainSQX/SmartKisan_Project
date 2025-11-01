package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
// --- IMPORTS UPDATED ---
import com.example.myapplication.screens.ChatbotScreen
import com.example.myapplication.screens.CropDiseaseScreen
import com.example.myapplication.screens.ForumScreen
import com.example.myapplication.screens.MarketplaceScreen
import com.example.myapplication.screens.HomeScreen
import com.example.myapplication.screens.LoginScreen
import com.example.myapplication.screens.RegisterScreen
import com.example.myapplication.screens.SplashScreen
import com.example.myapplication.viewmodel.AuthViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    // Create a single instance of the ViewModel to be shared
    val authViewModel: AuthViewModel = viewModel()

    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") {
            SplashScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("login") {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("register") {
            RegisterScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("home") {
            HomeScreen(navController = navController, authViewModel = authViewModel)
        }

        // --- NEW SCREENS ---
        composable("crop_disease") {
            CropDiseaseScreen(navController = navController)
        }
        composable("chatbot") {
            ChatbotScreen(navController = navController)
        }
        composable("marketplace") {
            MarketplaceScreen(navController = navController)
        }
        composable("forum") {
            ForumScreen(navController = navController)
        }
    }
}
