# SmartKisan_Project
# 🌿 Plant Disease Detection & Agri-Assistant

A full-stack mobile application built with **FastAPI (Python)** and **Jetpack Compose (Kotlin)** that uses AI to detect crop diseases and provide comprehensive agricultural support. Users can take or upload a photo of a plant leaf, and the app's backend will identify the disease using a custom-trained model and provide detailed remediation information using the Google Gemini API.



---

## ✨ Features

* 📸 **AI Disease Detection:** Upload a leaf photo (or use the camera) to get an instant disease prediction from a custom-trained `.h5` model.
* 🤖 **Gemini-Enhanced Results:** Prediction results are enhanced by the Google Gemini API to provide detailed causes, descriptions, and treatment solutions.
* 🌦️ **Real-time Weather:** A dashboard component displays current weather conditions and alerts for the user's location using the OpenWeatherMap API.
* 🛒 **Marketplace:** A hub for users to buy or sell agricultural products.
* 👥 **Community Forum:** A place for users to connect, ask questions, and share agricultural advice.
* 🤖 **Agri-Assistant Chatbot:** An AI-powered chatbot to answer farming-related questions.
* 🔒 **User Profile & Authentication:** Secure user registration, login, and a profile section. All powered by JWT for secure API access.

---

## 🔁 Project Flow & Data Logic

This diagram and description explain the end-to-end flow for the core disease detection feature.

**[High-level flow diagram: (Frontend) -> (FastAPI Backend) -> (AI Model + Gemini) -> (Frontend)]**

### User & Application Flow

1.  **Authentication:** A user opens the app, sees the **SplashScreen**, and is navigated to **LoginScreen** (or **HomeScreen** if already logged in). They can register (via **RegisterScreen**) or log in.
2.  **API Call (Login):** The **AuthViewModel** sends the user's credentials via Retrofit to the FastAPI backend.
3.  **Backend (Auth):** The `/token` endpoint in `auth.py` verifies the credentials against the **PostgreSQL** database (using `crud.py`) and returns a JWT access token.
4.  **Home:** The token is saved, and the user lands on **HomeScreen**. The **WeatherViewModel** makes a separate API call to OpenWeatherMap, and the UI displays the results.
5.  **Disease Detection:** The user navigates to **CropDiseaseScreen**.
    * The user grants camera/gallery permissions.
    * The user selects an image.
    * The **CropDiseaseViewModel** displays the image and prepares for upload.
6.  **API Call (Prediction):** When the user hits "Predict," the **DiseaseRepository** converts the image into a `MultipartBody.Part` and sends it to the backend using a special Retrofit client with a long timeout (`DiseaseApiClient`).
7.  **Backend (Prediction):** The `/disease/predict` endpoint in `disease_router.py` receives the request:
    * It first sends the image to `disease_predictor.py`, which loads the `plant_disease_model.h5` model and predicts the disease (e.g., "5").
    * It uses `labels.json` to map the output "5" to a name (e.g., "Tomato___Late_blight").
    * It then calls `gemini_service.py`, which sends the disease name ("Tomato___Late_blight") to the Google Gemini API with a prompt asking for a cause, description, and treatment.
    * The backend combines the model's prediction and Gemini's text into a single JSON response.
8.  **Display Results:** The **CropDiseaseViewModel** receives the JSON response. The UI on **CropDiseaseScreen** updates from a "loading" state to display the prediction, image, and detailed help text from Gemini.

---

## 🛠️ Tech Stack

| Backend (FastAPI) | Frontend (Android) |
| :--- | :--- |
| Python 3.10+ | Kotlin |
| FastAPI & Uvicorn | Jetpack Compose |
| PostgreSQL | Retrofit 2 |
| SQLAlchemy (ORM) | Kotlin Coroutines & Flow |
| Pantic | ViewModel |
| JWT (passlib & python-jose) | Coil (Image Loading) |
| TensorFlow/Keras | Android SDK |
| Google Gemini API | |
| OpenWeatherMap API | |

---

## 📋 Prerequisites

Before you begin, ensure you have the following software installed on your local machine:

* **Python:** Version 3.10 or later.
* **PostgreSQL:** A running instance of a PostgreSQL database.
* **Android Studio:** Latest stable version (e.g., Koala or newer).
* **Java Development Kit (JDK):** Version 17 or newer (usually bundled with Android Studio).
* **An Android Device or Emulator:** Running API level 26 (Oreo) or higher.

---

## 🔑 Configuration & Credentials

This project requires several API keys and a database connection. **Never hardcode these values.** Use environment variables for the backend and local constants for the frontend as described below.

### 1. Backend (`app_backend/app/`)

Create a file named `.env` in the `app_backend/app/` directory. This file will store all your secret keys and credentials.

```dotenv
# .env file
# -------------------------------------
# PostgreSQL Database
# Replace with your actual database username, password, and database name
DATABASE_URL="postgresql://YOUR_DB_USER:YOUR_DB_PASSWORD@localhost/YOUR_DB_NAME"

# JWT Settings
# Generate a strong random string for the secret key
SECRET_KEY="your_super_secret_key_for_jwt"
ALGORITHM="HS256"
ACCESS_TOKEN_EXPIRE_MINUTES=30

# Google Gemini API
# Get your key from Google AI Studio
GEMINI_API_KEY="your_google_gemini_api_key"
