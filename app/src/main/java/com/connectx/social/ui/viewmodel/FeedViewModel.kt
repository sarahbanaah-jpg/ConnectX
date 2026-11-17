package com.connectx.social.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.connectx.social.data.model.Post
import com.connectx.social.data.repository.AdminRepository
import com.connectx.social.data.repository.PostRepository
import com.connectx.social.util.UpdateChecker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FeedViewModel(application: Application) : AndroidViewModel(application) {
    private val postRepository = PostRepository()
    private val adminRepository = AdminRepository()
    private val updateChecker = UpdateChecker(application)

    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin.asStateFlow()
    private val _updateInfo = MutableStateFlow<UpdateChecker.UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateChecker.UpdateInfo?> = _updateInfo.asStateFlow()

    init {
        viewModelScope.launch {
            postRepository.getFeed().collect { list ->
                _posts.value = list
                _isLoading.value = false
            }
        }
    }

    fun createPost(content: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = postRepository.createPost(content)
            if (result.isFailure) _error.value = result.exceptionOrNull()?.message
            _isLoading.value = false
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch { postRepository.toggleLike(postId) }
    }

    fun checkAdmin() {
        viewModelScope.launch { _isAdmin.value = adminRepository.isCurrentUserAdmin() }
    }

    fun checkUpdate() {
        viewModelScope.launch { _updateInfo.value = updateChecker.check() }
    }

    fun openUpdate(url: String) { updateChecker.openDownload(url) }
}
