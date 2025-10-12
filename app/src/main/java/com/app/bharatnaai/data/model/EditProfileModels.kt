package com.app.bharatnaai.data.model

data class EditProfileState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val isFormValid: Boolean = false,
    
    // Field-specific errors
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val passwordError: String? = null,
    
    // Track which fields have been touched/focused
    val fullNameTouched: Boolean = false,
    val emailTouched: Boolean = false,
    val phoneTouched: Boolean = false,
    val passwordTouched: Boolean = false
)

data class UserProfile(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val profileImageUrl: String? = null
)

data class UpdateProfileRequest(
    val name: String,
    val email: String,
    val phone: String,
    val password: String? = null
)

data class UpdateProfileResponse(
    val success: Boolean,
    val message: String,
    val user: UserProfile? = null
)
