package com.example.whatsapp_clone.data.repository

import com.example.whatsapp_clone.data.model.Chat
import com.example.whatsapp_clone.data.model.Message
import com.example.whatsapp_clone.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseRepository {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    val currentUserId: String?
        get() = auth.currentUser?.uid

    val currentUserPhone: String?
        get() = auth.currentUser?.phoneNumber

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun signOut() {
        auth.signOut()
    }

    // --- User Profile ---
    fun saveUserProfile(user: User, onComplete: (Boolean) -> Unit) {
        val uid = currentUserId ?: return onComplete(false)
        val userToSave = user.copy(uid = uid)
        firestore.collection("users")
            .document(uid)
            .set(userToSave)
            .addOnSuccessListener { onComplete(true) }
            .addOnFailureListener { onComplete(false) }
    }

    fun getCurrentUserProfile(): Flow<User?> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                val user = snapshot?.toObject(User::class.java)
                trySend(user)
            }

        awaitClose { listener.remove() }
    }

    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val uid = currentUserId
        val listener = firestore.collection("users")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val users = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(User::class.java)
                }?.filter { it.uid != uid } ?: emptyList()
                trySend(users)
            }

        awaitClose { listener.remove() }
    }

    // --- Deterministic Chat ID ---
    fun getChatId(user1: String, user2: String): String {
        return if (user1 < user2) "${user1}_${user2}" else "${user2}_${user1}"
    }

    // --- Messaging ---
    fun sendMessage(chatId: String, receiverId: String, text: String, onComplete: (Boolean) -> Unit = {}) {
        val senderId = currentUserId ?: return onComplete(false)
        val messageDoc = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .document()

        val timestamp = System.currentTimeMillis()
        val message = Message(
            messageId = messageDoc.id,
            senderId = senderId,
            receiverId = receiverId,
            text = text,
            timestamp = timestamp,
            isRead = false
        )

        messageDoc.set(message).addOnSuccessListener {
            // Update the top-level chat document for the home screen list
            val chatUpdate = mapOf(
                "chatId" to chatId,
                "participants" to listOf(senderId, receiverId),
                "lastMessage" to text,
                "lastMessageTimestamp" to timestamp
            )
            firestore.collection("chats").document(chatId).set(chatUpdate)
                .addOnSuccessListener { onComplete(true) }
                .addOnFailureListener { onComplete(false) }
        }.addOnFailureListener {
            onComplete(false)
        }
    }

    fun getMessages(chatId: String): Flow<List<Message>> = callbackFlow {
        val listener = firestore.collection("chats")
            .document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(Message::class.java)
                } ?: emptyList()
                trySend(messages)
            }

        awaitClose { listener.remove() }
    }

    fun getChats(): Flow<List<Chat>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener = firestore.collection("chats")
            .whereArrayContains("participants", uid)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val chats = snapshot?.documents?.mapNotNull { doc ->
                    val chatId = doc.getString("chatId") ?: doc.id
                    val participants = (doc.get("participants") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                    val lastMessage = doc.getString("lastMessage") ?: ""
                    val lastMessageTimestamp = doc.getLong("lastMessageTimestamp") ?: 0L
                    val otherUserId = participants.firstOrNull { it != uid } ?: ""

                    Chat(
                        chatId = chatId,
                        participants = participants,
                        lastMessage = lastMessage,
                        lastMessageTimestamp = lastMessageTimestamp,
                        otherUserId = otherUserId,
                        otherUserName = "Contact", // Will resolve with user profile
                        unreadCount = 0
                    )
                } ?: emptyList()
                trySend(chats)
            }

        awaitClose { listener.remove() }
    }
}

