package com.gamepackage.codereview.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.gamepackage.codereview.logic.FavoritesListAdapter
import com.gamepackage.codereview.databinding.ActivityFavoritesListBinding
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFavoritesListBinding
    private lateinit var adapter: FavoritesListAdapter
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFavoritesListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1) Setup RecyclerView
        adapter = FavoritesListAdapter { favoritesDocId ->
            // Open FavoriteChatActivity to display this chat
            val intent = Intent(this, FavoriteChatActivity::class.java)
            intent.putExtra("FAVORITES_DOC_ID", favoritesDocId)
            startActivity(intent)
        }
        binding.favoritesRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.favoritesRecyclerView.adapter = adapter

        // 2) Fetch all documents under top-level "favorites" collection
        db.collection("favorites")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val list = querySnapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null

                    // Build a MutableMap<String, Any> with the keys adapter expects:
                    val favMap: MutableMap<String, Any> = mutableMapOf()
                    favMap["favoritesDocId"] = doc.id
                    favMap["ownerUid"] = data["ownerUid"] as? String ?: ""
                    favMap["chatId"] = data["chatId"] as? String ?: ""
                    favMap["title"] = data["title"] as? String ?: ""

                    // Handle timestamp safely
                    val tsAny = data["timestamp"]
                    val ts = if (tsAny is Timestamp) tsAny else Timestamp.now()
                    favMap["timestamp"] = ts

                    // Handle messages array safely
                    @Suppress("UNCHECKED_CAST")
                    val msgs = data["messages"] as? List<Map<String, Any>> ?: emptyList()
                    favMap["messages"] = msgs

                    favMap
                }
                adapter.submitList(list)
            }
            .addOnFailureListener {
                // Optionally show an error message or leave the list empty
            }
    }
}
