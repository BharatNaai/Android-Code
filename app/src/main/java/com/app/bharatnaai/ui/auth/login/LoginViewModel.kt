package com.app.bharatnaai.ui.auth.login

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.bharatnaai.data.repository.ApiResult
import com.app.bharatnaai.data.repository.AuthRepository
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authRepository = AuthRepository(application.applicationContext)

    // Form data
    private var emailPhone: String = ""
    private var password: String = ""
    private val _loginState = MutableLiveData<LoginState>()
    val loginState: LiveData<LoginState> = _loginState
    
    init {
        _loginState.value = LoginState()
    }
    
    fun updateFormData(
        emailPhone: String,
        password: String
    ) {
        this.emailPhone = emailPhone
        this.password = password
    }
    
    fun markFieldAsTouched(fieldName: String) {
        val currentState = _loginState.value ?: LoginState()
        _loginState.value = when (fieldName) {
            "emailPhone" -> currentState.copy(emailPhoneTouched = true)
            "password" -> currentState.copy(passwordTouched = true)
            else -> currentState
        }
    }
    
    fun validateForm(): Boolean {
        val currentState = _loginState.value ?: LoginState()
        
        var isValid = true
        var emailPhoneError: String? = null
        var passwordError: String? = null
        
        // Validate email/phone (only show error if field has been touched)
        if (emailPhone.isBlank()) {
            if (currentState.emailPhoneTouched) {
                emailPhoneError = "Email or phone is required"
            }
            isValid = false
        } else if (!isValidEmailOrPhone(emailPhone)) {
            if (currentState.emailPhoneTouched) {
                emailPhoneError = "Invalid email or phone format"
            }
            isValid = false
        }
        
        // Validate password (only show error if field has been touched)
        if (password.isBlank()) {
            if (currentState.passwordTouched) {
                passwordError = "Password is required"
            }
            isValid = false
        } else if (password.length < 6) {
            if (currentState.passwordTouched) {
                passwordError = "Password must be at least 6 characters"
            }
            isValid = false
        }
        
        _loginState.value = currentState.copy(
            emailPhoneError = emailPhoneError,
            passwordError = passwordError,
            isFormValid = isValid
        )
        
        return isValid
    }
    
    fun validateFormForSubmission(): Boolean {
        // Mark all fields as touched and validate
        val currentState = _loginState.value ?: LoginState()
        _loginState.value = currentState.copy(
            emailPhoneTouched = true,
            passwordTouched = true
        )
        return validateForm()
    }
    
    fun login() {
        if (!validateFormForSubmission()) return

        viewModelScope.launch {
            _loginState.value = _loginState.value?.copy(isLoading = true)

            when (val result = authRepository.loginUser(emailPhone, password)) {
                is ApiResult.Success -> {
                    _loginState.value = _loginState.value?.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
                is ApiResult.Error -> {
                    _loginState.value = _loginState.value?.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {
                }
            }
        }

    }
    
    private fun isValidEmailOrPhone(input: String): Boolean {
        // Check if it's a valid email
        val emailPattern = android.util.Patterns.EMAIL_ADDRESS
        if (emailPattern.matcher(input).matches()) {
            return true
        }
        
        // Check if it's a valid phone number (simple check for digits and length)
        val phonePattern = "^[+]?[0-9]{10,15}$".toRegex()
        return phonePattern.matches(input.replace("\\s".toRegex(), ""))
    }
    
    fun clearErrors() {
        _loginState.value = _loginState.value?.copy(
            emailPhoneError = null,
            passwordError = null,
            error = null
        )
    }
}
