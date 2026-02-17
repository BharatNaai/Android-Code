package com.app.bharatnaai.data.model

import java.util.Date

data class NotificationItem(
    val id: String,
    val type: NotificationType = NotificationType.GENERAL,
    val title: String,
    val message: String,
    val timestamp: Date = Date(),
    val isRead: Boolean = false,
    val iconResource: Int = 0, // Default value to avoid JSON parsing errors
    val actionData: String? = null // For navigation or additional data
)

enum class NotificationType {
    APPOINTMENT_CONFIRMED,
    APPOINTMENT_REMINDER,
    APPOINTMENT_UPDATED,
    SPECIAL_OFFER,
    GENERAL
}

data class NotificationSection(
    val sectionTitle: String,
    val notifications: List<NotificationItem>
)
