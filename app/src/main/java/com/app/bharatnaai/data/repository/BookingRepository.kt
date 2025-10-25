package com.app.bharatnaai.data.repository

import android.content.Context
import com.app.bharatnaai.data.model.BookingDetails
import com.app.bharatnaai.data.network.ApiService
import com.app.bharatnaai.utils.CommonMethod

class BookingRepository(
    private val context: Context,
    private val apiService: ApiService
) {
    private val commonMethod = CommonMethod()

    suspend fun getBookingDetails(userId: Long): List<BookingDetails> {
        if (!commonMethod.isInternetAvailable(context)) {
            throw Exception("No internet connection")
        }
        val resp = apiService.getBookingDetails(userId)
        if (!resp.isSuccessful) throw Exception("Failed to load bookings")
        return resp.body() ?: emptyList()
    }
}
