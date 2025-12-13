package com.app.bharatnaai.utils

import android.content.Context
import android.content.SharedPreferences
import com.app.bharatnaai.data.model.NotificationItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

object NotificationStorage {

    private const val PREF_NAME = "BharatNaaiNotifications"
    private const val KEY_NOTIFICATIONS = "key_notifications"

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun saveNotifications(context: Context, list: List<NotificationItem>) {
        val json = Gson().toJson(list)
        prefs(context).edit().putString(KEY_NOTIFICATIONS, json).apply()
    }

    fun getNotifications(context: Context): List<NotificationItem> {
        val json = prefs(context).getString(KEY_NOTIFICATIONS, null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<NotificationItem>>() {}.type
            Gson().fromJson<List<NotificationItem>>(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addNotification(context: Context, item: NotificationItem) {
        val current = getNotifications(context).toMutableList()
        current.add(0, item)
        saveNotifications(context, current)
    }

    fun markAsRead(context: Context, id: String) {
        val updated = getNotifications(context).map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        saveNotifications(context, updated)
    }
}
