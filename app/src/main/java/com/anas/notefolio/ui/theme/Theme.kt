package com.anas.notefolio.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = LightAccent,
    onPrimary = LightAccentText,
    primaryContainer = LightAccentSoft,
    onPrimaryContainer = LightAccent,
    background = LightBg,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = LightSurface2,
    onSurfaceVariant = LightTextMuted,
    outline = LightBorder,
    error = LightDanger,
    errorContainer = LightDangerSoft
)

private val DarkColors = darkColorScheme(
    primary = DarkAccent,
    onPrimary = DarkAccentText,
    primaryContainer = DarkAccentSoft,
    onPrimaryContainer = DarkAccent,
    background = DarkBg,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkSurface2,
    onSurfaceVariant = DarkTextMuted,
    outline = DarkBorder,
    error = DarkDanger,
    errorContainer = DarkDangerSoft
)

/**
 * theme:
 *   "system" -> follow OS setting (default)
 *   "light"  -> force light ("Spine" paper tone)
 *   "dark"   -> force dark ("Ink" tone)
 * accentKey: "blue" (default) | "green" | "orange" | "purple" — lets the user
 * re-tint the app's primary accent from Settings without touching the rest
 * of the paper/ink palette.
 */
@Composable
fun NoteFolioTheme(
    themePreference: String = "system",
    accentKey: String = "blue",
    content: @Composable () -> Unit
) {
    val useDark = when (themePreference) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val base = if (useDark) DarkColors else LightColors

    val accent = when (accentKey) {
        "green" -> if (useDark) Color(0xFF6FB57E) else SpineGreen
        "orange" -> if (useDark) DarkHighlight else LightHighlight
        "purple" -> if (useDark) Color(0xFFA48AD1) else SpinePurple
        else -> if (useDark) DarkAccent else LightAccent // "blue" default
    }
    val colors = base.copy(primary = accent, onPrimaryContainer = accent)

    MaterialTheme(
        colorScheme = colors,
        typography = NoteFolioTypography,
        content = content
    )
}
