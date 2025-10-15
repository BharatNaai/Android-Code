package com.app.bharatnaai.data.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String,
    @SerializedName("data")
    val data: T? = null,
    @SerializedName("error")
    val error: String? = null
)

data class RegisterResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)

data class LoginResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("message")
    val message: String
)

data class TokenRefreshResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("message")
    val message: String
)

data class ForgetPasswordResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message :String,
    @SerializedName("otp")
    val otp: String
)

data class ResetPasswordResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("message")
    val message: String
)

data class SaloonDetailsResponse(
    val nearbySalons: List<Salon>,
    val message: String,
    val success: Boolean
)

data class Salon(
    val salonId: Int,
    val salonName: String,
    val address: String?, // nullable
    val imagePath: String,
    val latitude: Double,
    val longitude: Double,
    val barbers: List<Barber>
)

data class Barber(
    val barberId: Int,
    val barberName: String,
    val phone: String,
    val email: String
)

data class CustomerDetails(
    val email: String,
    val fullName: String,
    val phone: String
)

