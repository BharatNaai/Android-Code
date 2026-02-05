package com.app.bharatnaai

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.app.bharatnaai.data.network.ApiClient
import com.app.bharatnaai.services.AppState

class BharatNaaiApplication : Application(),
    Application.ActivityLifecycleCallbacks {

    override fun onCreate() {
        super.onCreate()

        // Initialize ApiClient with context for AuthInterceptor
        ApiClient.initialize(this)
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityResumed(activity: Activity) {
        AppState.isInForeground = true
    }

    override fun onActivityPaused(activity: Activity) {
        AppState.isInForeground = false
    }

    // Unused but REQUIRED overrides
    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
    override fun onActivityStarted(activity: Activity) {}
    override fun onActivityStopped(activity: Activity) {}
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
    override fun onActivityDestroyed(activity: Activity) {}
}

