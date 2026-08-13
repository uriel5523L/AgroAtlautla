package com.agroatlautla.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors: ColorScheme = lightColorScheme(
    primary = AgroGreen,
    onPrimary = Color.White,
    secondary = AgroYellow,
    onSecondary = AgroText,
    background = AgroBackground,
    onBackground = AgroText,
    surface = Color.White,
    onSurface = AgroText,
    error = AgroDanger
)

@Composable
fun AgroAtlautlaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColors,
        content = content
    )
}
