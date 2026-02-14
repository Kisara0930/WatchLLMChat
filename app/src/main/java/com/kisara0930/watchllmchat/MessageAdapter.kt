package com.kisara0930.watchllmchat

import android.graphics.Color
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.kisara0930.watchllmchat.databinding.ItemMessageBinding

class MessageAdapter : ListAdapter<Message, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(private val binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: Message) {
            if (message.isLoading) {
                binding.messageTextView.visibility = View.GONE
                binding.loadingIndicator.visibility = View.VISIBLE
            } else {
                binding.messageTextView.visibility = View.VISIBLE
                binding.loadingIndicator.visibility = View.GONE
                binding.messageTextView.text = message.text

                if (message.author == "user") {
                    binding.messageTextView.setTextColor(Color.parseColor("#BDBDBD")) // Light Grey
                    binding.messageTextView.setTypeface(null, Typeface.ITALIC)
                } else {
                    binding.messageTextView.setTextColor(Color.WHITE)
                    binding.messageTextView.setTypeface(null, Typeface.NORMAL)
                }
            }
        }
    }
}

class MessageDiffCallback : DiffUtil.ItemCallback<Message>() {
    override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
        return oldItem == newItem
    }
}