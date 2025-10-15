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
                imageUrl = "https://media.istockphoto.com/id/1783519753/photo/young-woman-enjoying-while-getting-her-hair-washed-by-professional-hairdresser-at-salon.jpg?s=1024x1024&w=is&k=20&c=WkFrL5nT31nA23j5sePpr3zgbLOMqlMQ6G80x3UdfnA=",
                discountPercentage = 20
            ),
            ExclusiveOffer(
                id = "2",
                title = "Weekend Special",
                description = "Get 15% off on weekend appointments",
                imageUrl = "https://images.unsplash.com/photo-1505691938895-1758d7feb511?auto=format&fit=crop&w=800&q=80",
                discountPercentage = 15
            ),
            ExclusiveOffer(
                id = "3",
                title = "Premium Package",
                description = "Complete grooming package at discounted rates",
                imageUrl = "https://images.unsplash.com/photo-1623171678074-1b04ff0e694f?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=2070",
                discountPercentage = 25
            ),
            ExclusiveOffer(
                id = "4",
                title = "Exclusive Offers",
                description = "Save up to 20% on your first booking",
                imageUrl = "https://images.unsplash.com/photo-1623171678074-1b04ff0e694f?ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D&auto=format&fit=crop&q=80&w=2070",
                discountPercentage = 20
            ),
            ExclusiveOffer(
                id = "5",
                title = "Weekend Special",
                description = "Get 15% off on weekend appointments",
                imageUrl = "https://media.istockphoto.com/id/1783229950/photo/woman-using-mobile-phone-while-getting-hair-treatment-under-a-professional-hair-steamer.jpg?s=1024x1024&w=is&k=20&c=YYOp-lds2_9_i7k3zEU4QGgdg_x5Cky-877BBBbvRj0=",
                discountPercentage = 15
            ),
            ExclusiveOffer(
                id = "6",
                title = "Premium Package",
                description = "Complete grooming package at discounted rates",
                imageUrl = "https://media.istockphoto.com/id/1783519753/photo/young-woman-enjoying-while-getting-her-hair-washed-by-professional-hairdresser-at-salon.jpg?s=1024x1024&w=is&k=20&c=WkFrL5nT31nA23j5sePpr3zgbLOMqlMQ6G80x3UdfnA=",
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
