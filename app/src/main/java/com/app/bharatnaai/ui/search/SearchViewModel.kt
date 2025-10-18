package com.app.bharatnaai.ui.search

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.bharatnaai.data.model.FilterType
import com.app.bharatnaai.data.model.SearchState
import com.app.bharatnaai.data.model.Salon
import com.app.bharatnaai.data.repository.NearbySaloonRepository
import com.app.bharatnaai.utils.LocationHelper
import com.app.bharatnaai.utils.LocationResult
import kotlinx.coroutines.launch
import com.app.bharatnaai.utils.Constants

/**
 * SearchViewModel
 * -----------------
 * Handles:
 * 1. Fetching nearby salons via location (API)
 * 2. Managing search query and filters
 * 3. Combining both to show filtered salon list
 */
class SearchViewModel(application: Application) : AndroidViewModel(application) {

    private val _searchState = MutableLiveData<SearchState>()
    val searchState: LiveData<SearchState> = _searchState

    private val repository = NearbySaloonRepository()
    private val locationHelper = LocationHelper(application.applicationContext)

    init {
        _searchState.value = SearchState()
        fetchNearbySalonsByLocation()
    }

    fun updateSearchQuery(query: String) {
        val currentState = _searchState.value ?: return
        _searchState.value = currentState.copy(searchQuery = query)
        refreshFilteredList()
    }

    fun clearSearch() {
        val currentState = _searchState.value ?: return
        _searchState.value = currentState.copy(searchQuery = "")
        refreshFilteredList()
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
        refreshFilteredList()
    }



    fun fetchNearbySalonsByLocation() {
        _searchState.value = _searchState.value?.copy(isLoading = true)

        viewModelScope.launch {
            when (val locationResult = locationHelper.getCurrentLocation()) {
                is LocationResult.Success -> {
                    val lat = locationResult.location.latitude
                    val lng = locationResult.location.longitude
                    fetchNearbySalons(lat, lng)
                }
                is LocationResult.Error -> {
                    _searchState.value = _searchState.value?.copy(
                        isLoading = false,
                        error = locationResult.message
                    )
                }
            }
        }
    }

    fun fetchNearbySalons(lat: Double, lng: Double) {
        _searchState.value = _searchState.value?.copy(isLoading = true)

        viewModelScope.launch {
            try {
                val response = repository.getNearbySaloonDetails(lat, lng)
                val body = response.body()

                if (response.isSuccessful && body != null) {
                    val salons: List<Salon> = body.map { s ->
                        val absolute = if (s.imagePath.startsWith("http", ignoreCase = true)) s.imagePath
                        else Constants.BASE_URL.trim().trimEnd('/') + "/" + s.imagePath.trimStart('/')
                        s.copy(imagePath = absolute)
                    }
                    _searchState.value = _searchState.value?.copy(
                        allSalons = salons,
                        salons = salons, // initially unfiltered
                        isLoading = false,
                        error = null
                    )
                } else {
                    _searchState.value = _searchState.value?.copy(
                        isLoading = false,
                        error = "Failed to load salons"
                    )
                }
            } catch (e: Exception) {
                _searchState.value = _searchState.value?.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }

    private fun refreshFilteredList() {
        val currentState = _searchState.value ?: return
        var filteredSalons = currentState.allSalons

        // Get all active filters
        val activeFilters = currentState.filters.filter { it.isSelected }

        // Apply filters
        activeFilters.forEach { filter ->
            when (filter.type) {
                FilterType.RATING -> {
                    // Keep salons rated 4.0 or above (nullable-safe)
                    filteredSalons = filteredSalons.filter { (it.rating ?: 0.0) >= 4.0 }
                }

                FilterType.DISTANCE -> {
                    // Example: only show salons within 5 km (nullable-safe)
                    filteredSalons = filteredSalons.filter { (it.distance ?: Double.MAX_VALUE) <= 5.0 }
                }

                FilterType.PRICE -> {
                    // Example: show affordable salons (priceLevel <= 2) (nullable-safe)
                    filteredSalons = filteredSalons.filter { (it.priceLevel ?: Int.MAX_VALUE) <= 2 }
                }

                FilterType.SERVICE -> {
                    // Example: filter by a service name in current search query
                    val q = currentState.searchQuery
                    if (q.isNotBlank()) {
                        filteredSalons = filteredSalons.filter { salon ->
                            salon.services?.any { service ->
                                service.contains(q, ignoreCase = true)
                            } == true
                        }
                    }
                }
            }
        }

        // Apply search query after filtering
        val query = currentState.searchQuery
        if (query.isNotBlank()) {
            filteredSalons = filteredSalons.filter {
                it.salonName.contains(query, ignoreCase = true) ||
                        (it.address?.contains(query, ignoreCase = true) == true)
            }
        }

        // Update state
        _searchState.value = currentState.copy(salons = filteredSalons)
    }

    fun clearError() {
        _searchState.value = _searchState.value?.copy(error = null)
    }
}
