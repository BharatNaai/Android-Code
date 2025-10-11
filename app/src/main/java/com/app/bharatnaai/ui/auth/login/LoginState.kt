package com.app.bharatnaai.ui.auth.login

data class LoginState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val isFormValid: Boolean = false,
    
    // Field-specific errors
    val emailPhoneError: String? = null,
    val passwordError: String? = null,
    
    // Track which fields have been touched/focused
    val emailPhoneTouched: Boolean = false,
    val passwordTouched: Boolean = false
)
