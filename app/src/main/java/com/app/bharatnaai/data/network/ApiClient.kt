package com.app.bharatnaai.data.network

import android.content.Context
import com.app.bharatnaai.utils.Constants
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    
    private var authInterceptor: AuthInterceptor? = null
    
    private val loggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    private val headerInterceptor by lazy {
        Interceptor { chain ->
            val originalRequest = chain.request()
            val requestBuilder = originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
            
            chain.proceed(requestBuilder.build())
        }
    }
    
    fun initialize(context: Context) {
        authInterceptor = AuthInterceptor(context)
    }
    
    private val okHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
        
        // Add auth interceptor if available
        authInterceptor?.let { builder.addInterceptor(it) }
        
        builder.build()
    }
    
    private val retrofit by lazy {
        if (Constants.BASE_URL.isBlank()) {
            throw IllegalStateException("BASE_URL cannot be empty")
        }
        Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}
