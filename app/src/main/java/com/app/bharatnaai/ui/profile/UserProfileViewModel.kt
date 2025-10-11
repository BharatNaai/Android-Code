package com.app.bharatnaai.ui.profile

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.bharatnaai.data.repository.AuthRepository

data class UserProfile(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val email: String,
    val profileImageUrl: String? = null
)

data class ProfileState(
    val isLoggedIn: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val userProfile: UserProfile? = null
)

class UserProfileViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authRepository = AuthRepository(application.applicationContext)
    
    private val _profileState = MutableLiveData<ProfileState>()
    val profileState: LiveData<ProfileState> = _profileState
    
    private val _bookingHistory = MutableLiveData<List<BookingHistory>>()
    val bookingHistory: LiveData<List<BookingHistory>> = _bookingHistory
    
    private val _savedSalons = MutableLiveData<List<SavedSalon>>()
    val savedSalons: LiveData<List<SavedSalon>> = _savedSalons
    
    init {
        // Check actual authentication status
        val isLoggedIn = authRepository.isLoggedIn()
        _profileState.value = ProfileState(isLoggedIn = isLoggedIn)
        
        if (isLoggedIn) {
            loadUserProfile()
            loadBookingHistory()
            loadSavedSalons()
        }
    }
    
    private fun loadUserProfile() {
        // Mock user profile data
        val mockProfile = UserProfile(
            id = "1",
            name = "Sophia Carter",
            phoneNumber = "+1 (555) 123-4567",
            email = "sophia.carter@email.com"
        )
        
        _profileState.value = _profileState.value?.copy(
            userProfile = mockProfile
        )
    }
    
    private fun loadBookingHistory() {
        // Mock booking history data
        val mockBookings = listOf(
            BookingHistory(
                id = "1",
                salonName = "Salon Elegance",
                serviceName = "Haircut",
                bookingDate = "Oct 12, 2023"
            ),
            BookingHistory(
                id = "2",
                salonName = "Nail Studio",
                serviceName = "Manicure",
                bookingDate = "Sep 28, 2023"
            )
        )
        
        _bookingHistory.value = mockBookings
    }
    
    private fun loadSavedSalons() {
        // Mock saved salons data
        val mockSalons = listOf(
            SavedSalon(
                id = "1",
                name = "The Style Lounge",
                rating = 4.8f,
                distance = "1.2km"
            ),
            SavedSalon(
                id = "2",
                name = "Barber's Den",
                rating = 4.9f,
                distance = "2.5km"
            )
        )
        
        _savedSalons.value = mockSalons
    }
    
    fun logout() {
        // Clear authentication data
        authRepository.logout()
        
        // Update UI state
        _profileState.value = ProfileState(isLoggedIn = false)
        _bookingHistory.value = emptyList()
        _savedSalons.value = emptyList()
    }
    
    fun login() {
        _profileState.value = ProfileState(isLoggedIn = true)
        loadUserProfile()
        loadBookingHistory()
        loadSavedSalons()
    }
    
    fun removeSavedSalon(salon: SavedSalon) {
        val currentSalons = _savedSalons.value?.toMutableList() ?: return
        currentSalons.remove(salon)
        _savedSalons.value = currentSalons
    }
    
    fun clearError() {
        _profileState.value = _profileState.value?.copy(error = null)
    }
}
