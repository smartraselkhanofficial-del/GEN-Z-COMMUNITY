package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GenZDarkColorScheme = darkColorScheme(
  primary = NeonViolet,
  onPrimary = Color.White,
  primaryContainer = DarkSurfaceVariant,
  onPrimaryContainer = NeonViolet,
  secondary = NeonCyan,
  onSecondary = Color.Black,
  secondaryContainer = DarkSurfaceElevated,
  onSecondaryContainer = NeonCyan,
  tertiary = NeonPink,
  onTertiary = Color.White,
  background = DarkBackground,
  onBackground = TextPrimary,
  surface = DarkSurface,
  onSurface = TextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = BorderGlow
)

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = GenZDarkColorScheme,
    typography = Typography,
    content = content
  )
}
