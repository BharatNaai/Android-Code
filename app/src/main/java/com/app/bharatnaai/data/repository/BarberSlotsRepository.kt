package com.app.bharatnaai.data.repository

import com.app.bharatnaai.data.model.BarberSlotsAvailableResponse
import com.app.bharatnaai.data.network.ApiService

class BarberSlotsRepository(private val apiService: ApiService) {
    suspend fun getAvailableSlots(barberId: Int, date: String): BarberSlotsAvailableResponse? {
        val response = apiService.getAvailableSlots(barberId, date)
        return if (response.isSuccessful) response.body() else null
    }
}
