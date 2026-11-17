package com.connectx.social.data.model

import com.google.firebase.Timestamp

data class User(
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val username: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val isAdmin: Boolean = false,
    val isBanned: Boolean = false,
    val createdAt: Timestamp? = null,
    val lastActive: Timestamp? = null,
    val fcmToken: String = "",
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val postsCount: Int = 0
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "email" to email,
        "displayName" to displayName,
        "username" to username,
        "bio" to bio,
        "photoUrl" to photoUrl,
        "isAdmin" to isAdmin,
        "isBanned" to isBanned,
        "createdAt" to createdAt,
        "lastActive" to lastActive,
        "fcmToken" to fcmToken,
        "followersCount" to followersCount,
        "followingCount" to followingCount,
        "postsCount" to postsCount
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): User = User(
            uid = map["uid"] as? String ?: "",
            email = map["email"] as? String ?: "",
            displayName = map["displayName"] as? String ?: "",
            username = map["username"] as? String ?: "",
            bio = map["bio"] as? String ?: "",
            photoUrl = map["photoUrl"] as? String ?: "",
            isAdmin = map["isAdmin"] as? Boolean ?: false,
            isBanned = map["isBanned"] as? Boolean ?: false,
            createdAt = map["createdAt"] as? Timestamp,
            lastActive = map["lastActive"] as? Timestamp,
            fcmToken = map["fcmToken"] as? String ?: "",
            followersCount = (map["followersCount"] as? Long)?.toInt() ?: 0,
            followingCount = (map["followingCount"] as? Long)?.toInt() ?: 0,
            postsCount = (map["postsCount"] as? Long)?.toInt() ?: 0
        )
    }
}
