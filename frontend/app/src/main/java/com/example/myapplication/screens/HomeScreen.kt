package com.example.myapplication.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.myapplication.R
import com.example.myapplication.viewmodel.AuthEvent
import com.example.myapplication.viewmodel.AuthViewModel
// --- Import the new Weather VM and State ---
import com.example.myapplication.viewmodel.WeatherViewModel
import com.example.myapplication.viewmodel.WeatherUiState
import com.example.myapplication.viewmodel.WeatherData
import com.example.myapplication.viewmodel.ForecastItem
import com.example.myapplication.viewmodel.RoughWeatherWarning
import kotlinx.coroutines.flow.collectLatest

// --- Dashboard Item Data ---
data class DashboardItem(
    val title: String,
    val icon: ImageVector,
    val route: String
)

val dashboardItems = listOf(
    DashboardItem("Crop Disease", Icons.Filled.LocalFlorist, "crop_disease"),
    DashboardItem("AI Chatbot", Icons.Filled.Chat, "chatbot"),
    DashboardItem("Marketplace", Icons.Filled.Storefront, "marketplace"),
    DashboardItem("Community", Icons.Filled.Groups, "forum")
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    // --- Add the Weather ViewModel ---
    weatherViewModel: WeatherViewModel = viewModel()
) {

    // --- Observe Weather State ---
    val weatherState by weatherViewModel.uiState.collectAsState()

    // --- YOUR EXISTING LOGOUT LOGIC ---
    // (This is preserved from your file)
    LaunchedEffect(Unit) {
        authViewModel.authEvent.collectLatest { event ->
            if (event == AuthEvent.LOGOUT_SUCCESS) {
                navController.navigate("login") {
                    popUpTo("home") { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SmartKisaan", fontWeight = FontWeight.Bold) },
                actions = {
                    // --- YOUR LOGOUT BUTTON (MOVED) ---
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(Icons.Filled.Logout, contentDescription = "Log Out")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // --- 1. Weather Section (with State Handling) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp), // Give a fixed height to prevent jumps
                contentAlignment = Alignment.Center
            ) {
                when (val state = weatherState) {
                    is WeatherUiState.Loading -> {
                        CircularProgressIndicator()
                    }
                    is WeatherUiState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    is WeatherUiState.Success -> {
                        // Weather Card
                        WeatherCard(
                            weather = state.weatherData,
                            forecast = state.forecastList
                        )
                    }
                }
            }

            // --- Rough Weather Warning (with State Handling) ---
            if (weatherState is WeatherUiState.Success) {
                val warning = (weatherState as WeatherUiState.Success).warning
                RoughWeatherWarning(warning)
            }


            // --- 2. Middle Section (4 Features) ---
            Text(
                "Features",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxWidth()
                    // .heightIn(min = 300.dp), // <-- THIS IS THE PROBLEM
                    .height(350.dp), // <-- THIS IS THE FIX
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp),
                // This is correct, keep it!
                userScrollEnabled = false
            ) {
                items(dashboardItems) { item ->
                    FeatureCard(item = item) {
                        navController.navigate(item.route)
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherCard(weather: WeatherData, forecast: List<ForecastItem>) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth() // Changed from fillMaxSize
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    weather.location,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Light
                )

                Image(
                    painter = painterResource(id = weather.icon),
                    contentDescription = weather.condition,
                    modifier = Modifier.size(80.dp),
                    contentScale = ContentScale.Fit
                )

                Text(
                    weather.temperature,
                    style = MaterialTheme.typography.displayMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    weather.condition,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )

                Divider(
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(vertical = 20.dp)
                )

                // 5-Day Forecast
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    items(forecast.size) { index ->
                        val item = forecast[index]
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                item.day,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Light
                            )
                            Image(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.day,
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(vertical = 4.dp),
                                contentScale = ContentScale.Fit
                            )
                            Text(
                                item.temp,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RoughWeatherWarning(warning: RoughWeatherWarning) {
    if (warning.hasWarning) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.WarningAmber,
                    contentDescription = "Warning",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(warning.title, fontWeight = FontWeight.Bold)
                    Text(
                        warning.details,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureCard(item: DashboardItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .aspectRatio(1f) // Makes the card square
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = item.title,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }
    }
}
