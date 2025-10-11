package com.app.bharatnaai.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class CommonMethod {

     fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                ?: return false

        val network = connectivityManager.activeNetwork ?: return false

        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    }

    fun getValidText(value: String?): String {
        return value?.trim() ?: ""
    }

    fun checkEmptyValue(value: String?): Boolean {
        return value?.trim()?.isEmpty() ?: true
    }
}