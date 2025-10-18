package com.app.bharatnaai.ui.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bharatnaai.R
import bharatnaai.databinding.ItemSalonSearchBinding
import com.bumptech.glide.Glide
import com.app.bharatnaai.data.model.Salon
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.model.LazyHeaders
import com.app.bharatnaai.data.session.SessionManager
import com.app.bharatnaai.utils.Constants

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
                tvSalonName.text = salon.salonName
//                tvRating.text = salon.rating.toString()
                val distanceText = String.format("%.2f km", salon.distance ?: 0.0)
                tvDistance.text = distanceText

                val salonImage = salon.imagePath.trim()
                // If backend accidentally concatenated multiple URLs separated by whitespace, take the last token
                val tokenized = salonImage.split(Regex("\\s+")).lastOrNull()?.trim().orEmpty()
                val token = SessionManager.getInstance(ivSalonImage.context).getAccessToken()

                // Build absolute URL for relative paths
                val effectiveUrl = if (!tokenized.startsWith("http", true)) {
                    Constants.BASE_URL.trim().trimEnd('/') + "/" + tokenized.trimStart('/')
                } else {
                    tokenized
                }

                // Attach Authorization header for protected http(s) URLs
                val glideModel: Any = if (effectiveUrl.startsWith("http", true) && token != null) {
                    GlideUrl(
                        effectiveUrl,
                        LazyHeaders.Builder()
                            .addHeader("Authorization", "Bearer $token")
                            .build()
                    )
                } else {
                    effectiveUrl
                }

                Glide.with(ivSalonImage.context)
                    .load(glideModel)
                    .placeholder(R.drawable.saloon_image)
                    .error(R.drawable.saloon_image)
                    .into(ivSalonImage)

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
            return oldItem.salonId == newItem.salonId
        }

        override fun areContentsTheSame(oldItem: Salon, newItem: Salon): Boolean {
            return oldItem == newItem
        }
    }
}
