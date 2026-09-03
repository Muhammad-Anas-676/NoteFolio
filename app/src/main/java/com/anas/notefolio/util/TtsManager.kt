package com.anas.notefolio.util

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * The old WebView build hit "speech synthesis not available in this browser"
 * because Android's WebView often ships without the Web Speech API wired up.
 * This uses Android's native TextToSpeech engine directly instead, which is
 * always available on-device (no browser involved), fixing that bug for good.
 */
class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) tts?.language = Locale.getDefault()
        }
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        if (!ready) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "notefolio_utterance")
    }

    fun stop() {
        tts?.stop()
    }

    fun isSpeaking(): Boolean = tts?.isSpeaking == true

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
