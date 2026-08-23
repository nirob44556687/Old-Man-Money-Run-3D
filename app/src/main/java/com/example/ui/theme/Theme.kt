package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GameColorScheme = darkColorScheme(
    primary = GameGold,
    onPrimary = Color.Black,
    primaryContainer = GameOrange,
    onPrimaryContainer = Color.White,
    secondary = GameMoneyGreen,
    onSecondary = Color.Black,
    tertiary = GameMoneyPink,
    background = DarkSurface,
    onBackground = TextPrimary,
    surface = DarkCard,
    onSurface = TextPrimary,
    error = GameAngryRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GameColorScheme,
        typography = Typography,
        content = content
    )
}
