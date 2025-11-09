package com.app.bharatnaai.utils

import android.content.Context
import android.content.SharedPreferences
import com.app.bharatnaai.data.model.NotificationItem
import com.app.bharatnaai.data.model.NotificationType
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date
import java.util.UUID

object NotificationStorage {
    private const val PREFS_NAME = "bn_notifications_prefs"
    private const val KEY_LIST = "notifications_list"

    private val gson = Gson()

    private fun prefs(ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getNotifications(ctx: Context): List<NotificationItem> {
        val json = prefs(ctx).getString(KEY_LIST, null) ?: return emptyList()
        val type = object : TypeToken<List<NotificationItem>>() {}.type
        return runCatching { gson.fromJson<List<NotificationItem>>(json, type) }.getOrElse { emptyList() }
    }

    fun saveNotifications(ctx: Context, list: List<NotificationItem>) {
        val json = gson.toJson(list)
        prefs(ctx).edit().putString(KEY_LIST, json).apply()
    }

    fun addNotification(ctx: Context, item: NotificationItem) {
        val current = getNotifications(ctx).toMutableList()
        current.add(0, item)
        saveNotifications(ctx, current)
    }

    fun markAsRead(ctx: Context, id: String) {
        val updated = getNotifications(ctx).map { if (it.id == id) it.copy(isRead = true) else it }
        saveNotifications(ctx, updated)
    }

    // Helper to build NotificationItem from raw push payload
    fun fromPushPayload(
        title: String?,
        message: String?,
        type: String?,
        actionData: String?,
        timestampMillis: Long? = null
    ): NotificationItem {
        val mappedType = when (type?.uppercase()) {
            "APPOINTMENT_CONFIRMED" -> NotificationType.APPOINTMENT_CONFIRMED
            "APPOINTMENT_REMINDER" -> NotificationType.APPOINTMENT_REMINDER
            "APPOINTMENT_UPDATED" -> NotificationType.APPOINTMENT_UPDATED
            "SPECIAL_OFFER" -> NotificationType.SPECIAL_OFFER
            else -> NotificationType.GENERAL
        }
        return NotificationItem(
            id = UUID.randomUUID().toString(),
            type = mappedType,
            title = title ?: "Notification",
            message = message ?: "",
            timestamp = Date(timestampMillis ?: System.currentTimeMillis()),
            iconResource = bharatnaai.R.drawable.ic_bell_notification,
            actionData = actionData
        )
    }
}
