// G:\SmartKisan_Project\frontend\app\src\main\java\com\example\myapplication\screens\CropDiseaseScreen.kt
package com.example.myapplication.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.myapplication.R
import com.example.myapplication.model.DiseasePredictionResponse
import com.example.myapplication.viewmodel.CropDiseaseViewModel
import com.example.myapplication.viewmodel.PredictionUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropDiseaseScreen(
    navController: NavController,
    viewModel: CropDiseaseViewModel = viewModel()
) {
    val context = LocalContext.current
    val imageUri by viewModel.imageUri.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    // --- ActivityResult Launchers ---

    // 1. Gallery Launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.onImageSelected(uri)
            }
        }
    )

    // 2. Camera Launcher
    var tempUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                tempUri?.let { viewModel.onImageSelected(it) }
            }
        }
    )

    // 3. Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission granted, launch camera
                val newUri = viewModel.getTmpFileUri(context)
                tempUri = newUri
                cameraLauncher.launch(newUri)
            } else {
                // Handle permission denial (e.g., show a snackbar)
                // For now, we just log it
                println("Camera permission denied.")
            }
        }
    )

    // Main UI Box with "natury" background
    Box(modifier = Modifier.fillMaxSize()) {
        // Add your nature_bg.jpg to res/drawable
        Image(
            painter = painterResource(id = R.drawable.nature_bg),
            contentDescription = "Nature Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            alpha = 0.3f // Make it subtle
        )

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Crop Disease Detection",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent, // Transparent background
                        titleContentColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            containerColor = Color.Transparent // Make Scaffold background transparent
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // --- Image Selection Box ---
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(250.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (imageUri == null) {
                            Text(
                                "Select an image to analyze",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Selected leaf",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Upload/Capture Buttons ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            // Launch gallery
                            galleryLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Gallery")
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Button(
                        onClick = {
                            // Check for camera permission
                            val permission = Manifest.permission.CAMERA
                            if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                                // Permission already granted
                                val newUri = viewModel.getTmpFileUri(context)
                                tempUri = newUri
                                cameraLauncher.launch(newUri)
                            } else {
                                // Request permission
                                cameraPermissionLauncher.launch(permission)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Camera")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Analyze Button ---
                Button(
                    onClick = { viewModel.predictDisease(context) },
                    enabled = (imageUri != null && uiState !is PredictionUiState.Loading),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Analyze Leaf", fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // --- Results Section ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 200.dp),
                    contentAlignment = Alignment.TopCenter
                ) {
                    when (val state = uiState) {
                        is PredictionUiState.Idle -> {
                            // Nothing to show
                        }
                        is PredictionUiState.Loading -> {
                            CircularProgressIndicator()
                        }
                        is PredictionUiState.Error -> {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                        is PredictionUiState.Success -> {
                            PredictionResult(response = state.response)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PredictionResult(response: DiseasePredictionResponse) {
    val confidencePercent = (response.confidence * 100).toInt()

    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(gradient)
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = response.diseaseName.replace("___", ": ").replace("_", " "),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Confidence: $confidencePercent%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Divider(modifier = Modifier.padding(vertical = 16.dp))

        // Description
        Text(
            "What it is:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = response.description,
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Solution
        Text(
            "How to fix it:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = response.solution,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}