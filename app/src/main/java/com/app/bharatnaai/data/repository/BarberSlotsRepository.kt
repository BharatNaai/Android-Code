package com.app.bharatnaai.data.repository

import android.content.Context
import com.app.bharatnaai.data.model.BarberSlotsAvailableResponse
import com.app.bharatnaai.data.network.ApiService
import com.app.bharatnaai.utils.CommonMethod

class BarberSlotsRepository(
    private val context: Context,
    private val apiService: ApiService
) {
    private val commonMethod = CommonMethod()

    suspend fun getAvailableSlots(barberId: Int, date: String): BarberSlotsAvailableResponse? {
        if (!commonMethod.isInternetAvailable(context)) {
            throw Exception("No internet connection")
        }
        val response = apiService.getAvailableSlots(barberId, date)
        return if (response.isSuccessful) response.body() else null
    }
}
