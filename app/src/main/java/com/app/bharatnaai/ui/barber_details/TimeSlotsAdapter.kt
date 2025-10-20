package com.app.bharatnaai.ui.barber_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bharatnaai.databinding.ItemTimeSlotBinding
import com.app.bharatnaai.data.model.Slot
import java.text.SimpleDateFormat
import java.util.Locale

class TimeSlotsAdapter(
    private val onSelect: (Int) -> Unit
) : ListAdapter<Slot, TimeSlotsAdapter.SlotVH>(Diff()) {

    var selectedIndex: Int = -1
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotVH {
        val binding = ItemTimeSlotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SlotVH(binding)
    }

    override fun onBindViewHolder(holder: SlotVH, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class SlotVH(private val binding: ItemTimeSlotBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(slot: Slot, index: Int) {
            binding.tvTime.text = formatLabel(slot)

            // Disabled if not AVAILABLE
            val available = slot.status.equals("AVAILABLE", ignoreCase = true)
            binding.tvTime.isEnabled = available
            binding.tvTime.alpha = if (available) 1.0f else 0.5f

            // Selected styling via isSelected state list in background
            binding.tvTime.isSelected = index == selectedIndex

            binding.root.setOnClickListener {
                if (available) onSelect(index)
            }
        }

        private fun formatLabel(slot: Slot): String {
            val timeIn = try {
                SimpleDateFormat("HH:mm:ss", Locale.US).parse(slot.startTime)
            } catch (_: Exception) { null }
            val timeOut = if (timeIn != null) SimpleDateFormat("hh:mm a", Locale.US).format(timeIn) else slot.startTime

            return "$timeOut"
        }
    }

    private class Diff : DiffUtil.ItemCallback<Slot>() {
        override fun areItemsTheSame(oldItem: Slot, newItem: Slot): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Slot, newItem: Slot): Boolean = oldItem == newItem
    }
}

