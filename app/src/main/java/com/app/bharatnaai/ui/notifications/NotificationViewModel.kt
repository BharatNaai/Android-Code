package com.app.bharatnaai.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import bharatnaai.R
import com.app.bharatnaai.data.model.NotificationItem
import com.app.bharatnaai.data.model.NotificationSection
import com.app.bharatnaai.data.model.NotificationType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NotificationViewModel : ViewModel() {

    private val _notificationSections = MutableLiveData<List<NotificationSection>>()
    val notificationSections: LiveData<List<NotificationSection>> = _notificationSections

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isEmpty = MutableLiveData<Boolean>()
    val isEmpty: LiveData<Boolean> = _isEmpty

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _isLoading.value = true
            
            // Simulate API call delay
            kotlinx.coroutines.delay(500)
            
            val notifications = getMockNotifications()
            val sections = groupNotificationsByDate(notifications)
            
            _notificationSections.value = sections
            _isEmpty.value = sections.isEmpty() || sections.all { it.notifications.isEmpty() }
            _isLoading.value = false
        }
    }

    private fun getMockNotifications(): List<NotificationItem> {
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -1)
        }
        
        return listOf(
            // Today's notifications
            NotificationItem(
                id = "1",
                type = NotificationType.APPOINTMENT_CONFIRMED,
                title = "Appointment Confirmed",
                message = "Your appointment with Alex at The Barber Shop is confirmed.",
                timestamp = Calendar.getInstance().apply { 
                    set(Calendar.HOUR_OF_DAY, 10)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time,
                iconResource = R.drawable.ic_calendar_notification
            ),
            NotificationItem(
                id = "2",
                type = NotificationType.SPECIAL_OFFER,
                title = "Special Offer",
                message = "Special offer: 20% off your next haircut at The Barber Shop.",
                timestamp = Calendar.getInstance().apply { 
                    set(Calendar.HOUR_OF_DAY, 12)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time,
                iconResource = R.drawable.ic_percent_notification
            ),
            
            // Yesterday's notifications
            NotificationItem(
                id = "3",
                type = NotificationType.APPOINTMENT_REMINDER,
                title = "Appointment Reminder",
                message = "Reminder: Your appointment with Alex at The Barber Shop is tomorrow.",
                timestamp = yesterday.apply { 
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time,
                iconResource = R.drawable.ic_bell_notification
            ),
            NotificationItem(
                id = "4",
                type = NotificationType.APPOINTMENT_UPDATED,
                title = "Appointment Updated",
                message = "Your appointment with Alex at The Barber Shop has been updated.",
                timestamp = yesterday.apply { 
                    set(Calendar.HOUR_OF_DAY, 17)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.time,
                iconResource = R.drawable.ic_calendar_notification
            )
        )
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

    fun markNotificationAsRead(notificationId: String) {
        val currentSections = _notificationSections.value ?: return
        
        val updatedSections = currentSections.map { section ->
            section.copy(
                notifications = section.notifications.map { notification ->
                    if (notification.id == notificationId) {
                        notification.copy(isRead = true)
                    } else {
                        notification
                    }
                }
            )
        }
        
        _notificationSections.value = updatedSections
    }

    fun refreshNotifications() {
        loadNotifications()
    }
}
