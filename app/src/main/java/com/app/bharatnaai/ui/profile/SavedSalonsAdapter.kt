package com.app.bharatnaai.ui.profile

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bharatnaai.databinding.ItemSavedSalonBinding

data class SavedSalon(
    val id: String,
    val name: String,
    val rating: Float,
    val distance: String,
    val imageUrl: String? = null,
    val isBookmarked: Boolean = true
)

class SavedSalonsAdapter(
    private val onSalonClick: (SavedSalon) -> Unit,
    private val onBookmarkClick: (SavedSalon) -> Unit
) : ListAdapter<SavedSalon, SavedSalonsAdapter.SalonViewHolder>(SalonDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalonViewHolder {
        val binding = ItemSavedSalonBinding.inflate(
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
        private val binding: ItemSavedSalonBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(salon: SavedSalon) {
            binding.apply {
                tvSalonName.text = salon.name
                tvRating.text = salon.rating.toString()
                tvDistance.text = "• ${salon.distance}"
                
                // TODO: Load actual image using Glide/Picasso
                // For now, using placeholder color
                
                root.setOnClickListener {
                    onSalonClick(salon)
                }
                
                btnBookmark.setOnClickListener {
                    onBookmarkClick(salon)
                }
            }
        }
    }

    private class SalonDiffCallback : DiffUtil.ItemCallback<SavedSalon>() {
        override fun areItemsTheSame(oldItem: SavedSalon, newItem: SavedSalon): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SavedSalon, newItem: SavedSalon): Boolean {
            return oldItem == newItem
        }
    }
}
