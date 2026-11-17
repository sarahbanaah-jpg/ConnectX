package com.connectx.social.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.connectx.social.data.model.ChatMessage
import com.connectx.social.data.model.Conversation
import com.connectx.social.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {
    private val _conversations = MutableStateFlow<List<Conversation>>(emptyList())
    val conversations: StateFlow<List<Conversation>> = _conversations.asStateFlow()
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()
    private var messagesJob: Job? = null

    init {
        viewModelScope.launch {
            repository.getConversations().collect { _conversations.value = it }
        }
    }

    fun openConversation(conversationId: String) {
        messagesJob?.cancel()
        messagesJob = viewModelScope.launch {
            repository.markAsRead(conversationId)
            repository.getMessages(conversationId).collect { _messages.value = it }
        }
    }

    fun sendMessage(conversationId: String, content: String) {
        viewModelScope.launch { repository.sendMessage(conversationId, content) }
    }
}
