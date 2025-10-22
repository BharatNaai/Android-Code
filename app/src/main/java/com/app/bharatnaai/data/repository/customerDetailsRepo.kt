package com.app.bharatnaai.data.repository

import android.content.Context
import com.app.bharatnaai.data.model.CustomerDetails
import com.app.bharatnaai.data.network.ApiClient
import com.app.bharatnaai.data.session.SessionManager
import com.app.bharatnaai.utils.CommonMethod

class CustomerDetailsRepo(private val context: Context) {
    private val apiService = ApiClient.apiService
    private val sessionManager = SessionManager.getInstance(context)
    private val commonMethod = CommonMethod()

    suspend fun getCustomerDetails(): ApiResult<CustomerDetails> {
        // Check internet
        if (!commonMethod.isInternetAvailable(context)) {
            return ApiResult.Error("No internet connection")
        }

        // Retrieve token from session
        val token = sessionManager.getAccessToken()
        if (token.isNullOrEmpty()) {
            return ApiResult.Error("Not logged in")
        }

        return try {
            // API call with bearer token
            val response = apiService.getCustomerDetails("Bearer $token")

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    val customerDetails = CustomerDetails(
                        email = body.email,
                        fullName = body.fullName,
                        phone = body.phone,
                        userId = body.userId
                    )
                    ApiResult.Success(customerDetails)
                } else {
                    ApiResult.Error("Customer details not found in response")
                }
            } else {
                ApiResult.Error("Failed to fetch customer details: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "An unknown error occurred while fetching customer details")
        }
    }
}