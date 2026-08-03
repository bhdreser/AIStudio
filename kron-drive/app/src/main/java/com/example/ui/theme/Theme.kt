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
    primary = KronAccent,
    onPrimary = KronBg,
    secondary = KronPositive,
    onSecondary = KronBg,
    tertiary = KronPosDef,
    background = KronBg,
    surface = KronCard,
    onBackground = KronText,
    onSurface = KronText,
    outline = KronBorder
  )

private val LightColorScheme =
  lightColorScheme(
    primary = Color(0xFFCA8A04),
    onPrimary = Color.White,
    secondary = Color(0xFF15803D),
    onSecondary = Color.White,
    tertiary = Color(0xFF0369A1),
    background = Color(0xFFF8FAFC),
    surface = Color(0xFFEEF2F7),
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    outline = Color(0x7394A3B8)
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else DarkColorScheme


  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
