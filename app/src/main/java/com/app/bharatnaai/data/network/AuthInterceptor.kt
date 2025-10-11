package com.app.bharatnaai.data.network

import android.content.Context
import com.app.bharatnaai.data.repository.ApiResult
import com.app.bharatnaai.data.repository.AuthRepository
import com.app.bharatnaai.data.session.SessionManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val context: Context) : Interceptor {
    
    private val sessionManager = SessionManager.getInstance(context)
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        // Skip adding token for auth endpoints
        val url = originalRequest.url.toString()
        if (url.contains("/auth/login") || url.contains("/auth/register") || url.contains("/auth/refresh")) {
            return chain.proceed(originalRequest)
        }
        
        // Get access token
        val accessToken = sessionManager.getAccessToken()
        
        // If no token, proceed without authorization
        if (accessToken == null) {
            return chain.proceed(originalRequest)
        }
        
        // Check if token is expired and refresh if needed
        if (sessionManager.isAccessTokenExpired()) {
            val refreshResult = runBlocking {
                val authRepository = AuthRepository(context)
                authRepository.refreshToken()
            }
            
            when (refreshResult) {
                is ApiResult.Success -> {
                    // Token refreshed successfully, use new token
                    val newToken = sessionManager.getAccessToken()
                    val requestWithNewToken = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                    return chain.proceed(requestWithNewToken)
                }
                is ApiResult.Error -> {
                    // Refresh failed, clear session and proceed without token
                    sessionManager.clearSession()
                    return chain.proceed(originalRequest)
                }
                else -> {
                    // Shouldn't happen, but handle gracefully
                    return chain.proceed(originalRequest)
                }
            }
        }
        
        // Add token to request
        val requestWithToken = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
        
        val response = chain.proceed(requestWithToken)
        
        // If we get 401, try to refresh token once
        if (response.code == 401) {
            response.close()
            
            val refreshResult = runBlocking {
                val authRepository = AuthRepository(context)
                authRepository.refreshToken()
            }
            
            when (refreshResult) {
                is ApiResult.Success -> {
                    // Token refreshed, retry original request with new token
                    val newToken = sessionManager.getAccessToken()
                    val retryRequest = originalRequest.newBuilder()
                        .header("Authorization", "Bearer $newToken")
                        .build()
                    return chain.proceed(retryRequest)
                }
                is ApiResult.Error -> {
                    // Refresh failed, clear session
                    sessionManager.clearSession()
                    // Return the original 401 response
                    return chain.proceed(originalRequest)
                }
                else -> {
                    return chain.proceed(originalRequest)
                }
            }
        }
        
        return response
    }
}
