package com.june.assistant.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import androidx.core.content.ContextCompat
import java.util.Locale

class VoiceManager(
    private val context: Context,
    private val onSpeechResult: (String) -> Unit,
    private val onSpeechError: (String) -> Unit = {}
) : TextToSpeech.OnInitListener, RecognitionListener {

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    var isVoiceEnabled: Boolean = true
    private var isTtsReady: Boolean = false

    init {
        try {
            tts = TextToSpeech(context.applicationContext, this)
        } catch (_: Exception) {}

        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(this@VoiceManager)
                }
            }
        } catch (_: Exception) {}
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale.getDefault()
            isTtsReady = true
        }
    }

    fun speak(text: String) {
        if (!isVoiceEnabled || !isTtsReady) return
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "JUNE_TTS_ID")
    }

    fun stopSpeaking() {
        tts?.stop()
    }

    fun startListening() {
        stopSpeaking()
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            onSpeechError("Microphone permission (RECORD_AUDIO) is not granted.")
            return
        }

        if (speechRecognizer == null) {
            onSpeechError("Speech recognition service is unavailable on this device.")
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }
        speechRecognizer?.startListening(intent)
    }

    fun stopListening() {
        speechRecognizer?.stopListening()
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }

    // Speech recognition callbacks
    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            onSpeechResult(matches[0])
        }
    }

    override fun onError(error: Int) {
        val errorMsg = when (error) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client-side recognition error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions for speech recognition"
            SpeechRecognizer.ERROR_NETWORK -> "Network error during speech recognition"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout during speech recognition"
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech could be recognized"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognition service is busy"
            SpeechRecognizer.ERROR_SERVER -> "Recognition server error"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
            else -> "Speech recognition error (code $error)"
        }
        onSpeechError(errorMsg)
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}
