package com.example.bookpath

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.bookpath.R

// Constants for different message bubble layouts
private const val USER_MESSAGE = 0
private const val AI_MESSAGE = 1

/**
 * Adapter for the RecyclerView to display ChatMessage objects.
 */
class MessageAdapter(private var messages: List<ChatMessage> = emptyList()) :
    RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    // Helper function to update the list of messages and refresh the RecyclerView
    fun submitList(newMessages: List<ChatMessage>) {
        messages = newMessages
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isUser) USER_MESSAGE else AI_MESSAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val layoutId = when (viewType) {
            USER_MESSAGE -> R.layout.user_message // Layout for user's message bubble
            AI_MESSAGE -> R.layout.ai_message   // Layout for AI's message bubble
            else -> throw IllegalArgumentException("Invalid view type")
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount(): Int = messages.size

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Assume the message text view ID is 'messageText' in both layouts
        private val messageTextView: TextView = itemView.findViewById(R.id.messageText)

        // Assume the AI pending indicator (e.g., a progress bar) ID is 'loadingIndicator'
        // Only visible in the AI message layout
        private val loadingIndicator: View? = itemView.findViewById(R.id.loadingIndicator)

        fun bind(message: ChatMessage) {
            messageTextView.text = message.text

            // Show/hide the loading indicator only for AI messages that are pending
            if (loadingIndicator != null) {
                loadingIndicator.isVisible = message.isPending
                // Hide the text content if loading, to only show the indicator
                messageTextView.isVisible = !message.isPending
                // Display a placeholder text if loading and text is visible (optional)
                if (message.isPending) messageTextView.text = "Gemini is thinking..."
            }
        }
    }
}
