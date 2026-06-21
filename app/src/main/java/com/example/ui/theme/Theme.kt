package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = GreenPrimary,
    secondary = GoldAccent,
    tertiary = GreenSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFF000000),
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface
  )

private val LightColorScheme =
  lightColorScheme(
    primary = GreenPrimary,
    secondary = GoldAccent,
    tertiary = GreenSecondary,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color(0xFFFFFFFF),
    onSecondary = Color(0xFFFFFFFF),
    onBackground = LightOnSurface,
    onSurface = LightOnSurface
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // For branding purposes, disable dynamic system colors to preserve the green-gold Islamic identity
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
