package com.connectx.social.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectx.social.data.model.Post
import com.connectx.social.data.model.User
import com.connectx.social.data.repository.AuthRepository
import com.connectx.social.data.repository.ChatRepository
import com.connectx.social.data.repository.PostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val postRepository: PostRepository = PostRepository(),
    private val chatRepository: ChatRepository = ChatRepository()
) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private var currentUserId: String = ""

    fun load(userId: String) {
        currentUserId = userId
        viewModelScope.launch {
            _isLoading.value = true
            authRepository.getUserProfile(userId).getOrNull()?.let { _user.value = it }
            postRepository.getUserPosts(userId).getOrNull()?.let { _posts.value = it }
            _isLoading.value = false
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch {
            postRepository.toggleLike(postId)
            postRepository.getUserPosts(currentUserId).getOrNull()?.let { _posts.value = it }
        }
    }

    fun startConversation(onReady: (String) -> Unit) {
        viewModelScope.launch {
            chatRepository.getOrCreateConversation(currentUserId).getOrNull()?.let { onReady(it) }
        }
    }
}
