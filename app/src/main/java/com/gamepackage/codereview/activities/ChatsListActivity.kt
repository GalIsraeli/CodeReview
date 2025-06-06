// app/src/main/java/com/gamepackage/codereview/activities/ChatsListActivity.kt

package com.gamepackage.codereview.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gamepackage.codereview.adapters.ChatsListAdapter
import com.gamepackage.codereview.databinding.ActivityChatsListBinding
import com.gamepackage.codereview.logic.FirestoreHelper

class ChatsListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatsListBinding
    private lateinit var adapter: ChatsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatsListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Set up RecyclerView with a LinearLayoutManager
        adapter = ChatsListAdapter { selectedChatId ->
            // On row click: open ChatActivity in view-only mode
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("CHAT_ID", selectedChatId)
            startActivity(intent)
        }
        binding.chatsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.chatsRecyclerView.adapter = adapter

        // 2) Fetch all chats for this user
        FirestoreHelper.fetchAllChats { chatMetaList ->
            runOnUiThread {
                if (chatMetaList != null) {
                    adapter.submitList(chatMetaList)
                }
            }
        }

        // 3) FloatingActionButton: start a brand-new chat (go to MainActivity)
        binding.newChatFab.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-fetch whenever the activity comes back into view
        FirestoreHelper.fetchAllChats { chatMetaList ->
            runOnUiThread {
                if (chatMetaList != null) {
                    adapter.submitList(chatMetaList)
                }
            }
        }
    }
}
