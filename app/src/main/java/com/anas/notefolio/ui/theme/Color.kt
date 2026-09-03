package com.anas.notefolio.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================
// NoteFolio color tokens — copied 1:1 from the original web app's
// :root and [data-theme="dark"] CSS variables so the native app
// looks pixel-identical in both themes. Do not "improve" these
// without checking the source HTML first.
// ============================================================

// ---------- Light theme ("Spine" - paper tone) ----------
val LightBg = Color(0xFFF7F5F1)
val LightSurface = Color(0xFFFFFFFF)
val LightSurface2 = Color(0xFFEFECE5)
val LightBorder = Color(0xFFE2DED4)
val LightText = Color(0xFF201D18)
val LightTextMuted = Color(0xFF6B675F)
val LightAccent = Color(0xFF2F4A73)
val LightAccentSoft = Color(0xFFE7EDF5)
val LightAccentText = Color(0xFFFFFFFF)
val LightHighlight = Color(0xFFD98E3B)
val LightDanger = Color(0xFFC0433B)
val LightDangerSoft = Color(0xFFF7E6E4)

// ---------- Dark theme ("Ink" tone) ----------
val DarkBg = Color(0xFF14161A)
val DarkSurface = Color(0xFF1D2026)
val DarkSurface2 = Color(0xFF262A31)
val DarkBorder = Color(0xFF33373F)
val DarkText = Color(0xFFE9E7E2)
val DarkTextMuted = Color(0xFF9A978F)
val DarkAccent = Color(0xFF7C9CC9)
val DarkAccentSoft = Color(0xFF232C3A)
val DarkAccentText = Color(0xFF12151A)
val DarkHighlight = Color(0xFFE8A33D)
val DarkDanger = Color(0xFFE0645C)
val DarkDangerSoft = Color(0xFF3A2321)

// ---------- Note "spine" color swatches (left-edge bar per card) ----------
// These correspond to the colorHex() palette used to tag notes.
val SpineDefault = Color(0xFFBDB8AC)
val SpineRed = Color(0xFFC0433B)
val SpineOrange = Color(0xFFD98E3B)
val SpineYellow = Color(0xFFD9B23B)
val SpineGreen = Color(0xFF4C8C5C)
val SpineTeal = Color(0xFF3B8C8C)
val SpineBlue = Color(0xFF2F4A73)
val SpinePurple = Color(0xFF7A5FA8)
val SpinePink = Color(0xFFC15A96)
