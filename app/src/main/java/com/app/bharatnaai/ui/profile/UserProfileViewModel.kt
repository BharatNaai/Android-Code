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
    
    fun loadUserProfile() {
        val context = getApplication<Application>().applicationContext

        // Mock user profile data
        val profileData = CustomerDetails(
            fullName = PreferenceManager.getUserName(context)?:"",
            phone = PreferenceManager.getUserPhone(context)?:"",
            email = PreferenceManager.getUserEmail(context)?:"",
            userId = PreferenceManager.getUserId(context)?:0
        )
        
        _profileState.value = _profileState.value?.copy(
            customerDetails = profileData
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
