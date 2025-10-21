package com.app.bharatnaai.ui.my_booking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bharatnaai.databinding.ItemBookingSuccessBinding
import bharatnaai.databinding.ItemBookingSuccessSectionBinding

data class BookingSuccessItem(
    val id: String,
    val salonName: String,
    val serviceName: String,
    val confirmationNo: String,
    val dateLabel: String,
    val timeLabel: String
)

data class BookingSuccessSection(
    val sectionTitle: String,
    val items: List<BookingSuccessItem>
)

class BookingSuccessAdapter(
    private val onItemClick: (BookingSuccessItem) -> Unit
) : ListAdapter<BookingSuccessSection, RecyclerView.ViewHolder>(SectionDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemBookingSuccessSectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SectionVH(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as SectionVH).bind(getItem(position))
    }

    inner class SectionVH(private val binding: ItemBookingSuccessSectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(section: BookingSuccessSection) {
            binding.tvSectionTitle.text = section.sectionTitle
            val inner = BookingSuccessItemAdapter(onItemClick)
            binding.rvSectionItems.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = inner
                setHasFixedSize(false)
            }
            inner.submitList(section.items)
        }
    }

    class SectionDiff : DiffUtil.ItemCallback<BookingSuccessSection>() {
        override fun areItemsTheSame(oldItem: BookingSuccessSection, newItem: BookingSuccessSection): Boolean =
            oldItem.sectionTitle == newItem.sectionTitle
        override fun areContentsTheSame(oldItem: BookingSuccessSection, newItem: BookingSuccessSection): Boolean =
            oldItem == newItem
    }
}

class BookingSuccessItemAdapter(
    private val onItemClick: (BookingSuccessItem) -> Unit
) : ListAdapter<BookingSuccessItem, BookingSuccessItemAdapter.ItemVH>(ItemDiff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemVH {
        val binding = ItemBookingSuccessBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemVH(binding)
    }

    override fun onBindViewHolder(holder: ItemVH, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ItemVH(private val binding: ItemBookingSuccessBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BookingSuccessItem) {
            binding.tvSalonName.text = item.salonName
            binding.tvServiceName.text = item.serviceName
            binding.tvConfirmNo.text = item.confirmationNo
            binding.tvDate.text = item.dateLabel
            binding.tvTime.text = item.timeLabel
            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    class ItemDiff : DiffUtil.ItemCallback<BookingSuccessItem>() {
        override fun areItemsTheSame(oldItem: BookingSuccessItem, newItem: BookingSuccessItem): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: BookingSuccessItem, newItem: BookingSuccessItem): Boolean = oldItem == newItem
    }
}
