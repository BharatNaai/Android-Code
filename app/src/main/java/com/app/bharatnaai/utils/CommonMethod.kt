package com.app.bharatnaai.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.widget.AppCompatImageView
import bharatnaai.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

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

    fun loadImage(imageView: AppCompatImageView, imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.ic_profile)
            return
        }

        Glide.with(imageView.context)
            .load(imageUrl)
            .transform(CenterCrop())
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(imageView)
    }
}