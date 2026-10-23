package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CosmicColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = CosmicVoid,
    primaryContainer = SpaceCardElevated,
    onPrimaryContainer = NeonCyan,
    secondary = HyperMagenta,
    onSecondary = StarWhite,
    secondaryContainer = SpaceCard,
    onSecondaryContainer = HyperMagenta,
    tertiary = QuantumViolet,
    onTertiary = StarWhite,
    background = CosmicVoid,
    onBackground = StarWhite,
    surface = SpaceSurface,
    onSurface = StarWhite,
    surfaceVariant = SpaceCard,
    onSurfaceVariant = CosmicDim,
    outline = SpaceBorder,
    error = DangerRed,
    onError = StarWhite
)

@Composable
fun AstroMatrixTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // AstroMatrix is an interstellar dark-themed experience
    MaterialTheme(
        colorScheme = CosmicColorScheme,
        typography = Typography,
        content = content
    )
}
