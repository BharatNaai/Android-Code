package com.app.bharatnaai.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.app.bharatnaai.utils.PreferenceManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.atomic.AtomicInteger
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import bharatnaai.R
import com.app.bharatnaai.data.model.NotificationItem
import com.app.bharatnaai.data.model.NotificationType
import com.app.bharatnaai.ui.home.HomeFragment
import java.util.Date
import kotlin.random.Random

class BNFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        PreferenceManager.saveFcmToken(applicationContext, token)
    }

        Log.d("FCM_TEST", "New token: $token")
        PreferenceManager.saveToken(applicationContext, token)
        // Trigger registration via WorkManager or EventBus since service can't access ViewModel
        WorkManager.getInstance(applicationContext).enqueue(OneTimeWorkRequestBuilder<TokenSyncWorker>()
            .setInputData(workDataOf("token" to token)).build())

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title = message.data["title"] ?: message.notification?.title ?: getString(R.string.app_name)
        val body = message.data["message"] ?: message.notification?.body ?: ""

        val typeStr = message.data["type"] ?: "GENERAL"
        val type = runCatching { NotificationType.valueOf(typeStr) }
            .getOrDefault(NotificationType.GENERAL)

        if (!AppState.isInForeground) {
            showSystemNotification(title, body, typeStr)
        }
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
    }

    private fun showSystemNotification(title: String, body: String, typeStr: String) {
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

        // 🔹 Intent to open app when notification is tapped
        val intent = Intent(this, HomeFragment::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("type", typeStr)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_bell_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

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

    companion object {
        const val CHANNEL_GENERAL = "bn_channel_general"
        private val notificationIdGenerator = AtomicInteger(1000)
    }
    object AppState {
        var isInForeground = false
    }
}
