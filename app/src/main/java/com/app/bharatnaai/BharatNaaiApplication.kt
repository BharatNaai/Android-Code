package com.app.bharatnaai

import android.app.Activity
import android.app.Application
import com.app.bharatnaai.services.BNFirebaseMessagingService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import com.app.bharatnaai.data.network.ApiClient

class BharatNaaiApplication : Application(),
    Application.ActivityLifecycleCallbacks {

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
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityResumed(activity: Activity) {
        BNFirebaseMessagingService.AppState.isInForeground = true
    }

    override fun onActivityPaused(activity: Activity) {
        BNFirebaseMessagingService.AppState.isInForeground = false
    }

    // Unused but REQUIRED overrides
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}

