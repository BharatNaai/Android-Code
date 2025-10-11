package com.app.bharatnaai.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bharatnaai.databinding.ItemBookingHistoryBinding

data class BookingHistory(
    val id: String,
    val salonName: String,
    val serviceName: String,
    val bookingDate: String,
    val salonImageUrl: String? = null
)

class BookingHistoryAdapter(
    private val onBookingClick: (BookingHistory) -> Unit
) : ListAdapter<BookingHistory, BookingHistoryAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookingViewHolder(
        private val binding: ItemBookingHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: BookingHistory) {
            binding.apply {
                tvSalonName.text = booking.salonName
                tvServiceName.text = booking.serviceName
                tvBookingDate.text = booking.bookingDate
                
                // TODO: Load actual image using Glide/Picasso
                // For now, using placeholder color
                
                root.setOnClickListener {
                    onBookingClick(booking)
                }
            }
        }
    }

    private class BookingDiffCallback : DiffUtil.ItemCallback<BookingHistory>() {
        override fun areItemsTheSame(oldItem: BookingHistory, newItem: BookingHistory): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BookingHistory, newItem: BookingHistory): Boolean {
            return oldItem == newItem
        }
    }
}
