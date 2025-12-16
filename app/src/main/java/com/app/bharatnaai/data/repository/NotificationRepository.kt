package com.app.bharatnaai.data.repository

import android.content.Context
import com.app.bharatnaai.data.model.notificationRequest
import com.app.bharatnaai.data.model.notificationResponse
import com.app.bharatnaai.data.model.NotificationItem
import com.app.bharatnaai.data.network.ApiClient
import com.app.bharatnaai.data.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.Date

class NotificationRepository(
    private val context: Context,
    private val api: ApiService = ApiClient.apiService
) {
    suspend fun sendPushNotification(fcmToken: String, message: String): Response<notificationResponse> =
        withContext(Dispatchers.IO) {
            api.getFirebaseNotification(notificationRequest(fcmToken, message))
        }
}
