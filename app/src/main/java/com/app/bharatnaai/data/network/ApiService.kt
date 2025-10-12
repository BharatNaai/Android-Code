package com.app.bharatnaai.data.network

import com.app.bharatnaai.data.model.ApiResponse
import com.app.bharatnaai.data.model.CustomerDetails
import com.app.bharatnaai.data.model.ForgetPasswordRequest
import com.app.bharatnaai.data.model.ForgetPasswordResponse
import com.app.bharatnaai.data.model.LoginRequest
import com.app.bharatnaai.data.model.LoginResponse
import com.app.bharatnaai.data.model.RegisterRequest
import com.app.bharatnaai.data.model.RegisterResponse
import com.app.bharatnaai.data.model.ResetPasswordRequest
import com.app.bharatnaai.data.model.ResetPasswordResponse
import com.app.bharatnaai.data.model.SaloonDetailsRequest
import com.app.bharatnaai.data.model.SaloonDetailsResponse
import com.app.bharatnaai.data.model.TokenRefreshRequest
import com.app.bharatnaai.data.model.TokenRefreshResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {

    @POST("auth/register")
    suspend fun registerUser(
        @Body user: RegisterRequest
    ): Response<RegisterResponse>

    @POST("auth/login")
    suspend fun loginUser(
        @Body user: LoginRequest
    ): Response<LoginResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: TokenRefreshRequest
    ): Response<ApiResponse<TokenRefreshResponse>>

    @POST("auth/forgot-password")
    suspend fun forgetPassword(
        @Body user: ForgetPasswordRequest
    ): Response<ForgetPasswordResponse>

    @POST("auth/reset-password")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<ResetPasswordResponse>

    @GET("barbars/nearby-salons")
    suspend fun getNearbySaloonDetails(
        @Body salonDetailsRequest: SaloonDetailsRequest
    ): Response<SaloonDetailsResponse>

    @GET("auth/customerdetails")
    suspend fun getCustomerDetails(
        @Header("Authorization") accessToken : String
    ) :Response<CustomerDetails>
}