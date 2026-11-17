package com.connectx.social.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.connectx.social.data.model.AppConfig
import com.connectx.social.ui.viewmodel.AdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(onBack: () -> Unit, viewModel: AdminViewModel = viewModel()) {
    val users by viewModel.users.collectAsState()
    val flaggedPosts by viewModel.flaggedPosts.collectAsState()
    val flags by viewModel.flags.collectAsState()
    val config by viewModel.config.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Users", "Flagged", "Security", "Updates")

    LaunchedEffect(Unit) { viewModel.loadConfig() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(title) })
                }
            }
            when (selectedTab) {
                0 -> LazyColumn {
                    items(users, key = { it.uid }) { user ->
                        ListItem(
                            headlineContent = { Text(user.displayName) },
                            supportingContent = { Text("@${user.username} · ${user.email}") },
                            trailingContent = {
                                Row {
                                    if (user.isBanned) TextButton(onClick = { viewModel.banUser(user.uid, false) }) { Text("Unban") }
                                    else TextButton(onClick = { viewModel.banUser(user.uid, true) }) { Text("Ban") }
                                    if (!user.isAdmin) TextButton(onClick = { viewModel.setAdmin(user.uid, true) }) { Text("Make Admin") }
                                }
                            }
                        )
                    }
                }
                1 -> LazyColumn {
                    items(flaggedPosts, key = { it.id }) { post ->
                        Card(Modifier.padding(8.dp).fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text(post.authorName, fontWeight = FontWeight.Bold)
                                Text(post.content)
                                Text("Reason: ${post.flagReason}", color = MaterialTheme.colorScheme.error)
                                Row {
                                    TextButton(onClick = { viewModel.unflagPost(post.id) }) { Text("Unflag") }
                                    TextButton(onClick = { viewModel.deletePost(post.id) }) { Text("Delete") }
                                }
                            }
                        }
                    }
                }
                2 -> LazyColumn {
                    items(flags, key = { it.id }) { flag ->
                        ListItem(
                            headlineContent = { Text("${flag.type.uppercase()} · ${flag.severity}") },
                            supportingContent = { Text("${flag.reason}\nTarget: ${flag.targetId}") },
                            trailingContent = {
                                if (!flag.resolved) TextButton(onClick = { viewModel.resolveFlag(flag.id) }) { Text("Resolve") }
                                else Text("Resolved", color = MaterialTheme.colorScheme.secondary)
                            }
                        )
                    }
                }
                3 -> {
                    var versionCode by remember { mutableStateOf(config?.latestVersionCode?.toString() ?: "1") }
                    var versionName by remember { mutableStateOf(config?.latestVersionName ?: "1.0.0") }
                    var message by remember { mutableStateOf(config?.updateMessage ?: "") }
                    var url by remember { mutableStateOf(config?.downloadUrl ?: "") }
                    var force by remember { mutableStateOf(config?.forceUpdate ?: false) }
                    LaunchedEffect(config) {
                        config?.let {
                            versionCode = it.latestVersionCode.toString()
                            versionName = it.latestVersionName
                            message = it.updateMessage
                            url = it.downloadUrl
                            force = it.forceUpdate
                        }
                    }
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("In-App Update Settings", style = MaterialTheme.typography.titleMedium)
                        OutlinedTextField(value = versionCode, onValueChange = { versionCode = it.filter { c -> c.isDigit() } }, label = { Text("Latest Version Code") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = versionName, onValueChange = { versionName = it }, label = { Text("Latest Version Name") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = message, onValueChange = { message = it }, label = { Text("Update Message") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("Download URL (APK)") }, modifier = Modifier.fillMaxWidth())
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Checkbox(checked = force, onCheckedChange = { force = it })
                            Text("Force update")
                        }
                        Button(onClick = {
                            viewModel.saveConfig(AppConfig(
                                latestVersionCode = versionCode.toIntOrNull() ?: 1,
                                latestVersionName = versionName,
                                forceUpdate = force,
                                updateMessage = message,
                                downloadUrl = url
                            ))
                        }, modifier = Modifier.fillMaxWidth()) { Text("Save Update Config") }
                    }
                }
            }
        }
    }
}
