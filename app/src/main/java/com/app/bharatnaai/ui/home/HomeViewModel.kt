package com.app.bharatnaai.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import bharatnaai.R
import com.app.bharatnaai.utils.LocationHelper
import com.app.bharatnaai.utils.LocationWithAddressResult
import kotlinx.coroutines.launch

data class HomeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentLocation: String = "San Francisco",
    val isLocationLoading: Boolean = false,
    val hasLocationPermission: Boolean = false
)

data class FeaturedSalon(
    val id: String,
    val name: String,
    val rating: Float,
    val reviewCount: Int,
    val imageUrl: String? = null
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val locationHelper = LocationHelper(application)
    
    private val _homeState = MutableLiveData<HomeState>()
    val homeState: LiveData<HomeState> = _homeState
    
    private val _featuredSalons = MutableLiveData<List<FeaturedSalon>>()
    val featuredSalons: LiveData<List<FeaturedSalon>> = _featuredSalons
    
    private val _exclusiveOffers = MutableLiveData<List<ExclusiveOffer>>()
    val exclusiveOffers: LiveData<List<ExclusiveOffer>> = _exclusiveOffers
    
    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>> = _services
    
    init {
        _homeState.value = HomeState()
        loadFeaturedSalons()
        loadExclusiveOffers()
        loadServices()
    }
    
    private fun loadFeaturedSalons() {
        // Mock data for featured salons
        val mockSalons = listOf(
            FeaturedSalon(
                id = "1",
                name = "The Style Lounge",
                rating = 4.8f,
                reviewCount = 120
            ),
            FeaturedSalon(
                id = "2",
                name = "Sharp Cuts",
                rating = 4.9f,
                reviewCount = 95
            ),
            FeaturedSalon(
                id = "3",
                name = "Gentleman's Choice",
                rating = 4.7f,
                reviewCount = 203
            )
        )
        
        _featuredSalons.value = mockSalons
    }
    
    fun updateLocation(location: String) {
        _homeState.value = _homeState.value?.copy(currentLocation = location)
    }
    
    fun checkLocationPermission() {
        val hasPermission = locationHelper.hasLocationPermissions()
        _homeState.value = _homeState.value?.copy(hasLocationPermission = hasPermission)
        
        if (hasPermission) {
            getCurrentLocation()
        }
    }
    
    fun getCurrentLocation() {
        if (!locationHelper.hasLocationPermissions()) {
            _homeState.value = _homeState.value?.copy(
                error = "Location permission is required to get your current location"
            )
            return
        }
        
        if (!locationHelper.isLocationEnabled()) {
            _homeState.value = _homeState.value?.copy(
                error = "Please enable location services to get your current location"
            )
            return
        }
        
        _homeState.value = _homeState.value?.copy(isLocationLoading = true)
        
        viewModelScope.launch {
            when (val result = locationHelper.getCurrentLocationWithAddress()) {
                is LocationWithAddressResult.Success -> {
                    _homeState.value = _homeState.value?.copy(
                        currentLocation = result.locationName,
                        isLocationLoading = false,
                        error = null
                    )
                }
                is LocationWithAddressResult.Error -> {
                    _homeState.value = _homeState.value?.copy(
                        isLocationLoading = false,
                        error = result.message
                    )
                }
            }
        }
    }
    
    fun onLocationPermissionGranted() {
        _homeState.value = _homeState.value?.copy(hasLocationPermission = true)
        getCurrentLocation()
    }
    
    fun onLocationPermissionDenied() {
        _homeState.value = _homeState.value?.copy(
            hasLocationPermission = false,
            error = "Location permission denied. You can manually select your location."
        )
    }
    
    fun searchSalons(query: String) {
        // TODO: Implement search functionality
        _homeState.value = _homeState.value?.copy(isLoading = true)
        
        // Simulate search
        // In real implementation, this would call a repository/API
    }
    
    fun clearError() {
        _homeState.value = _homeState.value?.copy(error = null)
    }
    
    private fun loadExclusiveOffers() {
        // Mock data for exclusive offers
        val mockOffers = listOf(
            ExclusiveOffer(
                id = "1",
                title = "Exclusive Offers",
                description = "Save up to 20% on your first booking",
                discountPercentage = 20
            ),
            ExclusiveOffer(
                id = "2",
                title = "Weekend Special",
                description = "Get 15% off on weekend appointments",
                discountPercentage = 15
            ),
            ExclusiveOffer(
                id = "3",
                title = "Premium Package",
                description = "Complete grooming package at discounted rates",
                discountPercentage = 25
            )
        )
        
        _exclusiveOffers.value = mockOffers
    }
    
    private fun loadServices() {
        // Mock data for services
        val mockServices = listOf(
            Service(
                id = "1",
                name = "Haircut",
                iconResId = R.drawable.ic_haircut
            ),
            Service(
                id = "2",
                name = "Shaving",
                iconResId = R.drawable.ic_shaving
            ),
            Service(
                id = "3",
                name = "Grooming",
                iconResId = R.drawable.ic_grooming
            ),
            Service(
                id = "4",
                name = "Packages",
                iconResId = R.drawable.ic_packages
            )
        )
        
        _services.value = mockServices
    }
}
