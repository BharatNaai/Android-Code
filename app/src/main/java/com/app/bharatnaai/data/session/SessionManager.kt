package com.app.bharatnaai.data.session

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import org.json.JSONObject
import android.util.Base64


class SessionManager private constructor(private val context: Context) {
    
    companion object {
        @Volatile
        private var INSTANCE: SessionManager? = null
        
        private const val PREF_NAME = "bharatnaai_session"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        
        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SessionManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val sharedPreferences: SharedPreferences by lazy {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM) // 🔑 Strong encryption key
            .build()

        EncryptedSharedPreferences.create(
            context,
            PREF_NAME, // The name of your shared prefs file
            masterKey, // The master encryption key
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV, // Key encryption method
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM // Value encryption method
        )
    }


    /**
     * Save authentication tokens after successful login
     */
    fun saveTokens(accessToken: String, refreshToken: String) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    
    /**
     * Get stored access token
     */
    fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }
    
    /**
     * Get stored refresh token
     */
    fun getRefreshToken(): String? {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
    }
    
    /**
     * Update access token after refresh
     */
    fun updateAccessToken(newAccessToken: String) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, newAccessToken)
            apply()
        }
    }
    
    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false) && 
               getAccessToken() != null && 
               getRefreshToken() != null
    }
    
    /**
     * Check if access token is expired or about to expire (within 5 minutes)
     */
    fun isAccessTokenExpired(): Boolean {
        val accessToken = getAccessToken() ?: return true
        
        return try {
            val payload = decodeJWTPayload(accessToken)
            val exp = payload.optLong("exp", 0)
            val currentTime = System.currentTimeMillis() / 1000
            val bufferTime = 5 * 60 // 5 minutes buffer
            
            exp <= (currentTime + bufferTime)
        } catch (e: Exception) {
            true // If we can't decode, consider it expired
        }
    }
    
    /**
     * Clear all session data (logout)
     */
    fun clearSession() {
        sharedPreferences.edit().clear().apply()
    }
    
    /**
     * Decode JWT payload to extract expiration time
     */
    private fun decodeJWTPayload(jwt: String): JSONObject {
        val parts = jwt.split(".")
        if (parts.size != 3) {
            throw IllegalArgumentException("Invalid JWT format")
        }

        val payload = parts[1]

        // Decode using Android Base64 with URL_SAFE flag
        val decodedBytes = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
        val decodedString = String(decodedBytes, charset("UTF-8"))

        return JSONObject(decodedString)
    }
    
    /**
     * Get user info from access token (if needed)
     */
    fun getUserEmailFromToken(): String? {
        val accessToken = getAccessToken() ?: return null
        
        return try {
            val payload = decodeJWTPayload(accessToken)
            payload.optString("sub", null) // 'sub' typically contains user identifier
        } catch (e: Exception) {
            null
        }
    }
}
