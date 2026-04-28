package com.underthykilt.poketypes.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColorScheme = darkColorScheme(
    primary = Color(0xFFCC0000),
    secondary = Color(0xFF3D7DCA),
    background = Color(0xFF1A1A2E),
    surface = Color(0xFF16213E),
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun PokeTypesTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColorScheme, content = content)
}
