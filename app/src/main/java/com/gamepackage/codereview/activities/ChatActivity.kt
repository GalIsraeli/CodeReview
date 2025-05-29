package com.gamepackage.codereview.activities

import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gamepackage.codereview.databinding.ActivityChatBinding
import com.gamepackage.codereview.logic.ChatAdapter
import com.gamepackage.codereview.logic.Message
import com.google.firebase.Firebase
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<Message>()
    private lateinit var chat: Chat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatAdapter = ChatAdapter(messages)
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.messagesRecyclerView.adapter = chatAdapter

        val model = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.0-flash")


        chat = model.startChat(
            history = listOf(
                content(role = "user") { text("The following messages will be between you and a human, the next message will be text recognised from an immage taken on a phone , if its a code explain what it does , if its a riddle say the answer , if its anything else explain it in general") },
            )
        )

        val codeText = intent.getStringExtra("CODE_TEXT") ?: ""
        if (codeText.isNotEmpty()) {
            addMessage(codeText, isUser = true)
            sendToGemini(codeText)
        }

        binding.sendButton.setOnClickListener {
            val userInput = binding.messageInput.text.toString().trim()
            if (userInput.isNotEmpty()) {
                addMessage(userInput, isUser = true)
                binding.messageInput.text.clear()
                sendToGemini(userInput)
            }
        }
    }

    private fun sendToGemini(userInput: String) {
        lifecycleScope.launch {
            try {
                val response = chat.sendMessage(userInput)
                val reply = response.text ?: "No response from Gemini."
                addMessage(reply, isUser = false)
            } catch (e: Exception) {
                addMessage("Error: ${e.message}", isUser = false)
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(Message(text, isUser))
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null && ev.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is EditText) {
                val outRect = android.graphics.Rect()
                view.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    view.clearFocus()
                    val imm = getSystemService<InputMethodManager>()
                    imm?.hideSoftInputFromWindow(view.windowToken, HIDE_NOT_ALWAYS)
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}

