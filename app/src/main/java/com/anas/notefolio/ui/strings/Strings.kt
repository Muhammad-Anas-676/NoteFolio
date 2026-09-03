package com.anas.notefolio.ui.strings

// Mirrors the original web app's en/ur (Roman Urdu) dictionaries keyed by data-t attributes.
// Extend this map as more screens get translated in later batches.
private val EN = mapOf(
    "settings" to "Settings",
    "appearance" to "Appearance",
    "theme" to "Theme",
    "themeSystem" to "Follow system",
    "themeLight" to "Light",
    "themeDark" to "Dark",
    "accentColor" to "Accent color",
    "language" to "Language",
    "appIcon" to "App icon",
    "secData" to "Data",
    "exportData" to "Export notes (JSON)",
    "importData" to "Import notes (JSON)",
    "importSuccess" to "Notes imported successfully",
    "exportSuccess" to "Notes exported successfully",
    "importFailed" to "Import failed — file is not a valid NoteFolio export",
    "madeBy" to "Made by Anas"
)

private val UR = mapOf(
    "settings" to "Settings",
    "appearance" to "Look",
    "theme" to "Theme",
    "themeSystem" to "System jaisa",
    "themeLight" to "Light",
    "themeDark" to "Dark",
    "accentColor" to "Accent color",
    "language" to "Zaban",
    "appIcon" to "App icon",
    "secData" to "Data",
    "exportData" to "Notes export karein (JSON)",
    "importData" to "Notes import karein (JSON)",
    "importSuccess" to "Notes kamyabi se import ho gaye",
    "exportSuccess" to "Notes kamyabi se export ho gaye",
    "importFailed" to "Import fail ho gaya — file valid NoteFolio export nahi hai",
    "madeBy" to "Made by Anas"
)

fun t(key: String, language: String): String {
    val dict = if (language == "ur") UR else EN
    return dict[key] ?: EN[key] ?: key
}
