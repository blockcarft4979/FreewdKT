package com.freewdkt.bck.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.freewdkt.bck.R
import com.freewdkt.bck.data.ZoneItem
import com.freewdkt.bck.databinding.ItemZoneBinding
import com.freewdkt.bck.requestconstants.ApiConstants

class ZoneAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_ITEM = 1
    }

    private var zones: List<ZoneItem> = emptyList()
    var onItemClick: ((ZoneItem) -> Unit)? = null

    fun submitList(newZones: List<ZoneItem>) {
        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize() = zones.size
            override fun getNewListSize() = newZones.size
            override fun areItemsTheSame(oldPos: Int, newPos: Int) =
                zones[oldPos].zone == newZones[newPos].zone
            override fun areContentsTheSame(oldPos: Int, newPos: Int) =
                zones[oldPos] == newZones[newPos]
        })
        zones = newZones
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_ITEM
    }

    override fun getItemCount(): Int = zones.size + 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_home_header, parent, false)
                HeaderViewHolder(view)
            }

            else -> {
                val binding =
                    ItemZoneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ZoneViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ZoneViewHolder) {
            holder.bind(zones[position - 1])
        }
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    inner class ZoneViewHolder(private val binding: ItemZoneBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(zone: ZoneItem) {
            binding.zoneName.text = zone.name
            binding.zoneDesc.text = zone.description ?: ""
            val iconUrl = ApiConstants.BASE_URL + (zone.icon ?: "")
            Glide.with(binding.zoneIcon)
                .load(iconUrl)
                .into(binding.zoneIcon)
            binding.root.setOnClickListener {
                onItemClick?.invoke(zone)
            }
        }
    }
}
