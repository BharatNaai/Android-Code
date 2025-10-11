package com.app.bharatnaai.ui.auth.register

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.bharatnaai.data.model.RegisterRequest
import com.app.bharatnaai.data.repository.ApiResult
import com.app.bharatnaai.data.repository.AuthRepository
import kotlinx.coroutines.launch

class RegisterViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authRepository = AuthRepository(application.applicationContext)

    // Form data
    private var fullName: String = ""
    private var email: String = ""
    private var phoneNumber: String = ""
    private var password: String = ""
    private var confirmPassword: String = ""
    
    // LiveData for UI state
    private val _registerState = MutableLiveData<RegisterState>()
    val registerState: LiveData<RegisterState> = _registerState
    
    init {
        _registerState.value = RegisterState()
    }
    
    fun updateFormData(fullName: String, email: String, phone: String, password: String, confirmPassword: String) {
        this.fullName = fullName
        this.email = email
        this.phoneNumber = phone
        this.password = password
        this.confirmPassword = confirmPassword
    }
    
    fun markFieldAsTouched(fieldName: String) {
        val currentState = _registerState.value ?: RegisterState()
        _registerState.value = when (fieldName) {
            "fullName" -> currentState.copy(fullNameTouched = true)
            "email" -> currentState.copy(emailTouched = true)
            "phone" -> currentState.copy(phoneTouched = true)
            "password" -> currentState.copy(passwordTouched = true)
            "confirmPassword" -> currentState.copy(confirmPasswordTouched = true)
            else -> currentState
        }
    }
    
    fun validateForm(): Boolean {
        val currentState = _registerState.value ?: RegisterState()
        
        var isValid = true
        var fullNameError: String? = null
        var emailError: String? = null
        var phoneError: String? = null
        var passwordError: String? = null
        var confirmPasswordError: String? = null
        
        // Validate full name (only show error if field has been touched)
        if (fullName.isBlank()) {
            if (currentState.fullNameTouched) {
                fullNameError = "Full name is required"
            }
            isValid = false
        }
        
        // Validate email (only show error if field has been touched)
        if (email.isBlank()) {
            if (currentState.emailTouched) {
                emailError = "Email is required"
            }
            isValid = false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (currentState.emailTouched) {
                emailError = "Invalid email format"
            }
            isValid = false
        }
        
        // Validate phone (only show error if field has been touched)
        if (phoneNumber.isBlank()) {
            if (currentState.phoneTouched) {
                phoneError = "Phone number is required"
            }
            isValid = false
        } else if (phoneNumber.length < 10) {
            if (currentState.phoneTouched) {
                phoneError = "Phone number must be at least 10 digits"
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
        
        // Validate confirm password (only show error if field has been touched)
        if (confirmPassword.isBlank()) {
            if (currentState.confirmPasswordTouched) {
                confirmPasswordError = "Please confirm your password"
            }
            isValid = false
        } else if (password != confirmPassword) {
            if (currentState.confirmPasswordTouched) {
                confirmPasswordError = "Passwords do not match"
            }
            isValid = false
        }
        
        _registerState.value = currentState.copy(
            fullNameError = fullNameError,
            emailError = emailError,
            phoneError = phoneError,
            passwordError = passwordError,
            confirmPasswordError = confirmPasswordError,
            isFormValid = isValid
        )
        
        return isValid
    }
    
    fun validateFormForSubmission(): Boolean {
        // Mark all fields as touched and validate
        val currentState = _registerState.value ?: RegisterState()
        _registerState.value = currentState.copy(
            fullNameTouched = true,
            emailTouched = true,
            phoneTouched = true,
            passwordTouched = true,
            confirmPasswordTouched = true
        )
        return validateForm()
    }
    
    fun register() {
        if (!validateFormForSubmission()) return
        
        viewModelScope.launch {
            _registerState.value = _registerState.value?.copy(isLoading = true, error = null)

            val user = RegisterRequest(username = fullName, email = email, phone = phoneNumber, password = password)
            
            when (val result = authRepository.registerUser(user)) {
                is ApiResult.Success -> {
                    _registerState.value = _registerState.value?.copy(
                        isLoading = false,
                        isSuccess = true
                    )
                }
                is ApiResult.Error -> {
                    _registerState.value = _registerState.value?.copy(
                        isLoading = false,
                        error = result.message
                    )
                }
                is ApiResult.Loading -> {
                    // This state is already handled, but you could add specific logic here if needed
                }
            }
        }
    }
}
