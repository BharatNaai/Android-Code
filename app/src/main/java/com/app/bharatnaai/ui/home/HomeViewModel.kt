package com.app.bharatnaai.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import bharatnaai.R
import com.app.bharatnaai.data.model.Salon
import com.app.bharatnaai.data.repository.NearbySaloonRepository
import com.app.bharatnaai.utils.LocationHelper
import com.app.bharatnaai.utils.LocationWithAddressResult
import kotlinx.coroutines.launch
import com.app.bharatnaai.utils.Constants

data class HomeState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentLocation: String = "",
    val isLocationLoading: Boolean = false,
    val hasLocationPermission: Boolean = false
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    
    private val locationHelper = LocationHelper(application)
    private val nearbyRepo = NearbySaloonRepository()
    
    private val _homeState = MutableLiveData<HomeState>()
    val homeState: LiveData<HomeState> = _homeState
    
    private val _featuredSalons = MutableLiveData<List<Salon>>()
    val featuredSalons: LiveData<List<Salon>> = _featuredSalons
    
    private val _exclusiveOffers = MutableLiveData<List<ExclusiveOffer>>()
    val exclusiveOffers: LiveData<List<ExclusiveOffer>> = _exclusiveOffers
    
    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>> = _services
    
    init {
        _homeState.value = HomeState()
        loadExclusiveOffers()
        loadServices()
    }
    
    fun updateLocation(location: String) {
        _homeState.value = _homeState.value?.copy(currentLocation = location)
    }

    fun fetchNearbySalonsByLocation() {
        // Show small spinner for location while we resolve address
        _homeState.value = _homeState.value?.copy(isLocationLoading = true)

        viewModelScope.launch {
            when (val result = locationHelper.getCurrentLocationWithAddress()) {
                is LocationWithAddressResult.Success -> {
                    val lat = result.location.latitude
                    val lng = result.location.longitude
                    // Update readable location in state
                    _homeState.value = _homeState.value?.copy(
                        currentLocation = result.locationName,
                        isLocationLoading = false,
                        error = null
                    )
                    // Fetch salons for featured section
                    fetchFeaturedSalons(lat, lng)
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

    private fun fetchFeaturedSalons(lat: Double, lng: Double) {
        // Optionally set loading state if needed
        _homeState.value = _homeState.value?.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val response = nearbyRepo.getNearbySaloonDetails(lat, lng)
                val body = response.body()

                if (response.isSuccessful && body != null) {
                    val withAbsoluteImages = body.map { s ->
                        val raw = s.imagePath.trim()
                        // If the string accidentally contains multiple URLs separated by whitespace, take the last token
                        val tokenized = raw.split(Regex("\\s+")).lastOrNull()?.trim().orEmpty()
                        val absolute = if (tokenized.startsWith("http", ignoreCase = true)) tokenized
                        else Constants.BASE_URL.trim().trimEnd('/') + "/" + tokenized.trimStart('/')
                        s.copy(imagePath = absolute)
                    }
                    _featuredSalons.value = withAbsoluteImages
                    _homeState.value = _homeState.value?.copy(isLoading = false)
                } else {
                    _homeState.value = _homeState.value?.copy(
                        isLoading = false,
                        error = "Failed to load featured salons"
                    )
                }
            } catch (e: Exception) {
                _homeState.value = _homeState.value?.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error loading salons"
                )
            }
        }
    }
    
    fun onLocationPermissionGranted() {
        _homeState.value = _homeState.value?.copy(hasLocationPermission = true)
        fetchNearbySalonsByLocation()
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
