package com.app.bharatnaai.data.repository

import android.content.Context
import com.app.bharatnaai.data.network.ApiClient
import com.app.bharatnaai.data.model.Salon
import retrofit2.Response
import com.app.bharatnaai.utils.CommonMethod

class NearbySaloonRepository(private val context: Context) {
    private val apiService = ApiClient.apiService
    private val commonMethod = CommonMethod()

    suspend fun getNearbySaloonDetails(lat : Double, lng: Double): Response<List<Salon>> {
        if (!commonMethod.isInternetAvailable(context)) {
            throw Exception("No internet connection")
        }
        return apiService.getNearbySaloonDetails(lat, lng)
    }
}