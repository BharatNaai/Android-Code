package com.app.bharatnaai.data.repository

import com.app.bharatnaai.data.network.ApiClient
import com.app.bharatnaai.data.model.Salon
import retrofit2.Response

class NearbySaloonRepository {
    private val apiService = ApiClient.apiService

    suspend fun getNearbySaloonDetails(lat : Double, lng: Double): Response<List<Salon>> {
        return try {
            apiService.getNearbySaloonDetails(lat, lng)
        } catch (e: Exception) {
            throw e
        }
    }
}