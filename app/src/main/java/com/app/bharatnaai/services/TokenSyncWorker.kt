package com.app.bharatnaai.services

import android.content.Context
import android.provider.Settings
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.app.bharatnaai.data.repository.ApiResult
import com.app.bharatnaai.data.repository.AuthRepository
import com.app.bharatnaai.utils.PreferenceManager

class TokenSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val ctx = applicationContext
        val token = inputData.getString("token") ?: PreferenceManager.getToken(ctx)
        if (token.isNullOrBlank()) {
            return Result.success() // nothing to sync
        }

        val rawId = Settings.Secure.getString(ctx.contentResolver, Settings.Secure.ANDROID_ID)
        val deviceId = if (rawId.isNullOrBlank()) "UNKNOWN_DEVICE" else "ANDROID_" + rawId

        return try {
            val repo = AuthRepository(ctx)
            val result = repo.registerDevice(deviceId, token)
            return if (result is ApiResult.Success) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            // Non-fatal: you could choose retry if needed
            Result.failure()
        }
    }
}
