package com.app.bharatnaai.ui.search

data class Salon(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val services: List<SalonService>,
    val rating: Float,
    val reviewCount: Int,
    val distance: String,
    val address: String,
    val isOpen: Boolean = true,
    val openingHours: String? = null
)

data class SalonService(
    val id: String,
    val name: String,
    val price: String,
    val duration: String? = null,
    val description: String? = null
)

data class SearchFilter(
    val type: FilterType,
    val isSelected: Boolean = false
)

enum class FilterType(val displayName: String) {
    DISTANCE("Distance"),
    RATING("Rating"),
    PRICE("Price"),
    SERVICE("Service")
}

data class SearchState(
    val isLoading: Boolean = false,
    val searchQuery: String = "",
    val salons: List<Salon> = emptyList(),
    val filters: List<SearchFilter> = FilterType.values().map { 
        SearchFilter(it, it == FilterType.RATING) // Rating selected by default as shown in UI
    },
    val error: String? = null,
    val selectedService: String? = null
)
