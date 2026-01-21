package com.example.emergencycommunicationsystem.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TextToSpeechHelper(context: Context) {
    private var tts: TextToSpeech? = null
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking
    private var isInitialized = false

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
            } else {
                Log.e("TTS", "Initialization failed")
            }
        }
        
        // Listen for completion
        tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                _isSpeaking.value = true
            }

            override fun onDone(utteranceId: String?) {
                _isSpeaking.value = false
            }

            override fun onError(utteranceId: String?) {
                _isSpeaking.value = false
            }
        })
    }

    fun speak(text: String, languageCode: String = "en") {
        if (!isInitialized) return

        if (_isSpeaking.value) {
            stop()
            return
        }

        val locale = when (languageCode) {
            "fil", "tl" -> Locale("fil")
            "es" -> Locale("es")
            // Note: Many engines don't support regional dialects like Ilocano (ilo), 
            // so we fallback to the locale or English, but TTS will try its best or use default voice.
            else -> Locale(languageCode)
        }

        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w("TTS", "Language $languageCode not supported, falling back to English")
            tts?.setLanguage(Locale.US)
        }

        val params = android.os.Bundle()
        params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "weather_advice")
        
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "weather_advice")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
    }
}
