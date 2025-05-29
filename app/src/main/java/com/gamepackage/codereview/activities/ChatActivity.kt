package com.gamepackage.codereview.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gamepackage.codereview.databinding.ActivityChatBinding
import com.gamepackage.codereview.logic.ChatAdapter
import com.gamepackage.codereview.logic.Message


class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val codeText = intent.getStringExtra("CODE_TEXT") ?: ""

        chatAdapter = ChatAdapter(messages)
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.messagesRecyclerView.adapter = chatAdapter

        if (codeText.isNotEmpty()) {
            addMessage(codeText, isUser = true)
            simulateGeminiResponse(codeText)
        }

        binding.sendButton.setOnClickListener {
            val userInput = binding.messageInput.text.toString()
            if (userInput.isNotEmpty()) {
                addMessage(userInput, isUser = true)
                binding.messageInput.text.clear()
                simulateGeminiResponse(userInput)
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(Message(text, isUser))
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
    }

    // Temp mock for Gemini response — replace this later with your API call
    private fun simulateGeminiResponse(userInput: String) {
        val responseText = "Gemini: Here's an explanation of your code or reply."
        addMessage(responseText, isUser = false)
    }
}