package com.example.bookpath

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Data class to hold message content and identify the sender
data class Message(val text: String, val isFromUser: Boolean)

class ChatViewModel(apiKey: String) : ViewModel() {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val generativeModel: GenerativeModel

    init {
        // Initialize the GenerativeModel with your model name and API key
        generativeModel = GenerativeModel(
            modelName = "gemini-pro", // Or any other suitable model
            apiKey = apiKey
        )
    }

    fun sendMessage(userInput: String) {
        // Add user message to the list
        _messages.value += Message(userInput, isFromUser = true)

        // Launch a coroutine to get the response from the generative model
        viewModelScope.launch {
            try {
                val response = generativeModel.generateContent(userInput)
                response.text?.let {
                    // Add model's response to the list
                    _messages.value += Message(it, isFromUser = false)
                }
            } catch (e: Exception) {
                // Handle potential errors, e.g., network issues, API errors
                e.printStackTrace()
                _messages.value += Message("Error: ${e.message}", isFromUser = false)
            }
        }
    }
}
