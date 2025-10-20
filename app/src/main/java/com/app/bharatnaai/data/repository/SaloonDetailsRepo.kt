package com.app.bharatnaai.data.repository

import com.app.bharatnaai.data.model.Salon
import com.app.bharatnaai.data.network.ApiService

class SaloonDetailsRepo(private val apiService: ApiService) {

    suspend fun fetchSalonDetails(salonId: Int): Salon? {
        val response = apiService.getSalonDetails(salonId)
        return if (response.isSuccessful) response.body() else null
    }
}
