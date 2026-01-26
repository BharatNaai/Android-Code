package com.app.bharatnaai.ui.splash

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import bharatnaai.databinding.ActivitySplashBinding
import com.app.bharatnaai.ui.main.MainActivity
import com.app.bharatnaai.utils.CommonMethod

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var connectivityManager: ConnectivityManager
    private val handler = Handler(Looper.getMainLooper())
    private var hasNavigated = false
    private val commonMethod = CommonMethod()
    private var noInternetDialog: AlertDialog ?= null
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()
        binding.progressBar.alpha = 0.5f

        connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        observeNetwork()
        checkInternetAndProceed()
    }

    // ---------------- INTERNET CHECK ----------------
    private fun checkInternetAndProceed() {
        if (commonMethod.isInternetAvailable(this)) {
            proceedToMainWithDelay()
        } else {
            showNoInternetDialog()
        }
    }

    private fun showNoInternetDialog() {
        if (noInternetDialog?.isShowing == true) return
        noInternetDialog = commonMethod.noInternetDialog(this@SplashActivity)
        noInternetDialog?.show()
    }

    // ---------------- NETWORK OBSERVER ----------------
    private fun observeNetwork() {
        val request = NetworkRequest.Builder().build()
        
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                runOnUiThread {
                    if (noInternetDialog?.isShowing == true) {
                        noInternetDialog?.dismiss()
                    }
                    proceedToMainWithDelay()
                }
            }

            override fun onLost(network: Network) {
                runOnUiThread {
                    showNoInternetDialog()
                }
            }
        }

        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    // ---------------- NAVIGATION ----------------
    private fun proceedToMainWithDelay() {
        if (hasNavigated) return
        // Double check internet before navigating if called slightly prematurely or in race condition
        if (!commonMethod.isInternetAvailable(this)) return 

        hasNavigated = true
        handler.postDelayed({ navigateToMain() }, SPLASH_DELAY)
    }
    
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        try {
            if (::networkCallback.isInitialized) {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            }
        } catch (e: Exception) {
            // safe ignore
        }
        if (noInternetDialog?.isShowing == true) {
            noInternetDialog?.dismiss()
        }
    }

    private companion object {
        const val SPLASH_DELAY = 3000L // 3 seconds
    }
}
