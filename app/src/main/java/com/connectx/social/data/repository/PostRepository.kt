package com.connectx.social.data.repository

import com.connectx.social.data.model.Comment
import com.connectx.social.data.model.Post
import com.connectx.social.util.AiSecurityAssistant
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class PostRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val security: AiSecurityAssistant = AiSecurityAssistant()
) {
    fun getFeed(limit: Long = 30): Flow<List<Post>> = callbackFlow {
        val listener = db.collection("posts")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val posts = snapshot?.documents?.mapNotNull { doc ->
                    try { Post.fromMap(doc.id, doc.data!!) } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(posts.filter { !it.isFlagged })
            }
        awaitClose { listener.remove() }
    }

    suspend fun createPost(content: String, imageBytes: ByteArray? = null): Result<Post> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val securityResult = security.analyzePost(content)
            if (securityResult.shouldBlock) {
                return Result.failure(Exception("Content flagged by security: ${securityResult.reason}"))
            }
            var imageUrl = ""
            if (imageBytes != null) {
                val ref = storage.reference.child("posts/${user.uid}/${UUID.randomUUID()}.jpg")
                ref.putBytes(imageBytes).await()
                imageUrl = ref.downloadUrl.await().toString()
            }
            val userDoc = db.collection("users").document(user.uid).get().await()
            val authorName = userDoc.getString("displayName") ?: user.displayName ?: "User"
            val authorPhoto = userDoc.getString("photoUrl") ?: ""
            val postRef = db.collection("posts").document()
            val post = Post(
                id = postRef.id, authorId = user.uid, authorName = authorName,
                authorPhotoUrl = authorPhoto, content = content, imageUrl = imageUrl,
                createdAt = Timestamp.now(), isFlagged = securityResult.shouldFlag,
                flagReason = securityResult.reason
            )
            postRef.set(post.toMap()).await()
            db.collection("users").document(user.uid).update("postsCount", FieldValue.increment(1)).await()
            if (securityResult.shouldFlag) {
                security.createFlag("post", post.id, securityResult.reason, securityResult.severity)
            }
            Result.success(post)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun toggleLike(postId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val postRef = db.collection("posts").document(postId)
            db.runTransaction { transaction ->
                val snap = transaction.get(postRef)
                val likedBy = (snap.get("likedBy") as? List<*>)?.mapNotNull { it as? String }?.toMutableList() ?: mutableListOf()
                if (likedBy.contains(uid)) {
                    likedBy.remove(uid)
                    transaction.update(postRef, mapOf("likedBy" to likedBy, "likeCount" to likedBy.size))
                } else {
                    likedBy.add(uid)
                    transaction.update(postRef, mapOf("likedBy" to likedBy, "likeCount" to likedBy.size))
                }
            }.await()
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun getComments(postId: String): Flow<List<Comment>> = callbackFlow {
        val listener = db.collection("posts").document(postId).collection("comments")
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { trySend(emptyList()); return@addSnapshotListener }
                val comments = snapshot?.documents?.mapNotNull { doc ->
                    try { Comment.fromMap(doc.id, doc.data!!) } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(comments)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addComment(postId: String, content: String): Result<Comment> {
        val user = auth.currentUser ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val securityResult = security.analyzePost(content)
            if (securityResult.shouldBlock) {
                return Result.failure(Exception("Comment flagged: ${securityResult.reason}"))
            }
            val userDoc = db.collection("users").document(user.uid).get().await()
            val authorName = userDoc.getString("displayName") ?: user.displayName ?: "User"
            val authorPhoto = userDoc.getString("photoUrl") ?: ""
            val commentRef = db.collection("posts").document(postId).collection("comments").document()
            val comment = Comment(
                id = commentRef.id, postId = postId, authorId = user.uid,
                authorName = authorName, authorPhotoUrl = authorPhoto,
                content = content, createdAt = Timestamp.now()
            )
            commentRef.set(comment.toMap()).await()
            db.collection("posts").document(postId).update("commentCount", FieldValue.increment(1)).await()
            Result.success(comment)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deletePost(postId: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val post = db.collection("posts").document(postId).get().await()
            val authorId = post.getString("authorId")
            val isAdmin = db.collection("users").document(uid).get().await().getBoolean("isAdmin") ?: false
            if (authorId != uid && !isAdmin) return Result.failure(Exception("Not authorized"))
            db.collection("posts").document(postId).delete().await()
            if (authorId != null) {
                db.collection("users").document(authorId).update("postsCount", FieldValue.increment(-1)).await()
            }
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun getUserPosts(userId: String): Result<List<Post>> {
        return try {
            val snap = db.collection("posts").whereEqualTo("authorId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING).get().await()
            val posts = snap.documents.mapNotNull { doc ->
                try { Post.fromMap(doc.id, doc.data!!) } catch (e: Exception) { null }
            }
            Result.success(posts)
        } catch (e: Exception) { Result.failure(e) }
    }
}
