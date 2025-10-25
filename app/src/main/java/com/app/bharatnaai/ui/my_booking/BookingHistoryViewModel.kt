package com.app.bharatnaai.ui.my_booking

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.app.bharatnaai.data.network.ApiClient
import com.app.bharatnaai.data.repository.BookingRepository
import com.app.bharatnaai.utils.PreferenceManager
import kotlinx.coroutines.launch

class BookingHistoryViewModel(app: Application) : AndroidViewModel(app) {

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isEmpty = MutableLiveData(false)
    val isEmpty: LiveData<Boolean> = _isEmpty

    private val allItems = MutableLiveData<List<BookingItem>>()

    private val _currentTab = MutableLiveData(BookingStatus.UPCOMING)
    val currentTab: LiveData<BookingStatus> = _currentTab

    private val _items = MutableLiveData<List<BookingItem>>()
    val items: LiveData<List<BookingItem>> = _items

    private val repo = BookingRepository(getApplication(), ApiClient.apiService)

    init {
        // Attempt auto-fetch using saved user id
        val uid = PreferenceManager.getUserId(getApplication())
        if (uid != null) fetch(uid) else _isEmpty.value = true
    }

    fun setTab(status: BookingStatus) {
        if (_currentTab.value == status) return
        _currentTab.value = status
        filter()
    }

    fun refresh() {
        val uid = PreferenceManager.getUserId(getApplication())
        if (uid != null) fetch(uid)
    }

    fun fetch(userId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val details = repo.getBookingDetails(userId)
                val mapped = details.map { d ->
                    val status = if (d.status.equals("COMPLETED", ignoreCase = true)) BookingStatus.COMPLETED else BookingStatus.UPCOMING
                    BookingItem(
                        id = d.bookingId,
                        status = status,
                        title = d.serviceType,
                        subtitle = d.salonName + " ( " + d.barberName + " )",
                        dateTime = d.bookingDate + "  •  " + d.startTime + " - " + d.endTime,
                        price = "",
                        imageUrl = d.salonImage
                    )
                }
                allItems.value = mapped
                filter()
            } catch (e: Exception) {
                allItems.value = emptyList()
                filter()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun filter() {
        val status = _currentTab.value ?: BookingStatus.UPCOMING
        val filtered = (allItems.value ?: emptyList()).filter { it.status == status }
        _items.value = filtered
        _isEmpty.value = filtered.isEmpty()
    }
}
