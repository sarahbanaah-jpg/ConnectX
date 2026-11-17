package com.connectx.social.ui.screens.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.connectx.social.data.model.Conversation
import com.connectx.social.ui.viewmodel.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsScreen(
    onOpenChat: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ChatViewModel = viewModel()
) {
    val conversations by viewModel.conversations.collectAsState()
    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Messages") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        if (conversations.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("No conversations yet", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        } else {
            LazyColumn(Modifier.padding(padding)) {
                items(conversations, key = { it.id }) { conv ->
                    val otherId = conv.participants.firstOrNull { it != uid } ?: return@items
                    val name = conv.participantNames[otherId] ?: "User"
                    val photo = conv.participantPhotos[otherId] ?: ""
                    val unread = conv.unreadCount[uid] ?: 0
                    ListItem(
                        headlineContent = { Text(name, fontWeight = if (unread > 0) FontWeight.Bold else FontWeight.Normal) },
                        supportingContent = {
                            Text(conv.lastMessage.ifBlank { "No messages yet" }, maxLines = 1, overflow = TextOverflow.Ellipsis,
                                fontWeight = if (unread > 0) FontWeight.Medium else FontWeight.Normal)
                        },
                        leadingContent = {
                            AsyncImage(model = photo.ifBlank { null }, contentDescription = null,
                                modifier = Modifier.size(48.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        },
                        trailingContent = { if (unread > 0) Badge { Text("$unread") } },
                        modifier = Modifier.clickable { onOpenChat(conv.id) }
                    )
                }
            }
        }
    }
}
