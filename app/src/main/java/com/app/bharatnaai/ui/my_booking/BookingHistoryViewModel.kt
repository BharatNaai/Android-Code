package com.app.bharatnaai.ui.my_booking

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

class BookingHistoryViewModel : ViewModel() {

    private val _sections = MutableLiveData<List<BookingSuccessSection>>()
    val sections: LiveData<List<BookingSuccessSection>> = _sections

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isEmpty = MutableLiveData<Boolean>(false)
    val isEmpty: LiveData<Boolean> = _isEmpty

    init { loadBookings() }

    fun loadBookings() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(400)
            val items = mockItems()
            val grouped = items.groupBy { it.dateLabel }
                .map { (date, itemsForDate) -> BookingSuccessSection(date, itemsForDate) }
            _sections.value = grouped
            _isEmpty.value = grouped.isEmpty()
            _isLoading.value = false
        }
    }

    private fun mockItems(): List<BookingSuccessItem> {
        return listOf(
            BookingSuccessItem(
                id = "1",
                salonName = "The Style Lounge",
                serviceName = "Haircut",
                confirmationNo = "#BK120459",
                dateLabel = "Today",
                timeLabel = "09:40 AM"
            ),
            BookingSuccessItem(
                id = "2",
                salonName = "Barber Bros",
                serviceName = "Beard",
                confirmationNo = "#BK120460",
                dateLabel = "Yesterday",
                timeLabel = "11:10 AM"
            )
        )
    }
}
