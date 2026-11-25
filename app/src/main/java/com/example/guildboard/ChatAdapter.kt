package com.example.guildboard

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val messages: MutableList<ChatMessage>
) : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSender: TextView = itemView.findViewById(R.id.tvMessageSender)
        val tvTime: TextView = itemView.findViewById(R.id.tvMessageTime)
        val tvText: TextView = itemView.findViewById(R.id.tvMessageText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val msg = messages[position]
        holder.tvSender.text = msg.senderName
        holder.tvTime.text = msg.time
        holder.tvText.text = msg.messageText

        if (msg.isSystem) {
            holder.tvSender.setTextColor(Color.parseColor("#3A82F6"))
            holder.tvText.setTextColor(Color.parseColor("#B0B0B0"))
        } else {
            holder.tvSender.setTextColor(Color.parseColor("#E5A93C"))
            holder.tvText.setTextColor(Color.parseColor("#FFFFFF"))
        }
    }

    override fun getItemCount(): Int = messages.size

    fun addMessage(msg: ChatMessage) {
        messages.add(msg)
        notifyItemInserted(messages.size - 1)
    }
}