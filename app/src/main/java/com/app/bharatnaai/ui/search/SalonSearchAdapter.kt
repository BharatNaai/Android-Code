package com.app.bharatnaai.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bharatnaai.R
import bharatnaai.databinding.ItemSalonSearchBinding
import com.app.bharatnaai.data.model.Salon
import com.app.bharatnaai.utils.CommonMethod
import com.bumptech.glide.Glide

class SalonSearchAdapter(
    private val onBookNowClick: (Salon) -> Unit
) : ListAdapter<Salon, SalonSearchAdapter.SalonViewHolder>(SalonDiffCallback()) {

    private val commonMethod = CommonMethod()

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
                tvSalonName.text = salon.salonName
//                tvRating.text = salon.rating.toString()
                val distanceText = String.format("%.2f km", salon.distance ?: 0.0)
                tvDistance.text = distanceText
                val salonImage = salon.imagePath?.trim()

                if (salonImage.isNullOrEmpty()) {
                    Glide.with(ivSalonImage.context)
                        .load(R.drawable.saloon_image)
                        .into(ivSalonImage)
                } else {
                    commonMethod.loadImage(binding.ivSalonImage, salon.imagePath)
                }

                btnBookNow.setOnClickListener {
                    onBookNowClick(salon)
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
