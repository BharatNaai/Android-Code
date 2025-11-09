package com.app.bharatnaai.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.app.bharatnaai.ui.main.MainActivity
import com.app.bharatnaai.utils.NotificationStorage
import com.app.bharatnaai.utils.PreferenceManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.atomic.AtomicInteger

class BNFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        PreferenceManager.saveFcmToken(applicationContext, token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"]
        val body = remoteMessage.notification?.body ?: remoteMessage.data["message"]
        val type = remoteMessage.data["type"]
        val actionData = remoteMessage.data["actionData"]

        val item = NotificationStorage.fromPushPayload(title, body, type, actionData)
        NotificationStorage.addNotification(applicationContext, item)

        showSystemNotification(title ?: "Notification", body ?: "", actionData, type)
    }

    private fun showSystemNotification(title: String, message: String, actionData: String?, type: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            putExtra("notification_action_data", actionData)
            putExtra("notification_type", type)
        }

        val requestCode = notificationIdGenerator.getAndIncrement()
        val pendingIntent = PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val channelId = CHANNEL_GENERAL
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(bharatnaai.R.drawable.ic_bell_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(requestCode, notificationBuilder.build())
    }

    companion object {
        const val CHANNEL_GENERAL = "bn_channel_general"
        private val notificationIdGenerator = AtomicInteger(1000)
    }
}
