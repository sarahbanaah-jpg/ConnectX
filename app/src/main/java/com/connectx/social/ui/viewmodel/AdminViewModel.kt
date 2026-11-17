package com.connectx.social.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectx.social.data.model.AppConfig
import com.connectx.social.data.model.Post
import com.connectx.social.data.model.SecurityFlag
import com.connectx.social.data.model.User
import com.connectx.social.data.repository.AdminRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(
    private val repository: AdminRepository = AdminRepository()
) : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    private val _flaggedPosts = MutableStateFlow<List<Post>>(emptyList())
    val flaggedPosts: StateFlow<List<Post>> = _flaggedPosts.asStateFlow()
    private val _flags = MutableStateFlow<List<SecurityFlag>>(emptyList())
    val flags: StateFlow<List<SecurityFlag>> = _flags.asStateFlow()
    private val _config = MutableStateFlow<AppConfig?>(null)
    val config: StateFlow<AppConfig?> = _config.asStateFlow()

    init {
        viewModelScope.launch { repository.getAllUsers().collect { _users.value = it } }
        viewModelScope.launch { repository.getFlaggedPosts().collect { _flaggedPosts.value = it } }
        viewModelScope.launch { repository.getSecurityFlags().collect { _flags.value = it } }
    }

    fun banUser(uid: String, ban: Boolean) { viewModelScope.launch { repository.banUser(uid, ban) } }
    fun setAdmin(uid: String, isAdmin: Boolean) { viewModelScope.launch { repository.setAdmin(uid, isAdmin) } }
    fun unflagPost(postId: String) { viewModelScope.launch { repository.unflagPost(postId) } }
    fun deletePost(postId: String) { viewModelScope.launch { repository.deletePostAsAdmin(postId) } }
    fun resolveFlag(flagId: String) { viewModelScope.launch { repository.resolveFlag(flagId) } }

    fun loadConfig() {
        viewModelScope.launch {
            repository.getAppConfig().getOrNull()?.let { _config.value = it }
        }
    }

    fun saveConfig(config: AppConfig) {
        viewModelScope.launch {
            repository.updateAppConfig(config)
            _config.value = config
        }
    }
}
