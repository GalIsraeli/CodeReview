package com.gamepackage.codereview.logic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gamepackage.codereview.databinding.ItemChatListBinding
import com.gamepackage.codereview.utils.TimeFormatter
import com.google.firebase.Timestamp

/**
 * Adapter for displaying all documents under the top-level "favorites" collection.
 * Each item is a Map<String, Any> containing:
 *  • "ownerUid": String
 *  • "chatId": String
 *  • "title": String
 *  • "timestamp": com.google.firebase.Timestamp
 *  • "messages": List<Map<String,Any>>
 *
 * On item click, we’ll pass the favoritesDocId ("{ownerUid}_{chatId}") to FavoriteChatActivity.
 */
class FavoritesListAdapter(
    private val onClick: (favoritesDocId: String) -> Unit
) : ListAdapter<Map<String, Any>, FavoritesListAdapter.FavViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Map<String, Any>>() {
            override fun areItemsTheSame(
                oldItem: Map<String, Any>, newItem: Map<String, Any>
            ) = (oldItem["favoritesDocId"] as String) == (newItem["favoritesDocId"] as String)

            override fun areContentsTheSame(
                oldItem: Map<String, Any>, newItem: Map<String, Any>
            ) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavViewHolder {
        val binding = ItemChatListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: FavViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FavViewHolder(
        private val binding: ItemChatListBinding,
        private val onClick: (favoritesDocId: String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentData: Map<String, Any>? = null

        init {
            binding.root.setOnClickListener {
                currentData?.let { data ->
                    val favoritesDocId = data["favoritesDocId"] as String
                    onClick(favoritesDocId)
                }
            }
            // We do not display a favorite button here because these are already favorites.
            binding.favoriteButton.visibility = android.view.View.GONE
        }

        fun bind(data: Map<String, Any>) {
            currentData = data
            val title = data["title"] as? String ?: ""
            val ts = data["timestamp"] as Timestamp

            binding.chatTitle.text = title
            val ms = ts.seconds * 1000 + ts.nanoseconds / 1_000_000
            binding.chatTimestamp.text = TimeFormatter.formatTime(ms)
        }
    }
}
