package com.example.bookpath

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isPending: Boolean = false // Default to false, mainly used for AI responses
)
