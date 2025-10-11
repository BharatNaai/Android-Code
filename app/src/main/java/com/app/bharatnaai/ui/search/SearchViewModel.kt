package com.app.bharatnaai.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.bharatnaai.data.model.SaloonDetailsRequest
import com.app.bharatnaai.data.repository.NearbySaloonRepository
import com.app.bharatnaai.data.session.SessionManager
import com.app.bharatnaai.utils.LocationHelper
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    private val repository = NearbySaloonRepository()
    private val sessionManager = SessionManager.getInstance(application.applicationContext)
    private val locationHelper = LocationHelper(application.applicationContext)

    init {
        _searchState.value = SearchState(searchQuery = "Haircut")
        fetchNearbySalonsWithSessionAndLocation()
    }

    fun updateSearchQuery(query: String) {
        _searchState.value = _searchState.value?.copy(searchQuery = query)
        searchSalons(query)
    }

    fun clearSearch() {
        _searchState.value = _searchState.value?.copy(searchQuery = "")
        fetchNearbySalonsWithSessionAndLocation()
    }

    fun toggleFilter(filterType: FilterType) {
        val currentState = _searchState.value ?: return
        val updatedFilters = currentState.filters.map { filter ->
            if (filter.type == filterType) {
                filter.copy(isSelected = !filter.isSelected)
            } else {
                filter
            }
        }
        _searchState.value = currentState.copy(filters = updatedFilters)
        applyFilters()
    }

    private fun searchSalons(query: String) {
        val currentState = _searchState.value ?: return
        val allSalons = currentState.salons
        if (query.isBlank()) {
            fetchNearbySalonsWithSessionAndLocation()
            return
        }
        val filteredSalons = allSalons.filter { salon ->
            salon.name.contains(query, ignoreCase = true) ||
            salon.services.any { service ->
                service.name.contains(query, ignoreCase = true)
            }
        }
        _searchState.value = currentState.copy(salons = filteredSalons)
    }

    private fun applyFilters() {
        val currentState = _searchState.value ?: return
        var filteredSalons = currentState.salons
        if (currentState.filters.find { it.type == FilterType.RATING }?.isSelected == true) {
            filteredSalons = filteredSalons.filter { it.rating >= 4.5f }
        }
        _searchState.value = currentState.copy(salons = filteredSalons)
    }

    private fun fetchNearbySalonsWithSessionAndLocation() {
        _searchState.value = _searchState.value?.copy(isLoading = true)
        viewModelScope.launch {
            val authToken = sessionManager.getAccessToken()
            if (authToken.isNullOrBlank()) {
                _searchState.value = _searchState.value?.copy(
                    isLoading = false,
                    error = "User not authenticated. Please log in."
                )
                return@launch
            }
            when (val locationResult = locationHelper.getCurrentLocation()) {
                is com.app.bharatnaai.utils.LocationResult.Success -> {
                    val lat = locationResult.location.latitude.toString()
                    val lng = locationResult.location.longitude.toString()
                    fetchNearbySalons(authToken, lat, lng)
                }
                is com.app.bharatnaai.utils.LocationResult.Error -> {
                    _searchState.value = _searchState.value?.copy(
                        isLoading = false,
                        error = locationResult.message
                    )
                }
            }
        }
    }

    fun fetchNearbySalons(authToken: String, lat: String, lng: String) {
        _searchState.value = _searchState.value?.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val request = SaloonDetailsRequest(authToken, lat, lng)
                val response = repository.getNearbySaloonDetails(request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val salons = response.body()?.nearbySalons?.map { mapApiSalonToUi(it) } ?: emptyList()
                    _searchState.value = _searchState.value?.copy(
                        salons = salons,
                        isLoading = false,
                        error = null
                    )
                } else {
                    _searchState.value = _searchState.value?.copy(
                        isLoading = false,
                        error = response.body()?.message ?: "Failed to load salons"
                    )
                }
            } catch (e: Exception) {
                _searchState.value = _searchState.value?.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error"
                )
            }
        }
    }

    private fun mapApiSalonToUi(apiSalon: com.app.bharatnaai.data.model.Salon): Salon {
        return Salon(
            id = apiSalon.salonId.toString(),
            name = apiSalon.salonName,
            imageUrl = apiSalon.imagePath, // You may want to prepend base URL
            services = emptyList(), // Map barbers/services if needed
            rating = 0f, // If you have rating in API, use it
            reviewCount = 0, // If you have review count in API, use it
            distance = "", // Calculate or get from API if available
            address = apiSalon.address ?: "",
            isOpen = true // Or use API data if available
        )
    }

    fun clearError() {
        _searchState.value = _searchState.value?.copy(error = null)
    }
}
