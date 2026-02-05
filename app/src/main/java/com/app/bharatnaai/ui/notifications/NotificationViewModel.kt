package com.app.bharatnaai.ui.notifications

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.bharatnaai.data.model.NotificationItem
import com.app.bharatnaai.data.model.NotificationSection
import com.app.bharatnaai.data.repository.NotificationRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NotificationViewModel(application: Application) : AndroidViewModel(application) {

    private val _notificationSections = MutableLiveData<List<NotificationSection>>()
    val notificationSections: LiveData<List<NotificationSection>> = _notificationSections
    private val _toastMessage = MutableLiveData<String?>()
    val toastMessage: LiveData<String?> = _toastMessage
    private val repository = NotificationRepository(application.applicationContext)
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty
    init {
        // Keep empty by default; Fragment can supply repository data
        _notificationSections.value = emptyList()
        _isEmpty.value = true
    }

    fun setNotifications(notifications: List<NotificationItem>) {
        viewModelScope.launch {
            _isLoading.value = true
            val sections = groupNotificationsByDate(notifications)
            _notificationSections.value = sections
            _isEmpty.value = sections.isEmpty() || sections.all { it.notifications.isEmpty() }
            _isLoading.value = false
        }
    }



    private fun groupNotificationsByDate(notifications: List<NotificationItem>): List<NotificationSection> {
        val calendar = Calendar.getInstance()
        val today = calendar.time
        
        calendar.add(Calendar.DAY_OF_MONTH, -1)
        val yesterday = calendar.time
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        val groupedNotifications = notifications.groupBy { notification ->
            val notificationDate = dateFormat.format(notification.timestamp)
            val todayDate = dateFormat.format(today)
            val yesterdayDate = dateFormat.format(yesterday)
            
            when (notificationDate) {
                todayDate -> "Today"
                yesterdayDate -> "Yesterday"
                else -> {
                    val notificationCalendar = Calendar.getInstance().apply {
                        time = notification.timestamp
                    }
                    SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(notificationCalendar.time)
                }
            }
        }

        return groupedNotifications.map { (sectionTitle, notifications) ->
            NotificationSection(
                sectionTitle = sectionTitle,
                notifications = notifications.sortedByDescending { it.timestamp }
            )
        }.sortedWith { section1, section2 ->
            when {
                section1.sectionTitle == "Today" -> -1
                section2.sectionTitle == "Today" -> 1
                section1.sectionTitle == "Yesterday" -> -1
                section2.sectionTitle == "Yesterday" -> 1
                else -> section2.sectionTitle.compareTo(section1.sectionTitle)
            }
        }
    }

    fun sendPushNotification(fcmToken: String, message: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.sendPushNotification(fcmToken, message)
                if (response.isSuccessful) {
                    _toastMessage.value = "Notification sent"
                } else {
                    _toastMessage.value = response.errorBody()?.string() ?: "Failed to send notification"
                }
            } catch (e: Exception) {
                _toastMessage.value = "Failed to send notification"
            } finally {
                _isLoading.value = false
            }
        }
        
        _notificationSections.value = updatedSections
    }

    fun refreshNotifications() {
        // No-op by default; Fragment can reload from repository and call setNotifications
    }
    fun onToastShown() { _toastMessage.value = null }
}
