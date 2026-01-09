package com.app.bharatnaai.data.repository

import android.content.Context
import com.app.bharatnaai.data.model.notificationRequest
import com.app.bharatnaai.data.network.ApiClient
import com.app.bharatnaai.data.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response

class NotificationRepository(
    private val context: Context,
    private val api: ApiService = ApiClient.apiService
) {
    suspend fun sendPushNotification(fcmToken: String, message: String): Response<ResponseBody> =
        withContext(Dispatchers.IO) {
            api.getFirebaseNotification(notificationRequest(fcmToken, message))
        }
}
