package com.app.bharatnaai.data.network

import com.app.bharatnaai.data.model.ApiResponse
import com.app.bharatnaai.data.model.BarberSlotsAvailableResponse
import com.app.bharatnaai.data.model.CustomerDetails
import com.app.bharatnaai.data.model.ForgetPasswordRequest
import com.app.bharatnaai.data.model.ForgetPasswordResponse
import com.app.bharatnaai.data.model.LoginRequest
import com.app.bharatnaai.data.model.LoginResponse
import com.app.bharatnaai.data.model.RegisterRequest
import com.app.bharatnaai.data.model.RegisterResponse
import com.app.bharatnaai.data.model.ResetPasswordRequest
import com.app.bharatnaai.data.model.ResetPasswordResponse
import com.app.bharatnaai.data.model.Salon
import com.app.bharatnaai.data.model.TokenRefreshRequest
import com.app.bharatnaai.data.model.TokenRefreshResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    @GET("barbers/nearby-salons")
    suspend fun getNearbySaloonDetails(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double
    ): Response<List<Salon>>

    @GET("auth/customerdetails")
    suspend fun getCustomerDetails(
        @Header("Authorization") accessToken : String
    ) :Response<CustomerDetails>

    @GET("barbers/salon/{salonId}")
    suspend fun getSalonDetails(
        @Path("salonId") salonId: Int
    ): Response<Salon>

    @GET("barbers/slots")
    suspend fun getAvailableSlots(
        @Query("barberId") barberId: Int,
        @Query("date") date: String
    ): Response<BarberSlotsAvailableResponse>
}