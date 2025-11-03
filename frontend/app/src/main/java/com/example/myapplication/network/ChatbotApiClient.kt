package com.example.myapplication.network

import android.content.Context
import com.example.myapplication.datastore.AuthDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ChatbotApiClient {

    private const val BASE_URL = "http://10.0.2.2:8000/"

    // This is the "magic" part that adds the token
    private fun createAuthInterceptor(authDataStore: AuthDataStore): Interceptor {
        return Interceptor { chain ->
            // We use runBlocking here, which is generally not ideal,
            // but necessary for this synchronous interceptor.
            val token = runBlocking {
                authDataStore.authToken.first()
            }

            val request = chain.request().newBuilder()
            if (!token.isNullOrEmpty()) {
                request.addHeader("Authorization", "Bearer $token")
            }

            chain.proceed(request.build())
        }
    }

    private fun createOkHttpClient(context: Context): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val authDataStore = AuthDataStore(context)

        return OkHttpClient.Builder()
            .addInterceptor(createAuthInterceptor(authDataStore)) // Add our secure interceptor
            .addInterceptor(logging)
            .build()
    }

    // We need the application context to build this
    private var instance: ChatbotApiService? = null

    fun getInstance(context: Context): ChatbotApiService {
        if (instance == null) {
            instance = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(createOkHttpClient(context))
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ChatbotApiService::class.java)
        }
        return instance!!
    }
}