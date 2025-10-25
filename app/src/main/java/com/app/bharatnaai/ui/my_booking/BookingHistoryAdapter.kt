package com.app.bharatnaai.ui.my_booking

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bharatnaai.databinding.ItemBookingCardBinding

enum class BookingStatus { UPCOMING, COMPLETED }

data class BookingItem(
    val id: String,
    val status: BookingStatus,
    val title: String,
    val subtitle: String,
    val dateTime: String,
    val price: String,
    val imageUrl: String? = null
)

class BookingHistoryAdapter(
    private val listener: Listener
) : ListAdapter<BookingItem, BookingHistoryAdapter.ItemVH>(ItemDiff()) {

    interface Listener {
        fun onViewDetails(item: BookingItem)
        fun onReschedule(item: BookingItem)
        fun onRebook(item: BookingItem)
        fun onReview(item: BookingItem)
        fun onOverflow(anchor: View, item: BookingItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemVH {
        val binding = ItemBookingCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemVH(binding)
    }

    override fun onBindViewHolder(holder: ItemVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemVH(private val binding: ItemBookingCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BookingItem) {
            binding.tvStatus.text = if (item.status == BookingStatus.UPCOMING) "CONFIRMED" else "COMPLETED"
            binding.tvTitle.text = item.title
            binding.tvSubtitle.text = item.subtitle
            binding.tvMeta.text = item.dateTime + "  •  " + item.price

            // Button visibility and actions per status
            if (item.status == BookingStatus.UPCOMING) {
                binding.btnViewDetails.text = "View Details"
                binding.btnReschedule.text = "Reschedule"
                binding.btnReschedule.setOnClickListener { listener.onReschedule(item) }
                binding.btnViewDetails.setOnClickListener { listener.onViewDetails(item) }
            } else {
                binding.btnViewDetails.text = "Rebook"
                binding.btnReschedule.text = "Leave a Review"
                binding.btnReschedule.setOnClickListener { listener.onReview(item) }
                binding.btnViewDetails.setOnClickListener { listener.onRebook(item) }
            }

            binding.btnOverflow.setOnClickListener { view -> listener.onOverflow(view, item) }
            binding.root.setOnClickListener { listener.onViewDetails(item) }
        }
    }

    class ItemDiff : DiffUtil.ItemCallback<BookingItem>() {
        override fun areItemsTheSame(oldItem: BookingItem, newItem: BookingItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: BookingItem, newItem: BookingItem): Boolean = oldItem == newItem
    }
}
