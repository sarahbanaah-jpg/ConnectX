package com.connectx.social.data.model

import com.google.firebase.Timestamp

data class Post(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val content: String = "",
    val imageUrl: String = "",
    val likeCount: Int = 0,
    val commentCount: Int = 0,
    val likedBy: List<String> = emptyList(),
    val createdAt: Timestamp? = null,
    val isFlagged: Boolean = false,
    val flagReason: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "authorId" to authorId,
        "authorName" to authorName,
        "authorPhotoUrl" to authorPhotoUrl,
        "content" to content,
        "imageUrl" to imageUrl,
        "likeCount" to likeCount,
        "commentCount" to commentCount,
        "likedBy" to likedBy,
        "createdAt" to createdAt,
        "isFlagged" to isFlagged,
        "flagReason" to flagReason
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Post = Post(
            id = id,
            authorId = map["authorId"] as? String ?: "",
            authorName = map["authorName"] as? String ?: "",
            authorPhotoUrl = map["authorPhotoUrl"] as? String ?: "",
            content = map["content"] as? String ?: "",
            imageUrl = map["imageUrl"] as? String ?: "",
            likeCount = (map["likeCount"] as? Long)?.toInt() ?: 0,
            commentCount = (map["commentCount"] as? Long)?.toInt() ?: 0,
            likedBy = (map["likedBy"] as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
            createdAt = map["createdAt"] as? Timestamp,
            isFlagged = map["isFlagged"] as? Boolean ?: false,
            flagReason = map["flagReason"] as? String ?: ""
        )
    }
}

data class Comment(
    val id: String = "",
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val content: String = "",
    val createdAt: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "postId" to postId,
        "authorId" to authorId,
        "authorName" to authorName,
        "authorPhotoUrl" to authorPhotoUrl,
        "content" to content,
        "createdAt" to createdAt
    )

    companion object {
        fun fromMap(id: String, map: Map<String, Any?>): Comment = Comment(
            id = id,
            postId = map["postId"] as? String ?: "",
            authorId = map["authorId"] as? String ?: "",
            authorName = map["authorName"] as? String ?: "",
            authorPhotoUrl = map["authorPhotoUrl"] as? String ?: "",
            content = map["content"] as? String ?: "",
            createdAt = map["createdAt"] as? Timestamp
        )
    }
}
