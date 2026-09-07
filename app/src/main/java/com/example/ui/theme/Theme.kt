package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AegisDarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = ObsidianBg,
    primaryContainer = SlateCardHover,
    onPrimaryContainer = CyberCyan,
    secondary = CyberSky,
    onSecondary = ObsidianBg,
    secondaryContainer = SlateCard,
    onSecondaryContainer = CyberSky,
    tertiary = ElectricAmber,
    onTertiary = ObsidianBg,
    tertiaryContainer = AmberGlow,
    onTertiaryContainer = TextPrimary,
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = SlateCard,
    onSurfaceVariant = TextSecondary,
    error = ThreatRed,
    onError = TextPrimary,
    errorContainer = ThreatRedGlow,
    onErrorContainer = TextPrimary,
    outline = BorderSubtle,
    outlineVariant = BorderSubtle
)

private val PhantomDarkColorScheme = AegisDarkColorScheme

@Composable
fun AegisTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = AegisDarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false, // Preserve cybersecurity identity
    content: @Composable () -> Unit,
) {
    AegisTheme(
        darkTheme = darkTheme,
        dynamicColor = dynamicColor,
        content = content
    )
}

@Composable
fun PhantomAITheme(
    content: @Composable () -> Unit
) {
    AegisTheme(content = content)
}
