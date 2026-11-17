package com.connectx.social.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
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
import com.connectx.social.ui.screens.feed.PostCard
import com.connectx.social.ui.viewmodel.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: String,
    onEditProfile: () -> Unit,
    onMessage: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val user by viewModel.user.collectAsState()
    val posts by viewModel.posts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isOwnProfile = userId == FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(userId) { viewModel.load(userId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(user?.username?.let { "@$it" } ?: "Profile") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } },
                actions = {
                    if (isOwnProfile) IconButton(onClick = onEditProfile) { Icon(Icons.Default.Edit, "Edit") }
                    else IconButton(onClick = { viewModel.startConversation { onMessage(it) } }) { Icon(Icons.Default.Message, "Message") }
                }
            )
        }
    ) { padding ->
        if (isLoading && user == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else {
            LazyColumn(modifier = Modifier.padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        AsyncImage(model = user?.photoUrl?.ifBlank { null }, contentDescription = null, modifier = Modifier.size(96.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                        Spacer(Modifier.height(12.dp))
                        Text(user?.displayName ?: "", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Text("@${user?.username ?: ""}", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        if (!user?.bio.isNullOrBlank()) { Spacer(Modifier.height(8.dp)); Text(user?.bio ?: "", style = MaterialTheme.typography.bodyMedium) }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${user?.postsCount ?: 0}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Posts", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${user?.followersCount ?: 0}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Followers", style = MaterialTheme.typography.bodySmall)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${user?.followingCount ?: 0}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text("Following", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider()
                    }
                }
                items(posts, key = { it.id }) { post ->
                    PostCard(post, FirebaseAuth.getInstance().currentUser?.uid, { viewModel.toggleLike(post.id) }, {}, {})
                }
            }
        }
    }
}
