package com.app.bharatnaai.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

/**
 * Helper class to handle Biometric Authentication (Fingerprint/Face/PIN/Pattern/Password).
 * Handles hardware checks, enrollment status, and showing the biometric prompt.
 */
class BiometricHelper(private val activity: FragmentActivity) {

    private val executor: Executor = ContextCompat.getMainExecutor(activity)
    private val biometricManager = BiometricManager.from(activity)

    /**
     * Callback interface for Biometric authentication results.
     */
    interface BiometricAuthCallback {
        fun onAuthSuccess(result: BiometricPrompt.AuthenticationResult)
        fun onAuthError(errorCode: Int, errString: CharSequence)
        fun onAuthFailure()
        fun onBiometricNotAvailable(message: String)
        fun onBiometricNotEnrolled()
    }

    /**
     * Checks if biometric authentication is possible and allowed on the device.
     */
    fun checkBiometricAvailability(): Int {
        return biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
    }

    /**
     * Main method to initiate Biometric Authentication.
     */
    fun showBiometricPrompt(
        title: String = "Biometric Login",
        subtitle: String = "Log in using your biometric credential",
        description: String = "Please authenticate to proceed",
        callback: BiometricAuthCallback
    ) {
        val canAuthenticate = checkBiometricAvailability()

        when (canAuthenticate) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                setupBiometricPrompt(title, subtitle, description, callback)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                callback.onBiometricNotAvailable("Biometric features are not available on this device.")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                callback.onBiometricNotAvailable("Biometric features are currently unavailable.")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                callback.onBiometricNotEnrolled()
            }
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> {
                callback.onBiometricNotAvailable("A security update is required to use biometric features.")
            }
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> {
                callback.onBiometricNotAvailable("Biometric authentication is not supported on this device.")
            }
            else -> {
                callback.onBiometricNotAvailable("Biometric authentication is not possible at this time.")
            }
        }
    }

    private fun setupBiometricPrompt(
        title: String,
        subtitle: String,
        description: String,
        callback: BiometricAuthCallback
    ) {
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    callback.onAuthError(errorCode, errString)
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    callback.onAuthSuccess(result)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    callback.onAuthFailure()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)
            // If DEVICE_CREDENTIAL is included, setNegativeButtonText MUST NOT be called.
            // If you want only biometrics (no PIN/Pattern fallback), remove DEVICE_CREDENTIAL 
            // and call setNegativeButtonText("Cancel").
            .setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    /**
     * Utility to redirect user to biometric enrollment settings.
     */
    fun openBiometricSettings() {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
            }
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }
        activity.startActivity(intent)
    }
}
