package com.app.bharatnaai.data.repository

import android.content.Context
import com.app.bharatnaai.data.model.ApiResponse
import com.app.bharatnaai.data.model.ForgetPasswordRequest
import com.app.bharatnaai.data.model.ForgetPasswordResponse
import com.app.bharatnaai.data.model.LoginRequest
import com.app.bharatnaai.data.model.LoginResponse
import com.app.bharatnaai.data.model.RegisterRequest
import com.app.bharatnaai.data.model.RegisterResponse
import com.app.bharatnaai.data.model.ResetPasswordRequest
import com.app.bharatnaai.data.model.ResetPasswordResponse
import com.app.bharatnaai.data.model.TokenRefreshRequest
import com.app.bharatnaai.data.model.TokenRefreshResponse
import com.app.bharatnaai.data.network.ApiClient
import com.app.bharatnaai.data.session.SessionManager
import com.app.bharatnaai.utils.CommonMethod
import retrofit2.Response

class AuthRepository(private val context: Context) {
    
    private val apiService = ApiClient.apiService
    private val sessionManager = SessionManager.getInstance(context)
    private val commonMethod = CommonMethod()

    suspend fun registerUser(user: RegisterRequest): ApiResult<RegisterResponse> {
        if (!commonMethod.isInternetAvailable(context)) {
            return ApiResult.Error("No internet connection")
        }
        return try {
            val response = apiService.registerUser(user)
            if (response.isSuccessful) {
                response.body()?.let {
                    ApiResult.Success(it)
                } ?: ApiResult.Error("Empty response from server")
            } else {
                ApiResult.Error("Registration failed: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Registration failed")
        }
    }

    suspend fun loginUser(email: String, password: String): ApiResult<LoginResponse> {
        if (!commonMethod.isInternetAvailable(context)) {
            return ApiResult.Error("No internet connection")
        }
        return try {
            val loginRequest = LoginRequest(email = email, password = password)
            val response = apiService.loginUser(loginRequest)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    // Save tokens if login is successful
                    sessionManager.saveTokens(
                        accessToken = body.accessToken,
                        refreshToken = body.refreshToken
                    )
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error("Empty response from server")
                }
            } else {
                ApiResult.Error("Login failed: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Login failed")
        }
    }

    suspend fun refreshToken(): ApiResult<TokenRefreshResponse> {
        if (!commonMethod.isInternetAvailable(context)) {
            return ApiResult.Error("No internet connection")
        }
        return try {
            val refreshToken = sessionManager.getRefreshToken()
                ?: return ApiResult.Error("No refresh token available")
            
            val request = TokenRefreshRequest(refreshToken)
            val response = apiService.refreshToken(request)
            val result = response.toApiResult()
            
            // Update access token if refresh is successful
            if (result is ApiResult.Success) {
                sessionManager.updateAccessToken(result.data.accessToken)
            }
            
            result
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Token refresh failed")
        }
    }

    suspend fun forgetPassword(email: String): ApiResult<ForgetPasswordResponse>{
        if (!commonMethod.isInternetAvailable(context)) {
            return ApiResult.Error("No internet connection")
        }
        return try{
            val forgetpasswordRequest = ForgetPasswordRequest(email = email)
            val response = apiService.forgetPassword(forgetpasswordRequest)

            if(response.isSuccessful){
                val body = response.body()
                if(body != null){
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error("Empty response from server")
                }
            } else {
                ApiResult.Error("Failed: ${response.message()}")
            }
        } catch (e :Exception){
            ApiResult.Error(e.message ?: "Forgot password failed")
        }
    }

    suspend fun resetPassword(email: String, otp: String, password: String): ApiResult<ResetPasswordResponse> {
        if (!commonMethod.isInternetAvailable(context)) {
            return ApiResult.Error("No internet connection")
        }
        return try {
            val resetPasswordRequest = ResetPasswordRequest(
                email = email,
                otp = otp,
                password = password
            )
            val response = apiService.resetPassword(resetPasswordRequest)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    ApiResult.Success(body)
                } else {
                    ApiResult.Error("Empty response from server")
                }
            } else {
                ApiResult.Error("Reset password failed: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Reset password failed")
        }
    }
    
    fun logout() {
        sessionManager.clearSession()
    }
    
    fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }
    
    fun isTokenExpired(): Boolean {
        return sessionManager.isAccessTokenExpired()
    }
    
    fun getAccessToken(): String? {
        return sessionManager.getAccessToken()
    }
}

// Extension function to handle API responses
sealed class ApiResult<T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error<T>(val message: String) : ApiResult<T>()
    data class Loading<T>(val isLoading: Boolean = true) : ApiResult<T>()
}

// Extension function to convert Response to ApiResult
fun <T> Response<ApiResponse<T>>.toApiResult(): ApiResult<T> {
    return if (isSuccessful) {
        val body = body()
        if (body != null && body.success) {
            body.data?.let { 
                ApiResult.Success(it) 
            } ?: ApiResult.Error("No data received")
        } else {
            ApiResult.Error(body?.message ?: "Unknown error occurred")
        }
    } else {
        ApiResult.Error("Network error: ${message()}")
    }
}
