package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class MahiVoiceManager(private val context: Context) : TextToSpeech.OnInitListener {

    private val TAG = "MahiVoiceManager"

    // State flows
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _isTtsReady = MutableStateFlow(false)
    val isTtsReady: StateFlow<Boolean> = _isTtsReady.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _activeLanguage = MutableStateFlow("bn-BD")
    val activeLanguage: StateFlow<String> = _activeLanguage.asStateFlow()

    private val _pitch = MutableStateFlow(1.18f) // Female tone default
    val pitch: StateFlow<Float> = _pitch.asStateFlow()

    private val _speechRate = MutableStateFlow(0.95f) // Natural friendly cadence
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _statusMessage = MutableStateFlow("সিস্টেম প্রস্তুত (System Ready)")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    // Engines
    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    var onSpeechRecognized: ((String) -> Unit)? = null

    init {
        initializeTts()
    }

    private fun initializeTts() {
        textToSpeech = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.let { tts ->
                val targetLocale = when {
                    _activeLanguage.value.startsWith("hi") -> Locale.forLanguageTag("hi-IN")
                    _activeLanguage.value.startsWith("bn") -> Locale.forLanguageTag("bn-BD")
                    else -> Locale.US
                }
                val langResult = tts.setLanguage(targetLocale)

                if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    val fallback = tts.setLanguage(Locale.forLanguageTag("bn"))
                    if (fallback == TextToSpeech.LANG_MISSING_DATA || fallback == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.w(TAG, "Locale TTS not directly available, keeping default locale with pitch adjustment")
                        tts.language = Locale.getDefault()
                    }
                }

                // Female voice optimization: look for female voice in available voices
                try {
                    val voices = tts.voices
                    if (!voices.isNullOrEmpty()) {
                        val femaleVoice = voices.find { voice ->
                            val name = voice.name.lowercase()
                            (name.contains("female") || name.contains("bdf") || name.contains("ban") || name.contains("bn-")) &&
                                    !name.contains("male")
                        } ?: voices.find { it.locale.language == "bn" }
                        if (femaleVoice != null) {
                            tts.voice = femaleVoice
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error configuring voice: ${e.message}")
                }

                tts.setPitch(_pitch.value)
                tts.setSpeechRate(_speechRate.value)

                tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                        _statusMessage.value = "মাহি কথা বলছে... (Mahi Speaking)"
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                        _statusMessage.value = "মাহি শুনছে... (Mahi Ready)"
                    }

                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                        _statusMessage.value = "অডিও প্লেব্যাকে ত্রুটি (Audio Error)"
                    }
                })

                _isTtsReady.value = true
                Log.d(TAG, "TTS initialized successfully")
            }
        } else {
            Log.e(TAG, "TTS Initialization failed with status $status")
            _statusMessage.value = "টিটিএস ইঞ্জিন লোড হয়নি"
        }
    }

    fun speak(text: String) {
        if (text.isBlank()) return
        stopListening()
        textToSpeech?.let { tts ->
            tts.setPitch(_pitch.value)
            tts.setSpeechRate(_speechRate.value)
            
            // Clean text for speech (strip markdown symbols like ** # `)
            val cleanText = text
                .replace(Regex("\\*\\*|\\*|#+|_|`+"), "")
                .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1")
                .trim()

            val utteranceId = "mahi_speech_${System.currentTimeMillis()}"
            val params = Bundle().apply {
                putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, android.media.AudioManager.STREAM_MUSIC)
            }
            tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            _isSpeaking.value = true
        }
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
        _isSpeaking.value = false
        _statusMessage.value = "ভয়েস বন্ধ করা হয়েছে"
    }

    fun startListening() {
        stopSpeaking()

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            _statusMessage.value = "ভয়েস রিকগনিশন এই ডিভাইসে উপলব্ধ নেই"
            return
        }

        try {
            speechRecognizer?.destroy()
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _recognizedText.value = ""
                        _statusMessage.value = "শুনছি... কথা বলুন (Listening...)"
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        // Normalize RMS (usually -2 to 10 dB) to 0.0 - 1.0 range
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1f)
                        _audioAmplitude.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _statusMessage.value = "প্রসেসিং হচ্ছে... (Processing...)"
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _audioAmplitude.value = 0f
                        val msg = when (error) {
                            SpeechRecognizer.ERROR_NO_MATCH -> "কথা স্পষ্ট শোনা যায়নি (No Match)"
                            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "কোনো শব্দ পাওয়া যায়নি (Timeout)"
                            SpeechRecognizer.ERROR_NETWORK -> "নেটওয়ার্ক সমস্যা (Network Error)"
                            SpeechRecognizer.ERROR_AUDIO -> "মাইক্রোফোন ত্রুটি (Audio Error)"
                            else -> "ভয়েস ইনপুট ত্রুটি ($error)"
                        }
                        _statusMessage.value = msg
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _audioAmplitude.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            val text = matches[0]
                            _recognizedText.value = text
                            _statusMessage.value = "গ্রহণ করা হয়েছে: \"$text\""
                            onSpeechRecognized?.invoke(text)
                        } else {
                            _statusMessage.value = "কিছু শোনা যায়নি"
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            _recognizedText.value = matches[0]
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, _activeLanguage.value)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, _activeLanguage.value)
                putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                putExtra(RecognizerIntent.EXTRA_PROMPT, "মাহি শুনছে... বলুন")
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error starting voice recognition: ${e.message}")
            _statusMessage.value = "ভয়েস স্টার্ট করা যায়নি: ${e.message}"
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping voice recognition: ${e.message}")
        }
        _isListening.value = false
        _audioAmplitude.value = 0f
    }

    fun setPitch(newPitch: Float) {
        _pitch.value = newPitch.coerceIn(0.5f, 2.0f)
        textToSpeech?.setPitch(_pitch.value)
    }

    fun setSpeechRate(newRate: Float) {
        _speechRate.value = newRate.coerceIn(0.5f, 2.0f)
        textToSpeech?.setSpeechRate(_speechRate.value)
    }

    fun setLanguage(langCode: String) {
        _activeLanguage.value = langCode
        val locale = when {
            langCode.startsWith("hi") -> Locale.forLanguageTag("hi-IN")
            langCode.startsWith("bn") -> Locale.forLanguageTag("bn-BD")
            else -> Locale.US
        }
        textToSpeech?.setLanguage(locale)
    }

    fun destroy() {
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing resources: ${e.message}")
        }
    }
}
