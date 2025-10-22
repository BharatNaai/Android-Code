package com.app.bharatnaai.ui.barber_details

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.bharatnaai.data.model.Barber
import com.app.bharatnaai.data.network.ApiClient
import com.app.bharatnaai.data.repository.BarberSlotsRepository
import com.app.bharatnaai.data.model.Slot
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class ServiceChip(
    val id: String,
    val name: String,
    val price: Int,
    val selected: Boolean = false
)

data class BarberDetailsState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedBarber: Barber? = null,
    val monthTitle: String = "October 2024",
    val selectedDateMs: Long? = null,
    val timeSlots: List<Slot> = emptyList(),
    val selectedTimeIndex: Int = -1,
    val services: List<ServiceChip> = emptyList(),
    val promoCode: String? = null,
    val totalCents: Int = 4500
) {
    val totalLabel: String get() = "$" + String.format("%.2f", totalCents / 100.0)
}

class BarberDetailsViewModel(app: Application) : AndroidViewModel(app) {

    private val _state = MutableLiveData(BarberDetailsState())
    val state: LiveData<BarberDetailsState> = _state

    private val _barbers = MutableLiveData<List<Barber>>()
    val barbers: LiveData<List<Barber>> = _barbers

    private val slotsRepo = BarberSlotsRepository(getApplication(), ApiClient.apiService)

    init {
        _barbers.value?.firstOrNull()?.let { setSelectedBarber(it) }
        recalcTotal()
    }

    fun refresh() {
        _state.value = _state.value?.copy(isLoading = true, error = null)
        viewModelScope.launch {
            delay(500)
            _state.value = _state.value?.copy(isLoading = false)
        }
    }

    fun setSelectedBarber(barber: Barber) {
        _state.value = _state.value?.copy(selectedBarber = barber)
    }

    fun selectTimeSlot(index: Int) {
        _state.value = _state.value?.copy(selectedTimeIndex = index)
    }

    fun fetchSlots(barberId: Int, date: String) {
        _state.value = _state.value?.copy(isLoading = true, error = null)
        viewModelScope.launch {
            try {
                val body = slotsRepo.getAvailableSlots(barberId, date)
                val slots = body?.slots ?: emptyList()
                _state.value = _state.value?.copy(isLoading = false, timeSlots = slots, selectedTimeIndex = -1)
            } catch (e: Exception) {
                _state.value = _state.value?.copy(isLoading = false, error = e.message ?: "Unknown error")
            }
        }
    }

    fun toggleService(id: String) {
        val current = _state.value ?: return
        val updated = current.services.map { s -> if (s.id == id) s.copy(selected = !s.selected) else s }
        _state.value = current.copy(services = updated)
        recalcTotal()
    }

    fun applyPromo(code: String?) {
        _state.value = _state.value?.copy(promoCode = code)
        recalcTotal()
    }

    private fun recalcTotal() {
        val base = _state.value?.services?.filter { it.selected }?.sumOf { it.price } ?: 0
        // Simple rule: promo code "SAVE10" gives 10% off
        val discounted = if (_state.value?.promoCode.equals("SAVE10", ignoreCase = true)) (base * 0.9).toInt() else base
        _state.value = _state.value?.copy(totalCents = discounted)
    }
}
