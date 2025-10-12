package com.app.bharatnaai.ui.notifications

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bharatnaai.R
import bharatnaai.databinding.ItemNotificationBinding
import bharatnaai.databinding.ItemNotificationSectionBinding
import com.app.bharatnaai.data.model.NotificationItem
import com.app.bharatnaai.data.model.NotificationSection
import com.app.bharatnaai.data.model.NotificationType
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val onNotificationClick: (NotificationItem) -> Unit
) : ListAdapter<NotificationSection, RecyclerView.ViewHolder>(NotificationDiffCallback()) {

    companion object {
        private const val TYPE_SECTION = 0
        private const val TYPE_NOTIFICATION = 1
    }

    override fun getItemViewType(position: Int): Int {
        return TYPE_SECTION // We'll handle sections differently
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SectionViewHolder(
            ItemNotificationSectionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SectionViewHolder -> {
                holder.bind(getItem(position))
            }
        }
    }

    inner class SectionViewHolder(
        private val binding: ItemNotificationSectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(section: NotificationSection) {
            binding.tvSectionTitle.text = section.sectionTitle
            
            val notificationAdapter = NotificationItemAdapter(onNotificationClick)
            binding.rvSectionNotifications.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = notificationAdapter
                setHasFixedSize(false)
            }
            notificationAdapter.submitList(section.notifications)
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationSection>() {
        override fun areItemsTheSame(oldItem: NotificationSection, newItem: NotificationSection): Boolean {
            return oldItem.sectionTitle == newItem.sectionTitle
        }

        override fun areContentsTheSame(oldItem: NotificationSection, newItem: NotificationSection): Boolean {
            return oldItem == newItem
        }
    }
}

class NotificationItemAdapter(
    private val onNotificationClick: (NotificationItem) -> Unit
) : ListAdapter<NotificationItem, NotificationItemAdapter.NotificationViewHolder>(NotificationItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        return NotificationViewHolder(
            ItemNotificationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(notification: NotificationItem) {
            binding.apply {
                // Set notification message
                tvNotificationMessage.text = notification.message
                
                // Format and set time
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                tvNotificationTime.text = timeFormat.format(notification.timestamp)
                
                // Set icon based on notification type
                val iconRes = when (notification.type) {
                    NotificationType.APPOINTMENT_CONFIRMED -> R.drawable.ic_calendar_notification
                    NotificationType.APPOINTMENT_REMINDER -> R.drawable.ic_bell_notification
                    NotificationType.APPOINTMENT_UPDATED -> R.drawable.ic_calendar_notification
                    NotificationType.SPECIAL_OFFER -> R.drawable.ic_percent_notification
                    NotificationType.GENERAL -> R.drawable.ic_bell_notification
                }
                ivNotificationIcon.setImageResource(iconRes)
                
                // Set click listener
                root.setOnClickListener {
                    onNotificationClick(notification)
                }
                
                // Update appearance based on read status
                root.alpha = if (notification.isRead) 0.7f else 1.0f
            }
        }
    }

    class NotificationItemDiffCallback : DiffUtil.ItemCallback<NotificationItem>() {
        override fun areItemsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: NotificationItem, newItem: NotificationItem): Boolean {
            return oldItem == newItem
        }
    }
}
