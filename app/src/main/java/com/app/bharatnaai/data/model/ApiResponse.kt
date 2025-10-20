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

data class CustomerDetails(
    val email: String,
    val fullName: String,
    val phone: String
)

data class BarberSlotsAvailableResponse(
    val totalSlots: Int,
    val slots: List<Slot>,
    val success: Boolean
)

data class Slot(
    val id: Int,
    val barberId: Int,
    val salonId: Int,
    val serviceType: String,
    val slotDate: String,
    val startTime: String,
    val endTime: String,
    val status: String
)

