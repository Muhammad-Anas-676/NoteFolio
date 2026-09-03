# NoteFolio (Android, Kotlin + Jetpack Compose)

Native rewrite of the original single-file HTML notes app. Made by Anas.

## How to open
1. Open Android Studio (Ladybug or newer recommended).
2. File → Open → select this `NoteFolio` folder.
3. Let Gradle sync (needs Gradle 8.7 / AGP 8.5.2, pulled automatically via the wrapper).
4. Run on a device/emulator running Android 7.0 (API 24) or newer.

## What's implemented
- Notes: create/edit, checklist mode, tags, folders, color labels, pin, archive, trash (30-day auto-purge)
- Search, sort, folder filtering
- Settings: theme (system/light/dark), accent color, language (English / Roman Urdu), app icon picker (4 presets via activity-alias)
- Import/Export notes as JSON (fixes the original broken feature)
- Text-to-Speech via Android's native engine (fixes the "not available in this browser" bug)
- Voice-to-text via Android's native SpeechRecognizer
- Freehand sketch attachment per note
- QR code sharing of note content
- PIN lock (Keystore-backed HMAC hashing) + optional biometric unlock
- Reminders via WorkManager + notifications
- Battery Saver awareness (autosave backs off when the device's battery saver is on)
- Stats screen (note/word counts, top tags)

## Architecture
- Single-Activity + Jetpack Compose Navigation (multi-screen / MPA-style)
- Room for local, per-device storage (`/data/data/com.anas.notefolio/databases/notefolio.db`)
- DataStore for settings/security preferences
- MVVM: Repository → ViewModel → Composable screen, per feature area

## Known follow-ups (not blocking, good next steps in Android Studio)
- Generate proper raster launcher icons via Android Studio's Image Asset tool for API < 26 devices (adaptive icons already cover API 26+)
- Wire the language setting into more screens beyond Settings (the `t()` dictionary in `ui/strings/Strings.kt` is ready to extend)
- Add ProGuard/R8 keep rules if you hit issues with Room or ZXing in a minified release build
