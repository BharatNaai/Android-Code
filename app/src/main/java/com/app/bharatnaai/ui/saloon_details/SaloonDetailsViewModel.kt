package com.app.bharatnaai.ui.saloon_details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import bharatnaai.R
import com.app.bharatnaai.ui.home.Service
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.app.bharatnaai.data.model.Barber
import com.app.bharatnaai.data.network.ApiClient
import com.app.bharatnaai.data.repository.SaloonDetailsRepo

data class SaloonDetailsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val salonName: String = "The Style Lounge",
    val about: String = "The Style Lounge is a premier salon offering a range of hair and beauty services. Our experienced stylists are dedicated to providing personalized care and exceptional results.",
    val address: String = "123 Main Street, Anytown",
    val hours: String = "Mon-Fri: 9 AM - 7 PM, Sat: 10 AM - 6 PM",
    val selectedService: Service? = null,
    val totalAmountText: String = "$120.00",
    val saloonImage : String = ""
)

class SaloonDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableLiveData(SaloonDetailsState())
    val state: LiveData<SaloonDetailsState> = _state

    private val _services = MutableLiveData<List<Service>>()
    val services: LiveData<List<Service>> = _services

    private val _barbers = MutableLiveData<List<Barber>>()
    val barbers: LiveData<List<Barber>> = _barbers

    private val repo = SaloonDetailsRepo(getApplication(), ApiClient.apiService)

    init {
        loadServices()
        // Default selected service
        _services.value?.firstOrNull()?.let { svc ->
            _state.value = _state.value?.copy(selectedService = svc)
        }
    }

    fun loadSalon(salonId: Int) {
        _state.value = _state.value?.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val details = repo.fetchSalonDetails(salonId)
                if (details != null) {
                    _state.value = _state.value?.copy(
                        isLoading = false,
                        salonName = details.salonName,
                        address = details.address ?: "",
                        saloonImage = details.imagePath
                    )

                    _barbers.value = details.barbers

                } else {
                    _state.value = _state.value?.copy(isLoading = false, error = "Failed to load salon details")
                }
            } catch (e: Exception) {
                _state.value = _state.value?.copy(isLoading = false, error = e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() {
        _state.value = _state.value?.copy(isLoading = true, error = null)
        viewModelScope.launch {
            // Simulate fetch; replace with repository call when API is ready
            delay(600)
            // In a real implementation, fetch barbers/services for the selected salon here
            _state.value = _state.value?.copy(isLoading = false)
        }
    }

    fun selectService(service: Service) {
        _state.value = _state.value?.copy(selectedService = service)
    }

    fun cycleNextService() {
        val list = _services.value.orEmpty()
        if (list.isEmpty()) return
        val current = _state.value?.selectedService
        val idx = list.indexOfFirst { it.id == current?.id }
        val next = if (idx == -1 || idx == list.lastIndex) list.first() else list[idx + 1]
        selectService(next)
    }

    fun clearError() {
        _state.value = _state.value?.copy(error = null)
    }

    private fun loadServices() {
        val app = getApplication<Application>()
        val items = listOf(
            Service(id = "1", name = "Haircut", iconResId = R.drawable.ic_haircut),
            Service(id = "2", name = "Shaving", iconResId = R.drawable.ic_shaving),
            Service(id = "3", name = "Grooming", iconResId = R.drawable.ic_grooming),
            Service(id = "4", name = "Packages", iconResId = R.drawable.ic_packages)
        )
        _services.value = items
    }

    // Barbers now come from API; keep method out
}
