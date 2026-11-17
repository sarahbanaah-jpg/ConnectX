package com.connectx.social.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectx.social.data.repository.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    private val _currentUser = MutableStateFlow<FirebaseUser?>(repository.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    init {
        viewModelScope.launch {
            repository.authState.collect { user -> _currentUser.value = user }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            val result = repository.login(email.trim(), password)
            _uiState.value = if (result.isSuccess) AuthUiState(success = true)
            else AuthUiState(error = result.exceptionOrNull()?.message ?: "Login failed")
        }
    }

    fun register(email: String, password: String, displayName: String, username: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState(isLoading = true)
            if (username.length < 3) {
                _uiState.value = AuthUiState(error = "Username must be at least 3 characters")
                return@launch
            }
            if (!repository.isUsernameAvailable(username)) {
                _uiState.value = AuthUiState(error = "Username already taken")
                return@launch
            }
            val result = repository.register(email.trim(), password, displayName.trim(), username)
            _uiState.value = if (result.isSuccess) AuthUiState(success = true)
            else AuthUiState(error = result.exceptionOrNull()?.message ?: "Registration failed")
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
    }

    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }
}
