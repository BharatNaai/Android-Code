package com.app.bharatnaai.ui.profile

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.bharatnaai.data.model.EditProfileState
import com.app.bharatnaai.data.model.UserProfile
import com.app.bharatnaai.utils.PreferenceManager
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class EditProfileViewModel : ViewModel() {

    private val _editProfileState = MutableLiveData<EditProfileState>()
    val editProfileState: LiveData<EditProfileState> = _editProfileState

    private val _userProfile = MutableLiveData<UserProfile>()
    val userProfile: LiveData<UserProfile> = _userProfile

    private val _profileImageUri = MutableLiveData<String?>()
    val profileImageUri: LiveData<String?> = _profileImageUri
    
    // Form data
    private var fullName: String = ""
    private var email: String = ""
    private var phoneNumber: String = ""
    private var password: String = ""

    init {
        _editProfileState.value = EditProfileState()
    }

    fun loadUserProfile(context: Context) {
        viewModelScope.launch {
            val name = PreferenceManager.getUserName(context) ?: ""
            val email = PreferenceManager.getUserEmail(context) ?: ""
            val phone = PreferenceManager.getUserPhone(context) ?: ""
            
            val profile = UserProfile(
                name = name,
                email = email,
                phone = phone
            )
            
            _userProfile.value = profile
        }
    }

    fun updateFormData(fullName: String, email: String, phone: String, password: String) {
        this.fullName = fullName
        this.email = email
        this.phoneNumber = phone
        this.password = password
    }
    
    fun markFieldAsTouched(fieldName: String) {
        val currentState = _editProfileState.value ?: EditProfileState()
        _editProfileState.value = when (fieldName) {
            "fullName" -> currentState.copy(fullNameTouched = true)
            "email" -> currentState.copy(emailTouched = true)
            "phone" -> currentState.copy(phoneTouched = true)
            "password" -> currentState.copy(passwordTouched = true)
            else -> currentState
        }
    }

    fun validateForm(): Boolean {
        val currentState = _editProfileState.value ?: EditProfileState()
        
        var isValid = true
        var fullNameError: String? = null
        var emailError: String? = null
        var phoneError: String? = null
        var passwordError: String? = null
        
        // Validate full name (only show error if field has been touched)
        if (fullName.isBlank()) {
            if (currentState.fullNameTouched) {
                fullNameError = "Full name is required"
            }
            isValid = false
        } else if (fullName.length < 2) {
            if (currentState.fullNameTouched) {
                fullNameError = "Name must be at least 2 characters"
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
        
        // Validate password (only show error if field has been touched and not empty)
        if (password.isNotEmpty()) {
            if (password.length < 6) {
                if (currentState.passwordTouched) {
                    passwordError = "Password must be at least 6 characters"
                }
                isValid = false
            }
        }
        
        _editProfileState.value = currentState.copy(
            nameError = fullNameError,
            emailError = emailError,
            phoneError = phoneError,
            passwordError = passwordError,
            isFormValid = isValid
        )
        
        return isValid
    }
    
    fun validateFormForSubmission(): Boolean {
        // Mark all fields as touched and validate
        val currentState = _editProfileState.value ?: EditProfileState()
        _editProfileState.value = currentState.copy(
            fullNameTouched = true,
            emailTouched = true,
            phoneTouched = true,
            passwordTouched = true
        )
        return validateForm()
    }
    
    fun saveProfile(context: Context) {
        if (!validateFormForSubmission()) return
        
        viewModelScope.launch {
            _editProfileState.value = _editProfileState.value?.copy(isLoading = true, error = null)
            
            try {
                // Simulate API call delay
                kotlinx.coroutines.delay(1000)
                
                // Save to preferences (in real app, this would be an API call)
                PreferenceManager.saveUserName(context, fullName)
                PreferenceManager.saveUserEmail(context, email)
                PreferenceManager.saveUserPhone(context, phoneNumber)
                
                // Update local profile
                _userProfile.value = _userProfile.value?.copy(
                    name = fullName,
                    email = email,
                    phone = phoneNumber
                )
                
                _editProfileState.value = _editProfileState.value?.copy(
                    isLoading = false,
                    isSuccess = true
                )
                
            } catch (e: Exception) {
                _editProfileState.value = _editProfileState.value?.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update profile"
                )
            }
        }
    }

    fun updateProfileImage(imageUri: String) {
        _profileImageUri.value = imageUri
    }

    fun clearError() {
        _editProfileState.value = _editProfileState.value?.copy(error = null)
    }

    fun clearSuccess() {
        _editProfileState.value = _editProfileState.value?.copy(isSuccess = false)
    }

}
