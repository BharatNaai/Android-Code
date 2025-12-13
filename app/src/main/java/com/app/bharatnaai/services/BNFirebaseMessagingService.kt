package com.app.bharatnaai.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import bharatnaai.R
import com.app.bharatnaai.data.model.NotificationItem
import com.app.bharatnaai.data.model.NotificationType
import com.app.bharatnaai.data.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.Date
import java.util.Random

class BNFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // send token to backend if needed
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.data["title"]
            ?: message.notification?.title
            ?: getString(R.string.app_name)

        val body = message.data["message"]
            ?: message.notification?.body
            ?: ""

        val typeStr = message.data["type"] ?: "GENERAL"
        val type = runCatching { NotificationType.valueOf(typeStr) }
            .getOrDefault(NotificationType.GENERAL)

        showSystemNotification(title, body)

        val repo = NotificationRepository(applicationContext)
        val item = NotificationItem(
            id = System.currentTimeMillis().toString(),
            type = type,
            title = title,
            message = body,
            timestamp = Date(),
            isRead = false,
            iconResource = R.drawable.ic_bell_notification,
            actionData = null
        )
        repo.addInAppNotification(item)
    }

    private fun showSystemNotification(title: String, body: String) {
        val channelId = "bn_default_channel"
        createChannelIfNeeded(channelId)

        // Permission is required only for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionCheck = ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )

            // If permission is not granted, skip showing notification
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_bell_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(), builder.build())
    }

    private fun createChannelIfNeeded(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.app_name)
            val descriptionText = "General notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH

            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = Color.YELLOW
                enableVibration(true)
            }

            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
