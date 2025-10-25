package com.app.bharatnaai.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.bharatnaai.data.model.CustomerDetails
import com.app.bharatnaai.data.repository.AuthRepository
import com.app.bharatnaai.utils.PreferenceManager


data class ProfileState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val customerDetails: CustomerDetails? = null
)

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application.applicationContext)

    private val _profileState = MutableLiveData<ProfileState>()
    val profileState: LiveData<ProfileState> = _profileState

    init {
        // Check actual authentication status
        val isLoggedIn = authRepository.isLoggedIn()
        _profileState.value = ProfileState(isLoggedIn = isLoggedIn)

        if (isLoggedIn) {
            loadUserProfile()
        }
    }

    fun loadUserProfile() {
        val context = getApplication<Application>().applicationContext

        // Mock user profile data
        val profileData = CustomerDetails(
            fullName = PreferenceManager.getUserName(context) ?: "",
            phone = PreferenceManager.getUserPhone(context) ?: "",
            email = PreferenceManager.getUserEmail(context) ?: "",
            userId = PreferenceManager.getUserId(context) ?: 0
        )

        _profileState.value = _profileState.value?.copy(
            customerDetails = profileData
        )
    }

    fun logout() {
        // Clear authentication data
        authRepository.logout()

        // Update UI state
        _profileState.value = ProfileState(isLoggedIn = false)
    }

    fun login() {
        _profileState.value = ProfileState(isLoggedIn = true)
        loadUserProfile()

    }

    fun clearError() {
        _profileState.value = _profileState.value?.copy(error = null)
    }
}
