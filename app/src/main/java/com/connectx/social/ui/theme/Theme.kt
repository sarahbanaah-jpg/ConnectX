package com.connectx.social.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Primary = Color(0xFF6B4EFF)
private val PrimaryDark = Color(0xFF4A2FD9)
private val Secondary = Color(0xFF00D4AA)
private val BackgroundDark = Color(0xFF0F0F14)
private val SurfaceDark = Color(0xFF1A1A24)
private val Error = Color(0xFFFF4D6A)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = PrimaryDark,
    secondary = Secondary,
    onSecondary = Color.Black,
    background = BackgroundDark,
    onBackground = Color.White,
    surface = SurfaceDark,
    onSurface = Color.White,
    error = Error,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8E0FF),
    secondary = Secondary,
    onSecondary = Color.Black,
    background = Color(0xFFF8F7FC),
    onBackground = Color(0xFF1A1A24),
    surface = Color.White,
    onSurface = Color(0xFF1A1A24),
    error = Error,
    onError = Color.White
)

@Composable
fun ConnectXTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
