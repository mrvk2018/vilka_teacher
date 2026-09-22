package com.koreanimmersion.data.speech

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import com.koreanimmersion.R
import com.koreanimmersion.domain.speech.SpeechRecognitionRepository
import com.koreanimmersion.domain.speech.SpeechRecognitionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class AndroidSpeechRecognizerRepositoryImpl(
    context: Context
) : SpeechRecognitionRepository {

    private val appContext = context.applicationContext
    private val _state = MutableStateFlow<SpeechRecognitionState>(SpeechRecognitionState.Idle)
    override val state: StateFlow<SpeechRecognitionState> = _state.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    override fun startListening(languageCode: String) {
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            _state.value = SpeechRecognitionState.Error(
                appContext.getString(R.string.stt_error_not_available)
            )
            return
        }

        releaseRecognizer()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
            setRecognitionListener(recognitionListener)
        }

        val localeTag = languageCode.ifBlank { Locale.KOREAN.toString() }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, localeTag)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
        }

        _state.value = SpeechRecognitionState.Listening
        speechRecognizer?.startListening(intent)
    }

    override fun stopListening() {
        speechRecognizer?.stopListening()
    }

    override fun release() {
        releaseRecognizer()
        _state.value = SpeechRecognitionState.Idle
    }

    private fun releaseRecognizer() {
        speechRecognizer?.destroy()
        speechRecognizer = null
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {}

        override fun onBeginningOfSpeech() {}

        override fun onRmsChanged(rmsdB: Float) {}

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {}

        override fun onError(error: Int) {
            if (error == SpeechRecognizer.ERROR_CLIENT) {
                _state.value = SpeechRecognitionState.Idle
                return
            }
            _state.value = SpeechRecognitionState.Error(mapError(error))
        }

        override fun onResults(results: Bundle?) {
            val matches = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                .orEmpty()
            val text = matches.firstOrNull().orEmpty()
            if (text.isBlank()) {
                _state.value = SpeechRecognitionState.Error(
                    appContext.getString(R.string.stt_error_no_match)
                )
            } else {
                _state.value = SpeechRecognitionState.Success(
                    text = text,
                    alternates = matches.drop(1)
                )
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {}

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    private fun mapError(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_AUDIO ->
            appContext.getString(R.string.stt_error_audio)
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
            appContext.getString(R.string.stt_error_permission)
        SpeechRecognizer.ERROR_NETWORK,
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
            appContext.getString(R.string.stt_error_network)
        SpeechRecognizer.ERROR_NO_MATCH ->
            appContext.getString(R.string.stt_error_no_match)
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
            appContext.getString(R.string.stt_error_timeout)
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
            appContext.getString(R.string.stt_error_busy)
        else -> appContext.getString(R.string.stt_error_unknown, code)
    }
}
