package com.example.myapplication.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // !!! IMPORTANT !!!
    // If you are running the backend on your computer and testing on an
    // Android Emulator, use 10.0.2.2 to access your host machine's localhost.
    // If testing on a physical device, use your computer's network IP.
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}
