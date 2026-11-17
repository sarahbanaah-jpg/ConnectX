package com.connectx.social.ui.screens.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.connectx.social.data.model.Post
import com.connectx.social.ui.viewmodel.FeedViewModel
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    onProfileClick: (String) -> Unit,
    onConversationsClick: () -> Unit,
    onAdminClick: () -> Unit,
    onLogout: () -> Unit,
    viewModel: FeedViewModel = viewModel()
) {
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isAdmin by viewModel.isAdmin.collectAsState()
    var showCreate by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    val updateInfo by viewModel.updateInfo.collectAsState()

    LaunchedEffect(Unit) { viewModel.checkUpdate(); viewModel.checkAdmin() }
    LaunchedEffect(updateInfo) { if (updateInfo?.available == true) showUpdateDialog = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ConnectX", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onConversationsClick) { Icon(Icons.Default.Chat, "Messages") }
                    if (isAdmin) IconButton(onClick = onAdminClick) { Icon(Icons.Default.AdminPanelSettings, "Admin") }
                    IconButton(onClick = {
                        FirebaseAuth.getInstance().currentUser?.uid?.let { onProfileClick(it) }
                    }) { Icon(Icons.Default.Person, "Profile") }
                    IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, "Logout") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreate = true }, containerColor = MaterialTheme.colorScheme.primary) {
                Icon(Icons.Default.Add, "New Post")
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                isLoading && posts.isEmpty() -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                posts.isEmpty() -> Text("No posts yet. Be the first!", Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f))
                else -> LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(posts, key = { it.id }) { post ->
                        PostCard(post, FirebaseAuth.getInstance().currentUser?.uid, { viewModel.toggleLike(post.id) }, {}, { onProfileClick(post.authorId) })
                    }
                }
            }
            if (error != null) Snackbar(Modifier.align(Alignment.BottomCenter).padding(16.dp)) { Text(error!!) }
        }
    }

    if (showCreate) {
        CreatePostDialog(onDismiss = { showCreate = false }, onPost = { content -> viewModel.createPost(content); showCreate = false })
    }

    if (showUpdateDialog && updateInfo != null) {
        AlertDialog(
            onDismissRequest = { if (!updateInfo!!.force) showUpdateDialog = false },
            title = { Text("Update Available") },
            text = { Text("${updateInfo!!.message}\n\nVersion ${updateInfo!!.versionName}") },
            confirmButton = { TextButton(onClick = { viewModel.openUpdate(updateInfo!!.downloadUrl) }) { Text("Update") } },
            dismissButton = if (!updateInfo!!.force) {{ TextButton(onClick = { showUpdateDialog = false }) { Text("Later") } }} else null
        )
    }
}

@Composable
fun PostCard(post: Post, currentUserId: String?, onLike: () -> Unit, onComment: () -> Unit, onAuthorClick: () -> Unit) {
    val liked = currentUserId != null && post.likedBy.contains(currentUserId)
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { onAuthorClick() }) {
                AsyncImage(model = post.authorPhotoUrl.ifBlank { null }, contentDescription = null, modifier = Modifier.size(40.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(post.authorName, fontWeight = FontWeight.SemiBold)
                    Text(formatTime(post.createdAt?.toDate()), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(post.content, style = MaterialTheme.typography.bodyLarge)
            if (post.imageUrl.isNotBlank()) {
                Spacer(Modifier.height(12.dp))
                AsyncImage(model = post.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().heightIn(max = 300.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onLike) {
                    Icon(if (liked) Icons.Default.Favorite else Icons.Default.FavoriteBorder, "Like", tint = if (liked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface)
                }
                Text("${post.likeCount}")
                Spacer(Modifier.width(16.dp))
                IconButton(onClick = onComment) { Icon(Icons.Default.ChatBubbleOutline, "Comment") }
                Text("${post.commentCount}")
            }
        }
    }
}

@Composable
fun CreatePostDialog(onDismiss: () -> Unit, onPost: (String) -> Unit) {
    var content by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Post") },
        text = { OutlinedTextField(value = content, onValueChange = { content = it }, placeholder = { Text("What's on your mind?") }, modifier = Modifier.fillMaxWidth().height(120.dp), maxLines = 6) },
        confirmButton = { TextButton(onClick = { if (content.isNotBlank()) onPost(content) }, enabled = content.isNotBlank()) { Text("Post") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun formatTime(date: Date?): String {
    if (date == null) return ""
    val diff = System.currentTimeMillis() - date.time
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m"
        diff < 86_400_000 -> "${diff / 3_600_000}h"
        else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
    }
}
