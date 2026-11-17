package com.connectx.social.data.model

import com.google.firebase.Timestamp

data class Conversation(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantPhotos: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageTime: Timestamp? = null,
    val lastSenderId: String = "",
    val unreadCount: Map<String, Int> = emptyMap()
) {
    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Conversation {
            @Suppress("UNCHECKED_CAST")
            val names = map["participantNames"] as? Map<String, String> ?: emptyMap()
            @Suppress("UNCHECKED_CAST")
            val photos = map["participantPhotos"] as? Map<String, String> ?: emptyMap()
            @Suppress("UNCHECKED_CAST")
            val unread = (map["unreadCount"] as? Map<String, Long>)?.mapValues { it.value.toInt() } ?: emptyMap()
            return Conversation(
                id = id,
                participants = (map["participants"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                participantNames = names,
                participantPhotos = photos,
                lastMessage = map["lastMessage"] as? String ?: "",
                lastMessageTime = map["lastMessageTime"] as? Timestamp,
                lastSenderId = map["lastSenderId"] as? String ?: "",
                unreadCount = unread
            )
        }
    }
}

data class ChatMessage(
    val id: String = "",
    val conversationId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val createdAt: Timestamp? = null,
    val isRead: Boolean = false
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "conversationId" to conversationId,
        "senderId" to senderId,
        "senderName" to senderName,
        "content" to content,
        "imageUrl" to imageUrl,
        "createdAt" to createdAt,
        "isRead" to isRead
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): ChatMessage = ChatMessage(
            id = id,
            conversationId = map["conversationId"] as? String ?: "",
            senderId = map["senderId"] as? String ?: "",
            senderName = map["senderName"] as? String ?: "",
            content = map["content"] as? String ?: "",
            imageUrl = map["imageUrl"] as? String ?: "",
            createdAt = map["createdAt"] as? Timestamp,
            isRead = map["isRead"] as? Boolean ?: false
        )
    }
}
