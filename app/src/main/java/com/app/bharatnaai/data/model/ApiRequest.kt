package com.app.bharatnaai.data.model

import com.google.gson.annotations.SerializedName

data class ApiRequest<T>(
    @SerializedName("token")
    val token: String
)

data class TokenRefreshRequest(
    @SerializedName("refreshToken")
    val refreshToken: String
)

data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

data class RegisterRequest(
    @SerializedName("username")
    val username: String,
    @SerializedName("email")
    val email: String,
    @SerializedName("phone")
    val phone: String,
    @SerializedName("password")
    val password: String
)

data class ForgetPasswordRequest(
    @SerializedName("email")
    val email :String
)

data class ResetPasswordRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("otp")
    val otp: String,
    @SerializedName("password")
    val password: String
)

data class SaloonDetailsRequest(
    @SerializedName("authorization")
    val authToken :String,
    @SerializedName("lat")
    val lat: String,
    @SerializedName("lng")
    val lng: String
)