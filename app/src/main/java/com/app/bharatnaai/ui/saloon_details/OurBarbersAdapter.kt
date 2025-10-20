package com.app.bharatnaai.ui.saloon_details

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bharatnaai.databinding.ItemOurBarberBinding
import com.app.bharatnaai.data.model.Barber

class OurBarbersAdapter(
    private val onBarberClick: (Barber) -> Unit,
    private val onBookNow: (Barber) -> Unit
) : ListAdapter<Barber, OurBarbersAdapter.BarberViewHolder>(Diff()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BarberViewHolder {
        val binding = ItemOurBarberBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return BarberViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BarberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BarberViewHolder(
        private val binding: ItemOurBarberBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(barber: Barber) {
            binding.apply {
                tvBarberName.text = barber.barberName
                // Placeholders until backend provides details
                tvBarberRole.text = "Haircut Specialist"
                tvRating.text = "4.9 (120 reviews)"
                tvService1.text = "Classic Haircut"
                tvPrice1.text = "$40"
                tvService2.text = "Beard Trim"
                tvPrice2.text = "$25"

                root.setOnClickListener { onBarberClick(barber) }
                btnBookNow.setOnClickListener { onBookNow(barber) }
            }
        }
    }

    private class Diff : DiffUtil.ItemCallback<Barber>() {
        override fun areItemsTheSame(oldItem: Barber, newItem: Barber): Boolean =
            oldItem.barberId == newItem.barberId

        override fun areContentsTheSame(oldItem: Barber, newItem: Barber): Boolean =
            oldItem == newItem
    }
}
