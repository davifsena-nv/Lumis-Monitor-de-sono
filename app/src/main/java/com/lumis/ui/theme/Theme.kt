package com.lumis.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Paleta de cores inspirada em sonho: roxo profundo, azul noturno, brilho pérola
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9B6DFF),         // roxo sonho
    onPrimary = Color(0xFF1A0050),
    primaryContainer = Color(0xFF3D0099),
    onPrimaryContainer = Color(0xFFDFBFFF),
    secondary = Color(0xFF7EC8E3),        // azul estrelado
    onSecondary = Color(0xFF003548),
    secondaryContainer = Color(0xFF004D66),
    onSecondaryContainer = Color(0xFFB8E9FF),
    tertiary = Color(0xFFFFD700),         // dourado (estado EXCELLENT)
    background = Color(0xFF0D0D1A),       // azul quase preto
    onBackground = Color(0xFFE8E0FF),
    surface = Color(0xFF1A1A2E),          // superfície noturna
    onSurface = Color(0xFFE8E0FF),
    surfaceVariant = Color(0xFF2D2D4A),
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF9B90A8),
)

@Composable
fun LumisTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
