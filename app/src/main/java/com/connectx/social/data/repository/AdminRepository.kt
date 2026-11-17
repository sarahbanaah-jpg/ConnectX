package com.connectx.social.data.repository

import com.connectx.social.data.model.AppConfig
import com.connectx.social.data.model.Post
import com.connectx.social.data.model.SecurityFlag
import com.connectx.social.data.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AdminRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun isCurrentUserAdmin(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        return try {
            db.collection("users").document(uid).get().await().getBoolean("isAdmin") == true
        } catch (e: Exception) { false }
    }

    fun getAllUsers(): Flow<List<User>> = callbackFlow {
        val listener = db.collection("users").orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val users = snapshot?.documents?.mapNotNull { doc ->
                    try { User.fromMap(doc.data!!) } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    suspend fun banUser(uid: String, ban: Boolean): Result<Unit> {
        return try {
            db.collection("users").document(uid).update("isBanned", ban).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun setAdmin(uid: String, isAdmin: Boolean): Result<Unit> {
        return try {
            db.collection("users").document(uid).update("isAdmin", isAdmin).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun getFlaggedPosts(): Flow<List<Post>> = callbackFlow {
        val listener = db.collection("posts").whereEqualTo("isFlagged", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    try { Post.fromMap(doc.id, doc.data!!) } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(posts)
            }
        awaitClose { listener.remove() }
    }

    suspend fun unflagPost(postId: String): Result<Unit> {
        return try {
            db.collection("posts").document(postId).update(mapOf("isFlagged" to false, "flagReason" to "")).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deletePostAsAdmin(postId: String): Result<Unit> {
        return try {
            db.collection("posts").document(postId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun getSecurityFlags(): Flow<List<SecurityFlag>> = callbackFlow {
        val listener = db.collection("security_flags").orderBy("createdAt", Query.Direction.DESCENDING).limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val flags = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        val data = doc.data!!
                        SecurityFlag(
                            id = doc.id, type = data["type"] as? String ?: "",
                            targetId = data["targetId"] as? String ?: "",
                            reason = data["reason"] as? String ?: "",
                            severity = data["severity"] as? String ?: "medium",
                            reportedBy = data["reportedBy"] as? String ?: "ai_assistant",
                            createdAt = data["createdAt"] as? Timestamp,
                            resolved = data["resolved"] as? Boolean ?: false,
                            resolvedBy = data["resolvedBy"] as? String ?: ""
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(flags)
            }
        awaitClose { listener.remove() }
    }

    suspend fun resolveFlag(flagId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            db.collection("security_flags").document(flagId).update(mapOf("resolved" to true, "resolvedBy" to uid)).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getAppConfig(): Result<AppConfig> {
        return try {
            val snap = db.collection("config").document("app").get().await()
            if (snap.exists()) Result.success(AppConfig.fromMap(snap.data!!)) else Result.success(AppConfig())
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateAppConfig(config: AppConfig): Result<Unit> {
        return try {
            val data = mapOf(
                "latestVersionCode" to config.latestVersionCode,
                "latestVersionName" to config.latestVersionName,
                "forceUpdate" to config.forceUpdate,
                "updateMessage" to config.updateMessage,
                "downloadUrl" to config.downloadUrl,
                "maintenanceMode" to config.maintenanceMode,
                "maintenanceMessage" to config.maintenanceMessage
            )
            db.collection("config").document("app").set(data).await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }
}
