package com.app.bharatnaai.utils

import android.content.Context
import android.content.SharedPreferences
object PreferenceManager {

    private const val PREF_NAME = "BharatNaaiPrefs"
    private const val USER_NAME = "user_name"
    private const val USER_EMAIL = "user_email"
    private const val USER_PHONE = "user_phone"
    private const val USER_ID = "user_id"
    private const val PREFS_NAME = "bn_prefs"
    private const val KEY_FCM_TOKEN = "fcm_token"

    fun saveToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_FCM_TOKEN, null)
    }

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserName(context: Context, name: String) {
        getPreferences(context).edit().putString(USER_NAME, name).apply()
    }

    fun getUserName(context: Context): String? {
        return getPreferences(context).getString(USER_NAME, null)
    }

    fun saveUserEmail(context: Context, email: String) {
        getPreferences(context).edit().putString(USER_EMAIL, email).apply()
    }

    fun getUserEmail(context: Context): String? {
        return getPreferences(context).getString(USER_EMAIL, null)
    }

    fun saveUserPhone(context: Context, phone: String) {
        getPreferences(context).edit().putString(USER_PHONE, phone).apply()
    }

    fun getUserPhone(context: Context): String? {
        return getPreferences(context).getString(USER_PHONE, null)
    }

    fun saveUserId(context: Context, userId: Long) {
        getPreferences(context).edit().putLong(USER_ID, userId).apply()
    }

    fun getUserId(context: Context): Long =
        getPreferences(context).getLong(USER_ID, 0)

    fun clearAll(context: Context) {
        getPreferences(context).edit().clear().apply()
    }
}
