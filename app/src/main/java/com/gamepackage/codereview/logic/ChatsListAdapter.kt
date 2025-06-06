// app/src/main/java/com/gamepackage/codereview/adapters/ChatsListAdapter.kt
package com.gamepackage.codereview.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gamepackage.codereview.R
import com.gamepackage.codereview.databinding.ItemChatListBinding
import com.gamepackage.codereview.logic.FirestoreHelper
import com.gamepackage.codereview.utils.TimeFormatter
import com.google.firebase.Timestamp

/**
 * Each item is a Map<String, Any> containing:
 *  • "chatId": String
 *  • "title": String
 *  • "timestamp": com.google.firebase.Timestamp
 *  • "isFavorited": Boolean
 */
class ChatsListAdapter(
    private val onClick: (chatId: String) -> Unit
) : ListAdapter<Map<String, Any>, ChatsListAdapter.ChatViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Map<String, Any>>() {
            override fun areItemsTheSame(
                oldItem: Map<String, Any>,
                newItem: Map<String, Any>
            ): Boolean {
                // Compare by chatId
                return (oldItem["chatId"] as String) == (newItem["chatId"] as String)
            }

            override fun areContentsTheSame(
                oldItem: Map<String, Any>,
                newItem: Map<String, Any>
            ): Boolean {
                val oldTitle = oldItem["title"] as String
                val newTitle = newItem["title"] as String

                val oldTs = oldItem["timestamp"] as Timestamp
                val newTs = newItem["timestamp"] as Timestamp

                val oldFav = oldItem["isFavorited"] as Boolean
                val newFav = newItem["isFavorited"] as Boolean

                return oldTitle == newTitle
                        && oldTs == newTs
                        && oldFav == newFav
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChatViewHolder(binding, onClick)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    /**
     * Flip only the “isFavorited” flag at [position] and resubmit a new list.
     */
    fun toggleFavoriteAt(position: Int) {
        val oldList: List<Map<String, Any>> = currentList
        if (position < 0 || position >= oldList.size) return

        // 1. Copy the List into a MutableList
        val newMutable: MutableList<Map<String, Any>> = oldList.toMutableList()

        // 2. Copy the one Map at [position] so we don't mutate the original
        val oldItem: Map<String, Any> = oldList[position]
        val toggledItem: MutableMap<String, Any> = oldItem.toMutableMap()
        val wasFav = oldItem["isFavorited"] as? Boolean ?: false
        toggledItem["isFavorited"] = !wasFav

        // 3. Replace only that item
        newMutable[position] = toggledItem

        // 4. Submit the new List; DiffUtil will diff and re-bind that ViewHolder.
        submitList(newMutable)
    }

    inner class ChatViewHolder(
        private val binding: ItemChatListBinding,
        private val onClick: (chatId: String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private var currentData: Map<String, Any>? = null

        init {
            // Row click → open chat
            binding.root.setOnClickListener {
                currentData?.let { data ->
                    val chatId = data["chatId"] as String
                    onClick(chatId)
                }
            }

            // Star click → toggle favorite
            binding.favoriteButton.setOnClickListener {
                currentData?.let { data ->
                    val chatId = data["chatId"] as String
                    val isFav = data["isFavorited"] as Boolean

                    // 1. Optimistic UI: flip the icon immediately
                    binding.favoriteButton.setImageResource(
                        if (!isFav) R.drawable.ic_star_filled
                        else R.drawable.ic_star_outline
                    )

                    // 2. Update Firestore, then update the adapter’s list on success
                    FirestoreHelper.setChatFavorited(chatId, !isFav) { success ->
                        if (success) {
                            val pos = adapterPosition
                            if (pos != RecyclerView.NO_POSITION) {
                                this@ChatsListAdapter.toggleFavoriteAt(pos)
                                Toast.makeText(
                                    binding.root.context,
                                    if (!isFav) "Favorited" else "Unfavorited",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            // Revert icon on failure
                            binding.favoriteButton.setImageResource(
                                if (isFav) R.drawable.ic_star_filled
                                else R.drawable.ic_star_outline
                            )
                            Toast.makeText(
                                binding.root.context,
                                "Error updating favorite",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        fun bind(data: Map<String, Any>) {
            currentData = data

            binding.chatTitle.text = data["title"] as String

            val ts = data["timestamp"] as Timestamp
            val ms = ts.seconds * 1000 + ts.nanoseconds / 1_000_000
            binding.chatTimestamp.text = TimeFormatter.formatTime(ms)

            // Always set the correct star icon based on “isFavorited”
            val isFav = data["isFavorited"] as Boolean
            binding.favoriteButton.setImageResource(
                if (isFav) R.drawable.ic_star_filled
                else R.drawable.ic_star_outline
            )
        }
    }
}
