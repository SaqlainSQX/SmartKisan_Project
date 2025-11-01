// G:\SmartKisan_Project\frontend\app\src\main\java\com\example\myapplication\network\DiseaseApiClient.kt
package com.example.myapplication.network

import com.example.myapplication.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object DiseaseApiClient {

    // Same base URL as your auth client
    private const val BASE_URL = "http://10.0.2.2:8000/"

    // We create a separate OkHttpClient because image uploads
    // can take longer, and we want logging.
    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // 1 minute timeout
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)

        // Add logging interceptor only in debug builds
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)
            builder.addInterceptor(logging)
        }

        builder.build()
    }

    val instance: DiseaseApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient) // Use our custom client
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(DiseaseApiService::class.java)
    }
}