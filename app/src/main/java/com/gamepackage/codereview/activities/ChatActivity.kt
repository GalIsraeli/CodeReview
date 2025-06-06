package com.gamepackage.codereview.activities

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gamepackage.codereview.databinding.ActivityChatBinding
import com.gamepackage.codereview.logic.ChatAdapter
import com.gamepackage.codereview.logic.FirestoreHelper
import com.gamepackage.codereview.logic.Message
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.ai.Chat
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import kotlinx.coroutines.launch

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private val messages = mutableListOf<Message>()

    private var chatId: String? = null
    private var isNewChat = false

    private lateinit var chat: Chat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) RecyclerView setup
        chatAdapter = ChatAdapter(messages)
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.messagesRecyclerView.adapter = chatAdapter

        // 2) Firebase AI model
        val model = Firebase.ai(backend = GenerativeBackend.googleAI())
            .generativeModel("gemini-2.0-flash")
        chat = model.startChat(
            history = listOf(
                content(role = "user") {
                    text(
                        "The following messages will be between you and a human. " +
                                "The next message comes from recognized text: if it’s code, explain it; if a riddle, give the answer; else explain generally."
                    )
                }
            )
        )

        // 3) Intent extras
        val initialCodeText = intent.getStringExtra("CODE_TEXT") ?: ""
        val passedChatId = intent.getStringExtra("CHAT_ID")

        if (passedChatId != null) {
            // VIEW‐ONLY existing chat
            chatId = passedChatId
            loadExistingChat(passedChatId)

            // Hide the EditText and Send button
            binding.messageInput.visibility = View.GONE
            binding.sendButton.visibility = View.GONE

        } else {
            // NEW chat: create in Firestore
            isNewChat = true
            val title = initialCodeText
                .lineSequence()
                .firstOrNull()
                ?.take(30)
                ?: "Chat_${Timestamp.now().seconds}"

            FirestoreHelper.createNewChat(title) { newId ->
                if (newId != null) {
                    chatId = newId
                    if (initialCodeText.isNotEmpty()) {
                        addMessage(initialCodeText, isUser = true)
                        saveMessageToFirestore(initialCodeText, isUser = true)
                        sendToGemini(initialCodeText)
                    }
                } else {
                    finish()
                }
            }

            binding.sendButton.setOnClickListener {
                val userInput = binding.messageInput.text.toString().trim()
                if (userInput.isNotEmpty() && chatId != null) {
                    addMessage(userInput, isUser = true)
                    saveMessageToFirestore(userInput, isUser = true)
                    binding.messageInput.text.clear()
                    sendToGemini(userInput)
                }
            }
        }
    }

    private fun loadExistingChat(existingId: String) {
        FirestoreHelper.fetchChatMessages(existingId) { messagesList ->
            runOnUiThread {
                messagesList?.forEach { msgMap ->
                    val text = msgMap["text"] as? String ?: ""
                    val isUser = msgMap["isUser"] as? Boolean ?: false
                    addMessage(text, isUser)
                }
            }
        }
    }

    private fun addMessage(text: String, isUser: Boolean) {
        messages.add(Message(text, isUser))
        chatAdapter.notifyItemInserted(messages.size - 1)
        binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
    }

    private fun saveMessageToFirestore(text: String, isUser: Boolean) {
        chatId?.let { id ->
            FirestoreHelper.addMessageToChat(id, text, isUser) {
                // optional: handle success/failure
            }
        }
    }

    private fun sendToGemini(userInput: String) {
        lifecycleScope.launch {
            try {
                val response = chat.sendMessage(userInput)
                val reply = response.text ?: "No response from Gemini."
                addMessage(reply, isUser = false)
                saveMessageToFirestore(reply, isUser = false)
            } catch (e: Exception) {
                addMessage("Error: ${e.message}", isUser = false)
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null && ev.action == MotionEvent.ACTION_DOWN) {
            val view = currentFocus
            if (view is android.widget.EditText) {
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
