package com.connectx.social.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectx.social.data.model.User
import com.connectx.social.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EditProfileViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun load() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch {
            repository.getUserProfile(uid).getOrNull()?.let { _user.value = it }
        }
    }

    fun save(displayName: String, bio: String, username: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            _error.value = null
            val result = repository.updateProfile(
                displayName = displayName.trim(), bio = bio.trim(), username = username.trim()
            )
            _isSaving.value = false
            if (result.isSuccess) onSuccess()
            else _error.value = result.exceptionOrNull()?.message ?: "Save failed"
        }
    }
}
