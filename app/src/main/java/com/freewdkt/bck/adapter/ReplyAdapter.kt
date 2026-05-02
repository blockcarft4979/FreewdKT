package com.freewdkt.bck.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.freewdkt.bck.R
import com.freewdkt.bck.data.Reply
import com.freewdkt.bck.requestconstants.ApiConstants
import com.freewdkt.bck.utils.formatRelativeTime
import com.google.android.material.imageview.ShapeableImageView

class ReplyAdapter : ListAdapter<Reply, ReplyAdapter.ViewHolder>(ReplyDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reply, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val usernameTv = itemView.findViewById<TextView>(R.id.replyUsername)
        private val dateTv = itemView.findViewById<TextView>(R.id.replyDate)
        private val contentTv = itemView.findViewById<TextView>(R.id.replyContent)
        private val replyAvatar = itemView.findViewById<ShapeableImageView>(R.id.replyAvatar)

        fun bind(reply: Reply) {
            usernameTv.text = reply.username
            dateTv.text = formatRelativeTime(reply.date)
            contentTv.text = reply.content

            val avatarUrl = reply.qq?.let { ApiConstants.userIcon(it) } ?: ""
            Glide.with(replyAvatar)
                .load(avatarUrl)
                .into(replyAvatar)
        }
    }
}

class ReplyDiffCallback : DiffUtil.ItemCallback<Reply>() {
    override fun areItemsTheSame(oldItem: Reply, newItem: Reply): Boolean {
        return oldItem.content == newItem.content && oldItem.date == newItem.date && oldItem.qq == newItem.qq
    }

    override fun areContentsTheSame(oldItem: Reply, newItem: Reply): Boolean {
        return oldItem == newItem
    }
}