package com.gamepackage.codereview.logic

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A singleton object that wraps Firestore operations for per-user chat storage.
 *
 * Firestore structure:
 *   users (collection)
 *     └─ {uid} (document)
 *         └─ chats (sub-collection)
 *             └─ {chatId} (document)
 *                 • "title": String
 *                 • "timestamp": Timestamp
 *                 • "isFavorited": Boolean
 *                 • "messages": List<Map<String,Any>>
 */
object FirestoreHelper {
    @Suppress("StaticFieldLeak")
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /** Returns null if no user is signed in. Otherwise returns the CollectionReference for "users/{uid}/chats". */
    private fun userChatsCollection() = auth.currentUser?.uid
        ?.let { uid ->
            db.collection("users")
                .document(uid)
                .collection("chats")
        }

    /**
     * Create a new chat document with an auto-generated ID under users/{uid}/chats.
     * - title: a String (e.g. first line of code or timestamp).
     * On success, invokes onCreated(chatId). On failure, onCreated(null).
     */
    fun createNewChat(
        title: String,
        onCreated: (chatId: String?) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onCreated(null)
            return
        }

        val newChatRef = db
            .collection("users")
            .document(uid)
            .collection("chats")
            .document() // auto-ID

        val data = mapOf(
            "title" to title,
            "timestamp" to Timestamp.now(),
            "isFavorited" to false,
            "messages" to listOf<Map<String, Any>>()
        )

        newChatRef.set(data)
            .addOnSuccessListener { onCreated(newChatRef.id) }
            .addOnFailureListener {
                onCreated(null)
            }
    }

    /**
     * Append a new message to an existing chat’s “messages” array.
     * - chatId: the Firestore doc ID under users/{uid}/chats
     * - messageText: the message content
     * - isUser: true if it was sent by the user, false if by AI
     * On completion, invokes onComplete(success).
     */
    fun addMessageToChat(
        chatId: String,
        messageText: String,
        isUser: Boolean,
        onComplete: (success: Boolean) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onComplete(false)
            return
        }

        val chatDocRef = db
            .collection("users")
            .document(uid)
            .collection("chats")
            .document(chatId)

        val newMessage = mapOf(
            "text" to messageText,
            "isUser" to isUser,
            "sentAt" to Timestamp.now()
        )

        // Atomically append the map into the “messages” array
        chatDocRef.update("messages", FieldValue.arrayUnion(newMessage))
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun setChatFavorited(
        chatId: String,
        isFav: Boolean,
        onComplete: (success: Boolean) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onComplete(false)
            return
        }

        val userChatRef = db
            .collection("users")
            .document(uid)
            .collection("chats")
            .document(chatId)

        userChatRef.update("isFavorited", isFav)
            .addOnSuccessListener {
                if (isFav) {
                    // Copy into /favorites/{uid}_{chatId}
                    userChatRef.get()
                        .addOnSuccessListener { doc ->
                            val data = doc.data
                            if (data != null) {
                                val favoriteData = hashMapOf(
                                    "ownerUid" to uid,
                                    "chatId" to chatId,
                                    "title" to (data["title"] as? String ?: ""),
                                    "timestamp" to (data["timestamp"] as? com.google.firebase.Timestamp
                                        ?: com.google.firebase.Timestamp.now()),
                                    "messages" to (data["messages"] as? List<Map<String, Any>>
                                        ?: emptyList()),
                                )
                                db.collection("favorites")
                                    .document("${uid}_$chatId")
                                    .set(favoriteData)
                                    .addOnSuccessListener { onComplete(true) }
                                    .addOnFailureListener { e ->
                                        // Log the exception
                                        Log.e("FirestoreHelper", "Failed to COPY to favorites: ${e.message}", e)
                                        onComplete(false)
                                    }
                            } else {
                                Log.e("FirestoreHelper", "Chat document unexpectedly had no data while favoriting.")
                                onComplete(false)
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreHelper", "Failed to FETCH chat doc for copying: ${e.message}", e)
                            onComplete(false)
                        }
                } else {
                    // Remove from /favorites/{uid}_{chatId}
                    db.collection("favorites")
                        .document("${uid}_$chatId")
                        .delete()
                        .addOnSuccessListener { onComplete(true) }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreHelper", "Failed to DELETE from favorites: ${e.message}", e)
                            onComplete(false)
                        }
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreHelper", "Failed to UPDATE isFavorited in user chat: ${e.message}", e)
                onComplete(false)
            }
    }


    /**
     * Fetch all chats for the signed-in user, ordered by timestamp descending.
     * Returns a list of maps, each containing:
     *  • "chatId": String
     *  • "title": String
     *  • "timestamp": com.google.firebase.Timestamp
     *  • "isFavorited": Boolean
     *
     * On completion, invokes onFetched(list) or onFetched(null) on failure.
     */
    fun fetchAllChats(onFetched: (List<Map<String, Any>>?) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onFetched(null)
            return
        }

        db.collection("users")
            .document(uid)
            .collection("chats")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val result = querySnapshot.documents.map { doc ->
                    val data = doc.data ?: emptyMap<String, Any>()
                    mapOf(
                        "chatId" to doc.id,
                        "title" to (data["title"] as? String ?: ""),
                        "timestamp" to (data["timestamp"] as? com.google.firebase.Timestamp
                            ?: com.google.firebase.Timestamp.now()),
                        "isFavorited" to (data["isFavorited"] as? Boolean ?: false)
                    )
                }
                onFetched(result)
            }
            .addOnFailureListener {
                onFetched(null)
            }
    }

    /**
     * Fetch the “messages” array from a specific chat document.
     * Returns a List<Map<String,Any>> where each map has:
     *  • "text": String
     *  • "isUser": Boolean
     *  • "sentAt": com.google.firebase.Timestamp
     *
     * On completion, invokes onFetched(messages) or onFetched(null) on failure.
     */
    fun fetchChatMessages(
        chatId: String,
        onFetched: (List<Map<String, Any>>?) -> Unit
    ) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onFetched(null)
            return
        }

        db.collection("users")
            .document(uid)
            .collection("chats")
            .document(chatId)
            .get()
            .addOnSuccessListener { docSnapshot ->
                val data = docSnapshot.data ?: emptyMap<String, Any>()
                @Suppress("UNCHECKED_CAST")
                val messages = data["messages"] as? List<Map<String, Any>> ?: emptyList()
                onFetched(messages)
            }
            .addOnFailureListener {
                onFetched(null)
            }
    }
}
