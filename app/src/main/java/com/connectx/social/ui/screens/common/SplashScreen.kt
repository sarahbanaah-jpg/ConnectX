package com.connectx.social.ui.screens.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAuthenticated: () -> Unit, onUnauthenticated: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(800)
        if (FirebaseAuth.getInstance().currentUser != null) onAuthenticated()
        else onUnauthenticated()
    }
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Text("ConnectX", color = MaterialTheme.colorScheme.primary, fontSize = 36.sp, fontWeight = FontWeight.Bold)
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.BottomCenter).size(32.dp),
            color = MaterialTheme.colorScheme.primary, strokeWidth = 3.dp
        )
    }
}
