package com.app.bharatnaai

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.app.bharatnaai.data.network.ApiClient

class BharatNaaiApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize ApiClient with context for AuthInterceptor
        ApiClient.initialize(this)
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)
            val general = NotificationChannel(
                com.app.bharatnaai.services.BNFirebaseMessagingService.CHANNEL_GENERAL,
                "General",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(general)
        }
    }
}
