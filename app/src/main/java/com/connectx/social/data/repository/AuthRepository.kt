package com.connectx.social.data.repository

import com.connectx.social.data.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    val authState: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun register(email: String, password: String, displayName: String, username: String): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user ?: return Result.failure(Exception("User creation failed"))
            val profileUpdates = UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
            firebaseUser.updateProfile(profileUpdates).await()
            val fcmToken = try { FirebaseMessaging.getInstance().token.await() } catch (e: Exception) { "" }
            val user = User(
                uid = firebaseUser.uid, email = email, displayName = displayName,
                username = username.lowercase().trim(), createdAt = Timestamp.now(),
                lastActive = Timestamp.now(), fcmToken = fcmToken
            )
            db.collection("users").document(firebaseUser.uid).set(user.toMap()).await()
            Result.success(user)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return Result.failure(Exception("Login failed"))
            val fcmToken = try { FirebaseMessaging.getInstance().token.await() } catch (e: Exception) { null }
            val updates = mutableMapOf<String, Any>("lastActive" to Timestamp.now())
            if (fcmToken != null) updates["fcmToken"] = fcmToken
            db.collection("users").document(user.uid).update(updates).await()
            Result.success(user)
        } catch (e: Exception) { Result.failure(e) }
    }

    fun logout() { auth.signOut() }

    suspend fun getUserProfile(uid: String): Result<User> {
        return try {
            val snap = db.collection("users").document(uid).get().await()
            if (snap.exists()) Result.success(User.fromMap(snap.data!!))
            else Result.failure(Exception("User not found"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun updateProfile(displayName: String? = null, bio: String? = null, photoUrl: String? = null, username: String? = null): Result<Unit> {
        val uid = currentUser?.uid ?: return Result.failure(Exception("Not authenticated"))
        return try {
            val updates = mutableMapOf<String, Any>()
            displayName?.let { updates["displayName"] = it }
            bio?.let { updates["bio"] = it }
            photoUrl?.let { updates["photoUrl"] = it }
            username?.let { updates["username"] = it.lowercase().trim() }
            if (updates.isNotEmpty()) db.collection("users").document(uid).update(updates).await()
            displayName?.let {
                currentUser?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(it).build())?.await()
            }
            Result.success(Unit)
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun isUsernameAvailable(username: String): Boolean {
        return try {
            val snap = db.collection("users").whereEqualTo("username", username.lowercase().trim()).limit(1).get().await()
            snap.isEmpty
        } catch (e: Exception) { true }
    }
}
