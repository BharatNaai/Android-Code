package com.app.bharatnaai.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.app.bharatnaai.ui.main.MainActivity
import bharatnaai.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySplashBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Hide action bar for full screen experience
        supportActionBar?.hide()
        
        // Subtle loading effect
        binding.progressBar.alpha = 0.5f
        
        // Always navigate to Main to allow exploration.
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToMain()
        }, SPLASH_DELAY)
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private companion object {
        const val SPLASH_DELAY = 2000L // 3 seconds
    }
}
