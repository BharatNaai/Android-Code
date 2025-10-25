package com.app.bharatnaai.data.model

import com.google.gson.annotations.SerializedName
import com.google.gson.JsonObject

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
    val phone: String,
    val userId: Long
)

data class BarberSlotsAvailableResponse(
    @SerializedName("totalSlots") val totalSlots: Int,
    @SerializedName("slots") val slots: List<Slot>,
    @SerializedName("success") val success: Boolean,
    @SerializedName("barberId") val barberId: Int? = null,
    @SerializedName("serviceType") val serviceType: String? = null,
    @SerializedName("date") val date: String? = null
)

// Raw response used to accommodate heterogeneous slot objects when serviceType is COMBO
data class BarberSlotsAvailableResponseRaw(
    @SerializedName("totalSlots") val totalSlots: Int,
    @SerializedName("slots") val slots: List<JsonObject>,
    @SerializedName("success") val success: Boolean,
    @SerializedName("barberId") val barberId: Int? = null,
    @SerializedName("serviceType") val serviceType: String? = null,
    @SerializedName("date") val date: String? = null
)

data class Slot(
    @SerializedName("slotId") val id: Int,
    @SerializedName("serviceType") val serviceType: String,
    @SerializedName("slotDate") val slotDate: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("endTime") val endTime: String,
    @SerializedName("status") val status: String,
    // Not from backend; used when serviceType == COMBO to carry second slot id
    val secondaryId: Int? = null
)

data class BookingSlot(
    val success :Boolean,
    val message : String
)

data class BookingDetails(
    val barberName: String,
    val barberPhone: String,
    val salonName: String,
    val salonAddress: String,
    val salonImage: String,
    val bookingDate: String,
    val bookingId: String,
    val serviceType: String,
    val startTime: String,
    val endTime: String,
    val status: String
)
