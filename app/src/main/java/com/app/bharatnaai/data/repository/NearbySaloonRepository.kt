package com.app.bharatnaai.data.repository

import com.app.bharatnaai.data.model.SaloonDetailsRequest
import com.app.bharatnaai.data.model.SaloonDetailsResponse
import com.app.bharatnaai.data.network.ApiClient
import retrofit2.Response

class NearbySaloonRepository {
    private val apiService = ApiClient.apiService

    suspend fun getNearbySaloonDetails(request: SaloonDetailsRequest): Response<SaloonDetailsResponse> {
        return try {
            apiService.getNearbySaloonDetails(request)
        } catch (e: Exception) {
            throw e
        }
    }
}