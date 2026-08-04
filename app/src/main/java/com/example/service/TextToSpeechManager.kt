package com.example.service

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.LanguageCode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class TextToSpeechManager(private val context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingSpeakRequest: Triple<String, LanguageCode, Float>? = null

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking

    init {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isInitialized = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })

                pendingSpeakRequest?.let { (text, lang, rate) ->
                    pendingSpeakRequest = null
                    speak(text, lang, rate)
                }
            } else {
                Log.e("TextToSpeechManager", "TTS Initialization failed")
            }
        }
    }

    fun speak(text: String, language: LanguageCode, speechRate: Float = 1.0f) {
        if (!isInitialized) {
            pendingSpeakRequest = Triple(text, language, speechRate)
            return
        }

        stop()

        // Clean text from markdown symbols (*, #, _, -, etc.) for smooth natural speech
        val cleanedText = text
            .replace(Regex("[*#_`~-]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()

        if (cleanedText.isBlank()) return

        val primaryLocale = when (language) {
            LanguageCode.EN -> Locale.US
            LanguageCode.TR -> Locale("tr", "TR")
            LanguageCode.DE -> Locale.GERMANY
            LanguageCode.FR -> Locale.FRANCE
            LanguageCode.ES -> Locale("es", "ES")
            LanguageCode.IT -> Locale.ITALY
            LanguageCode.PT -> Locale("pt", "BR")
            LanguageCode.AR -> Locale("ar", "SA")
            LanguageCode.RU -> Locale("ru", "RU")
            LanguageCode.JA -> Locale.JAPAN
            LanguageCode.KO -> Locale.KOREAN
            LanguageCode.ZH -> Locale.CHINA
            LanguageCode.HI -> Locale("hi", "IN")
        }

        val fallbackLocale = Locale(language.code)

        tts?.apply {
            setSpeechRate(speechRate)
            
            // Try primary locale (e.g. tr_TR), then language code (e.g. tr)
            var res = setLanguage(primaryLocale)
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                res = setLanguage(fallbackLocale)
            }

            // Search available voices matching the target language if available on device
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                try {
                    val matchingVoice = voices?.firstOrNull { voice ->
                        voice.locale.language.equals(language.code, ignoreCase = true)
                    }
                    if (matchingVoice != null) {
                        voice = matchingVoice
                    }
                } catch (e: Exception) {
                    Log.w("TextToSpeechManager", "Voice search failed: ${e.message}")
                }
            }

            speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, "whats_this_tts_id")
        }
    }

    fun stop() {
        try {
            if (tts?.isSpeaking == true) {
                tts?.stop()
            }
        } catch (e: Exception) {
            Log.e("TextToSpeechManager", "Error stopping TTS", e)
        } finally {
            _isSpeaking.value = false
        }
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
    }
}
