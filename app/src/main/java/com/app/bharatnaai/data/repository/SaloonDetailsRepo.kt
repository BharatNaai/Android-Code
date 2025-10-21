package com.app.bharatnaai.data.repository

import android.content.Context
import com.app.bharatnaai.data.model.Salon
import com.app.bharatnaai.data.network.ApiService
import com.app.bharatnaai.utils.CommonMethod

class SaloonDetailsRepo(
    private val context: Context,
    private val apiService: ApiService
) {
    private val commonMethod = CommonMethod()

    suspend fun fetchSalonDetails(salonId: Int): Salon? {
        if (!commonMethod.isInternetAvailable(context)) {
            throw Exception("No internet connection")
        }
        val response = apiService.getSalonDetails(salonId)
        return if (response.isSuccessful) response.body() else null
    }
}
