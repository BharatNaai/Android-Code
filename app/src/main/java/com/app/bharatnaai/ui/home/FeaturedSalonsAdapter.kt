package com.app.bharatnaai.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bharatnaai.databinding.ItemFeaturedSalonBinding

class FeaturedSalonsAdapter(
    private val onSalonClick: (FeaturedSalon) -> Unit
) : ListAdapter<FeaturedSalon, FeaturedSalonsAdapter.SalonViewHolder>(SalonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalonViewHolder {
        val binding = ItemFeaturedSalonBinding.inflate(
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
        private val binding: ItemFeaturedSalonBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(salon: FeaturedSalon) {
            binding.apply {
                tvSalonName.text = salon.name
                tvRating.text = salon.rating.toString()
                tvReviews.text = "(${salon.reviewCount} reviews)"
                
                // TODO: Load actual image using Glide/Picasso
                // For now, using placeholder color
                
                root.setOnClickListener {
                    onSalonClick(salon)
                }
            }
        }
    }

    private class SalonDiffCallback : DiffUtil.ItemCallback<FeaturedSalon>() {
        override fun areItemsTheSame(oldItem: FeaturedSalon, newItem: FeaturedSalon): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: FeaturedSalon, newItem: FeaturedSalon): Boolean {
            return oldItem == newItem
        }
    }
}
