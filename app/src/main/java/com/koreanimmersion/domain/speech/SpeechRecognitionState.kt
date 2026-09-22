package com.koreanimmersion.domain.speech

/** Состояния локального STT (готово к замене на серверный распознаватель). */
sealed interface SpeechRecognitionState {
    data object Idle : SpeechRecognitionState
    data object Listening : SpeechRecognitionState
    data class Success(
        val text: String,
        val alternates: List<String> = emptyList()
    ) : SpeechRecognitionState
    data class Error(val message: String) : SpeechRecognitionState
}
