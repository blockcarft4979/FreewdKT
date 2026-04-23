package com.freewdkt.bck.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.freewdkt.bck.data.Post
import com.freewdkt.bck.databinding.ItemPostBinding
import com.freewdkt.bck.requestconstants.ApiConstants
import com.freewdkt.bck.utils.formatRelativeTime

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.link == newItem.link
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}

class PostAdapter : ListAdapter<Post, PostAdapter.ViewHolder>(PostDiffCallback()) {
    var onItemClick: ((Post) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPostBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)   // 传递 onItemClick
    }

    class ViewHolder(private val binding: ItemPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(post: Post, clickListener: ((Post) -> Unit)?) {
            binding.root.setOnClickListener {
                clickListener?.invoke(post)
            }
            binding.username.text = post.username
            binding.date.text = formatRelativeTime(post.date)
            if (post.title.isNotBlank()) {
                binding.title.text = post.title
                binding.title.visibility = View.VISIBLE
            } else {
                binding.title.visibility = View.GONE
            }
            binding.avatar.load(ApiConstants.userIcon(post.qq))
            binding.content.text = post.msg
        }
    }
}