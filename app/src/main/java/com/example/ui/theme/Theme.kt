package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = BestNetGreen,
    onPrimary = Color.White,
    primaryContainer = BestNetGreenDark,
    onPrimaryContainer = BestNetGreenLight,
    secondary = BestNetCyan,
    onSecondary = Color.White,
    background = Color(0xFF0F172A),
    surface = Color(0xFF1E293B),
    onBackground = Color(0xFFF8FAFC),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF475569)
  )

private val LightColorScheme =
  lightColorScheme(
    primary = BestNetGreen,
    onPrimary = Color.White,
    primaryContainer = BestNetGreenLight,
    onPrimaryContainer = BestNetGreenDark,
    secondary = BestNetDarkNavy,
    onSecondary = Color.White,
    background = BestNetBackground,
    surface = BestNetSurface,
    onBackground = BestNetInk,
    onSurface = BestNetInk,
    surfaceVariant = BestNetSurfaceVariant,
    onSurfaceVariant = BestNetMuted,
    outline = BestNetBorder,
    error = BestNetRed,
    onError = Color.White
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

