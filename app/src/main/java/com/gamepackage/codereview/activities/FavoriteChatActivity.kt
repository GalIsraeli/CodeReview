package com.gamepackage.codereview.activities

import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gamepackage.codereview.databinding.ActivityChatBinding
import com.gamepackage.codereview.logic.ChatAdapter
import com.gamepackage.codereview.logic.Message
import com.google.firebase.firestore.FirebaseFirestore

class FavoriteChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var chatAdapter: ChatAdapter

    // 1) Define a mutable list of Message
    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 2) Initialize ChatAdapter with that mutable list
        chatAdapter = ChatAdapter(messages)
        binding.messagesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.messagesRecyclerView.adapter = chatAdapter

        // Disable input and send-button (view-only)
        binding.messageInput.isEnabled = false
        binding.sendButton.isEnabled = false
        binding.sendButton.visibility = android.view.View.GONE
        binding.messageInput.visibility = android.view.View.GONE

        // 3) Read the passed "FAVORITES_DOC_ID"
        val favoritesDocId = intent.getStringExtra("FAVORITES_DOC_ID") ?: return

        // 4) Fetch the favorite chat document from Firestore
        FirebaseFirestore.getInstance()
            .collection("favorites")
            .document(favoritesDocId)
            .get()
            .addOnSuccessListener { docSnapshot ->
                val data = docSnapshot.data ?: return@addOnSuccessListener

                @Suppress("UNCHECKED_CAST")
                val messagesList = data["messages"] as? List<Map<String, Any>> ?: emptyList()

                // 5) For each message map, extract text & isUser, then add to "messages"
                messagesList.forEach { msgMap ->
                    val text = msgMap["text"] as? String ?: ""
                    val isUser = msgMap["isUser"] as? Boolean ?: false

                    // Instead of adapter.addMessage(...), do:
                    messages.add(Message(text, isUser))
                    chatAdapter.notifyItemInserted(messages.size - 1)
                }

                // Scroll to the bottom (optional)
                binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
            }
            .addOnFailureListener {
                // You can show a Toast or finish() here if desired
            }
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
        return super.dispatchTouchEvent(ev)
    }
}
