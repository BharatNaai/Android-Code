package com.app.bharatnaai.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bharatnaai.R
import bharatnaai.databinding.ItemFeaturedSalonBinding
import com.app.bharatnaai.data.model.Salon
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.app.bharatnaai.data.session.SessionManager

class FeaturedSalonsAdapter(
    private val onSalonClick: (Salon) -> Unit
) : ListAdapter<Salon, FeaturedSalonsAdapter.SalonViewHolder>(SalonDiffCallback()) {

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

        fun bind(salon: Salon) {
            binding.apply {
                tvSalonName.text = salon.salonName
                val salonImage = salon.imagePath.trim()

                val token = SessionManager.getInstance(ivSalonImage.context).getAccessToken()
                val glideModel: Any = if (!salonImage.startsWith("http", true)) {
                    salonImage
                } else if (token != null) {
                    GlideUrl(
                        salonImage,
                        LazyHeaders.Builder()
                            .addHeader("Authorization", "Bearer $token")
                            .build()
                    )
                } else {
                    salonImage
                }

                Glide.with(ivSalonImage.context)
                    .load(glideModel)
                    .placeholder(R.drawable.saloon_image)
                    .error(R.drawable.saloon_image)
                    .into(ivSalonImage)
                
                root.setOnClickListener {
                    onSalonClick(salon)
                }
            }
        }
    }

    private class SalonDiffCallback : DiffUtil.ItemCallback<Salon>() {
        override fun areItemsTheSame(oldItem: Salon, newItem: Salon): Boolean {
            return oldItem.salonId == newItem.salonId
        }

        override fun areContentsTheSame(oldItem: Salon, newItem: Salon): Boolean {
            return oldItem == newItem
        }
    }
}
