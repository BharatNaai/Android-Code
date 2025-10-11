package com.app.bharatnaai.ui.auth.forgotpassword

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.bharatnaai.data.repository.ApiResult
import com.app.bharatnaai.data.repository.AuthRepository
import kotlinx.coroutines.launch

enum class ResetMethod {
    SMS, EMAIL
}

enum class ForgotPasswordStep {
    SELECT_METHOD,
    VERIFY_OTP,
    RESET_COMPLETE
}

data class ForgotPasswordState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val selectedMethod: ResetMethod = ResetMethod.EMAIL,
    val currentStep: ForgotPasswordStep = ForgotPasswordStep.SELECT_METHOD,
    val email: String = "",
    
    // Field-specific errors
    val otpError: String? = null,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    
    // Track which fields have been touched
    val otpTouched: Boolean = false,
    val newPasswordTouched: Boolean = false,
    val confirmPasswordTouched: Boolean = false
)

class ForgotPasswordViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authRepository = AuthRepository(application.applicationContext)
    
    // LiveData for UI state
    private val _forgotPasswordState = MutableLiveData<ForgotPasswordState>()
    val forgotPasswordState: LiveData<ForgotPasswordState> = _forgotPasswordState
    
    // Form data
    private var otp: String = ""
    private var newPassword: String = ""
    private var confirmPassword: String = ""
    
    init {
        _forgotPasswordState.value = ForgotPasswordState()
    }
    
    fun setSelectedMethod(method: ResetMethod) {
        _forgotPasswordState.value = _forgotPasswordState.value?.copy(
            selectedMethod = method
        )
    }
    
    fun updateFormData(otp: String = this.otp, newPassword: String = this.newPassword, confirmPassword: String = this.confirmPassword) {
        this.otp = otp
        this.newPassword = newPassword
        this.confirmPassword = confirmPassword
    }
    
    fun markFieldAsTouched(fieldName: String) {
        val currentState = _forgotPasswordState.value ?: ForgotPasswordState()
        _forgotPasswordState.value = when (fieldName) {
            "otp" -> currentState.copy(otpTouched = true)
            "newPassword" -> currentState.copy(newPasswordTouched = true)
            "confirmPassword" -> currentState.copy(confirmPasswordTouched = true)
            else -> currentState
        }
    }
    
    fun sendResetCode(method: ResetMethod, email: String) {
        viewModelScope.launch {
            _forgotPasswordState.value = _forgotPasswordState.value?.copy(
                isLoading = true,
                error = null
            )
            
            try {
                when (val result = authRepository.forgetPassword(email)) {
                    is ApiResult.Success -> {
                        _forgotPasswordState.value = _forgotPasswordState.value?.copy(
                            isLoading = false,
                            currentStep = ForgotPasswordStep.VERIFY_OTP,
                            selectedMethod = method,
                            email = email
                        )
                    }
                    is ApiResult.Error -> {
                        _forgotPasswordState.value = _forgotPasswordState.value?.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    is ApiResult.Loading -> {
                        // Handle loading state if needed
                    }
                }
            } catch (e: Exception) {
                _forgotPasswordState.value = _forgotPasswordState.value?.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to send reset code"
                )
            }
        }
    }
    
    fun validateOtpForm(): Boolean {
        val currentState = _forgotPasswordState.value ?: ForgotPasswordState()
        
        var isValid = true
        var otpError: String? = null
        var newPasswordError: String? = null
        var confirmPasswordError: String? = null
        
        // Validate OTP
        if (otp.isBlank()) {
            if (currentState.otpTouched) {
                otpError = "OTP is required"
            }
            isValid = false
        } else if (otp.length != 6) {
            if (currentState.otpTouched) {
                otpError = "OTP must be 6 digits"
            }
            isValid = false
        }
        
        // Validate new password
        if (newPassword.isBlank()) {
            if (currentState.newPasswordTouched) {
                newPasswordError = "New password is required"
            }
            isValid = false
        } else if (newPassword.length < 6) {
            if (currentState.newPasswordTouched) {
                newPasswordError = "Password must be at least 6 characters"
            }
            isValid = false
        }
        
        // Validate confirm password
        if (confirmPassword.isBlank()) {
            if (currentState.confirmPasswordTouched) {
                confirmPasswordError = "Please confirm your password"
            }
            isValid = false
        } else if (confirmPassword != newPassword) {
            if (currentState.confirmPasswordTouched) {
                confirmPasswordError = "Passwords do not match"
            }
            isValid = false
        }
        
        _forgotPasswordState.value = currentState.copy(
            otpError = otpError,
            newPasswordError = newPasswordError,
            confirmPasswordError = confirmPasswordError
        )
        
        return isValid
    }
    
    fun validateFormForSubmission(): Boolean {
        // Mark all fields as touched and validate
        val currentState = _forgotPasswordState.value ?: ForgotPasswordState()
        _forgotPasswordState.value = currentState.copy(
            otpTouched = true,
            newPasswordTouched = true,
            confirmPasswordTouched = true
        )
        return validateOtpForm()
    }
    
    fun resetPassword() {
        if (!validateFormForSubmission()) return
        
        viewModelScope.launch {
            _forgotPasswordState.value = _forgotPasswordState.value?.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val currentState = _forgotPasswordState.value!!
                
                when (val result = authRepository.resetPassword(currentState.email, otp, newPassword)) {
                    is ApiResult.Success -> {
                        _forgotPasswordState.value = _forgotPasswordState.value?.copy(
                            isLoading = false,
                            isSuccess = true,
                            currentStep = ForgotPasswordStep.RESET_COMPLETE
                        )
                    }
                    is ApiResult.Error -> {
                        _forgotPasswordState.value = _forgotPasswordState.value?.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                    is ApiResult.Loading -> {
                        // Handle loading state if needed
                    }
                }
            } catch (e: Exception) {
                _forgotPasswordState.value = _forgotPasswordState.value?.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to reset password"
                )
            }
        }
    }
    
    fun clearError() {
        _forgotPasswordState.value = _forgotPasswordState.value?.copy(error = null)
    }
}
