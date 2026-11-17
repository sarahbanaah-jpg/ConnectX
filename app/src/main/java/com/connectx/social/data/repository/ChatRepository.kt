package com.connectx.social.data.repository

import com.connectx.social.data.model.ChatMessage
import com.connectx.social.data.model.Conversation
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ChatRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    fun getConversations(): Flow<List<Conversation>> = callbackFlow {
        val uid = auth.currentUser?.uid
        if (uid == null) { trySend(emptyList()); awaitClose { }; return@callbackFlow }
        val listener = db.collection("conversations")
            .whereArrayContains("participants", uid)
            .orderBy("lastMessageTime", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val list = snapshot?.documents?.mapNotNull { doc ->
                    try { Conversation.fromMap(doc.id, doc.data!!) } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getOrCreateConversation(otherUserId: String): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
        if (uid == otherUserId) return Result.failure(Exception("Cannot message yourself"))
        return try {
            val existing = db.collection("conversations").whereArrayContains("participants", uid).get().await()
            val found = existing.documents.firstOrNull { doc ->
                val parts = doc.get("participants") as? List<*>
                parts != null && parts.contains(otherUserId) && parts.size == 2
            }
            if (found != null) return Result.success(found.id)
            val myDoc = db.collection("users").document(uid).get().await()
            val otherDoc = db.collection("users").document(otherUserId).get().await()
            val myName = myDoc.getString("displayName") ?: "User"
            val otherName = otherDoc.getString("displayName") ?: "User"
            val myPhoto = myDoc.getString("photoUrl") ?: ""
            val otherPhoto = otherDoc.getString("photoUrl") ?: ""
            val convRef = db.collection("conversations").document()
            val data = mapOf(
                "participants" to listOf(uid, otherUserId),
                "participantNames" to mapOf(uid to myName, otherUserId to otherName),
                "participantPhotos" to mapOf(uid to myPhoto, otherUserId to otherPhoto),
                "lastMessage" to "", "lastMessageTime" to Timestamp.now(),
                "lastSenderId" to "", "unreadCount" to mapOf(uid to 0, otherUserId to 0)
            )
            convRef.set(data).await()
            Result.success(convRef.id)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun getMessages(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = db.collection("conversations").document(conversationId)
            .collection("messages").orderBy("createdAt", Query.Direction.ASCENDING).limitToLast(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val messages = snapshot?.documents?.mapNotNull { doc ->
                    try { ChatMessage.fromMap(doc.id, doc.data!!) } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(conversationId: String, content: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val userDoc = db.collection("users").document(user.uid).get().await()
            val senderName = userDoc.getString("displayName") ?: user.displayName ?: "User"
            val msgRef = db.collection("conversations").document(conversationId).collection("messages").document()
            val message = ChatMessage(
                id = msgRef.id, conversationId = conversationId, senderId = user.uid,
                senderName = senderName, content = content, createdAt = Timestamp.now()
            )
            msgRef.set(message.toMap()).await()
            val convRef = db.collection("conversations").document(conversationId)
            val convSnap = convRef.get().await()
            val participants = (convSnap.get("participants") as? List<*>)?.mapNotNull { it as? String } ?: emptyList()
            val unreadUpdates = mutableMapOf<String, Any>()
            participants.forEach { p -> if (p != user.uid) unreadUpdates["unreadCount.$p"] = FieldValue.increment(1) }
            unreadUpdates["lastMessage"] = content
            unreadUpdates["lastMessageTime"] = Timestamp.now()
            unreadUpdates["lastSenderId"] = user.uid
            convRef.update(unreadUpdates).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun markAsRead(conversationId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            db.collection("conversations").document(conversationId).update("unreadCount.$uid", 0).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
