package com.koreanimmersion.domain.speech

import kotlinx.coroutines.flow.StateFlow

/**
 * Абстракция STT: сейчас Android [SpeechRecognizer], позже — облачный API с тем же контрактом.
 */
interface SpeechRecognitionRepository {
    val state: StateFlow<SpeechRecognitionState>

    fun startListening(languageCode: String)

    fun stopListening()

    fun release()
}
