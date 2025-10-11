package com.app.bharatnaai.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import bharatnaai.databinding.ItemExclusiveOfferBinding

data class ExclusiveOffer(
    val id: String,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val discountPercentage: Int = 0
)

class ExclusiveOffersAdapter(
    private val onOfferClick: (ExclusiveOffer) -> Unit
) : ListAdapter<ExclusiveOffer, ExclusiveOffersAdapter.OfferViewHolder>(OfferDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val binding = ItemExclusiveOfferBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OfferViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class OfferViewHolder(
        private val binding: ItemExclusiveOfferBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(offer: ExclusiveOffer) {
            binding.apply {
                tvOfferTitle.text = offer.title
                tvOfferDescription.text = offer.description
                
                // TODO: Load actual image using Glide/Picasso
                // For now, using placeholder color
                
                root.setOnClickListener {
                    onOfferClick(offer)
                }
            }
        }
    }

    private class OfferDiffCallback : DiffUtil.ItemCallback<ExclusiveOffer>() {
        override fun areItemsTheSame(oldItem: ExclusiveOffer, newItem: ExclusiveOffer): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ExclusiveOffer, newItem: ExclusiveOffer): Boolean {
            return oldItem == newItem
        }
    }
}
