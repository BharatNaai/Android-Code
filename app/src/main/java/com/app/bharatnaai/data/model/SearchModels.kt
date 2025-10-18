package com.app.bharatnaai.data.model

data class SaloonDetailsResponse(
    val nearbySalons: List<Salon>,
    val message: String,
    val success: Boolean
)

data class Salon(
    val salonId: Int,
    val salonName: String,
    val address: String?, // nullable
    val imagePath: String,
    val latitude: Double,
    val longitude: Double,
    val barbers: List<Barber>,
    val rating: Double? = null,
    val distance: Double? = null, // in km if provided by backend
    val priceLevel: Int? = null, // 1..n if provided
    val services: List<String>? = null // names of services if provided
)

data class Barber(
    val barberId: Int,
    val barberName: String,
    val phone: String,
    val email: String
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
    val allSalons: List<Salon> = emptyList(),
    val salons: List<Salon> = emptyList(),
    val searchQuery: String = "",
    val filters: List<SearchFilter> = listOf(
        SearchFilter(FilterType.DISTANCE),
        SearchFilter(FilterType.RATING),
        SearchFilter(FilterType.PRICE),
        SearchFilter(FilterType.SERVICE)
    ),
    val isLoading: Boolean = false,
    val error: String? = null
)

