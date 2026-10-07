package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val JarvisColorScheme = darkColorScheme(
    primary = JarvisCyan,
    onPrimary = CyberBlack,
    primaryContainer = JarvisDarkBlue,
    onPrimaryContainer = JarvisCyanLight,
    secondary = JarvisBlue,
    onSecondary = TextPrimary,
    secondaryContainer = CyberSurfaceVariant,
    onSecondaryContainer = TextPrimary,
    tertiary = NeonGold,
    onTertiary = CyberBlack,
    background = CyberBlack,
    onBackground = TextPrimary,
    surface = CyberNavy,
    onSurface = TextPrimary,
    surfaceVariant = CyberSurface,
    onSurfaceVariant = TextSecondary,
    outline = JarvisCyan.copy(alpha = 0.4f),
    outlineVariant = JarvisCyan.copy(alpha = 0.15f)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = JarvisColorScheme,
        typography = Typography,
        content = content
    )
}
