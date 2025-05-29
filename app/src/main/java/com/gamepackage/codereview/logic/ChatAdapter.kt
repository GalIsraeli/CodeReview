package com.gamepackage.codereview.logic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gamepackage.codereview.R

class ChatAdapter(private val messages: List<Message>) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val messageText: TextView = itemView.findViewById(R.id.messageText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messages[position]
        holder.messageText.text = message.text

        // Position bubble: align left or right
        val params = holder.messageText.layoutParams as ViewGroup.MarginLayoutParams
        if (message.isUser) {
            holder.messageText.setBackgroundResource(R.drawable.message_bg_user)
            params.marginStart = 64
            params.marginEnd = 0
        } else {
            holder.messageText.setBackgroundResource(R.drawable.message_bg)
            params.marginStart = 0
            params.marginEnd = 64
        }
        holder.messageText.layoutParams = params
    }

    override fun getItemCount() = messages.size
}