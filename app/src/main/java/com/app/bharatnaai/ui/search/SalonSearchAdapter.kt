package com.app.bharatnaai.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bharatnaai.databinding.ItemSalonSearchBinding

class SalonSearchAdapter(
    private val onSalonClick: (Salon) -> Unit,
    private val onBookNowClick: (Salon) -> Unit
) : ListAdapter<Salon, SalonSearchAdapter.SalonViewHolder>(SalonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalonViewHolder {
        val binding = ItemSalonSearchBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SalonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SalonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SalonViewHolder(
        private val binding: ItemSalonSearchBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(salon: Salon) {
            binding.apply {
                tvSalonName.text = salon.name
                tvRating.text = salon.rating.toString()
                tvDistance.text = salon.distance
                
                // Show primary service and price
                val primaryService = salon.services.firstOrNull()
                tvServicePrice.text = if (primaryService != null) {
                    "${primaryService.name} • ${primaryService.price}"
                } else {
                    "Services available"
                }
                
                // TODO: Load actual image using Glide/Picasso
                // For now, using placeholder
                // Glide.with(ivSalonImage.context)
                //     .load(salon.imageUrl)
                //     .placeholder(R.drawable.ic_salon_placeholder)
                //     .into(ivSalonImage)
                
                root.setOnClickListener {
                    onSalonClick(salon)
                }

                btnBookNow.setOnClickListener {
                    onBookNowClick(salon)
                }
            }
        }
    }

    private class SalonDiffCallback : DiffUtil.ItemCallback<Salon>() {
        override fun areItemsTheSame(oldItem: Salon, newItem: Salon): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Salon, newItem: Salon): Boolean {
            return oldItem == newItem
        }
    }
}
