package com.app.bharatnaai

import android.app.Application
import com.app.bharatnaai.data.network.ApiClient

class BharatNaaiApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // Initialize ApiClient with context for AuthInterceptor
        ApiClient.initialize(this)
    }
}
