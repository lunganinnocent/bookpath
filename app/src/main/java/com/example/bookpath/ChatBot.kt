package com.example.bookpath

import android.os.Bundle
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.viewModels
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.ai.client.generativeai.GenerativeModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookpath.MessageAdapter
import com.example.bookpath.databinding.UserMessageBinding
import kotlinx.coroutines.launch
import java.util.Properties
class ChatBot : AppCompatActivity() {

    // We will use a custom factory to inject the API key into the ViewModel
    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(getApiKeyFromProperties())
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageInput: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_chat_bot) // Assuming your main layout is named activity_chat_bot.xml


        recyclerView = findViewById(R.id.RecycleView)
        messageInput = findViewById(R.id.edit)


        messageAdapter = MessageAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ChatBot)
            adapter = messageAdapter
        }


        // The send button is the 'drawableEnd' of the EditText. We use a TouchListener.
        messageInput.setOnTouchListener { v, event ->
            if (event.action == android.view.MotionEvent.ACTION_UP) {
                // Check if the click was on the drawableEnd (send icon)
                if (event.rawX >= (messageInput.right - messageInput.compoundDrawables[2].bounds.width())) {
                    val message = messageInput.text.toString().trim()
                    if (message.isNotEmpty()) {
                        viewModel.sendMessage(message)
                        messageInput.setText("") // Clear the input field
                        // Scroll to the bottom when a new message is sent
                        recyclerView.scrollToPosition(messageAdapter.itemCount - 1)
                    }
                    return@setOnTouchListener true
                }
            }
            return@setOnTouchListener false
        }


        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.messages.collect { messages ->
                    // Convert the List<Message> to a List<ChatMessage>
                    val chatMessages = messages.map { message ->
                        ChatMessage(
                            text = message.text,
                            isUser = message.text == "user", // Assumes author is "user" for the user's messages
                            isPending = false // Default pending state to false
                        )
                    }

                    // Submit the correctly typed list to the adapter
                    messageAdapter.submitList(chatMessages)

                    // Scroll to the bottom on every update
                    if (chatMessages.isNotEmpty()) {
                        recyclerView.scrollToPosition(chatMessages.size - 1)
                    }
                }
            }
        }




    }

    /**
     * Reads the API Key securely from the local.properties file using reflection/properties.
     * NOTE: This is for development only. For production, secure keys using a backend or Vault.
     */
    private fun getApiKeyFromProperties(): String {
        return try {
            val properties = Properties()
            assets.open("local.properties").use {
                properties.load(it)
            }

            properties.getProperty("apiKey") ?: throw Exception("API Key not found in properties.")
        } catch (e: Exception) {
            e.printStackTrace()

            "AIzaSyDwRs-iM0lPCqbctZOF4dtjLICphXCCyM0"
        }
    }
}

// Factory to allow the ViewModel to be initialized with the API key
class ChatViewModelFactory(private val apiKey: String) : androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(apiKey) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}




