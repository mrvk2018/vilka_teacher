package com.koreanimmersion.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.koreanimmersion.KoreanImmersionApp
import com.koreanimmersion.core.database.LearnedPhrasesJson
import com.koreanimmersion.core.database.entity.TopicEntity
import com.koreanimmersion.data.local.DatabaseSeeder
import com.koreanimmersion.data.repository.LlmChatResult
import com.koreanimmersion.data.speech.AndroidSpeechRecognizerRepositoryImpl
import com.koreanimmersion.domain.llm.LlmChatMessage
import com.koreanimmersion.domain.llm.LlmTeacherReplyParser
import com.koreanimmersion.domain.speech.SpeechRecognitionRepository
import com.koreanimmersion.domain.speech.SpeechRecognitionState
import com.koreanimmersion.tts.LocaleTtsEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class PhraseLlmDialogViewModel(app: Application) : AndroidViewModel(app) {

    private val courseRepo = KoreanImmersionApp.instance.courseContentRepository
    private val progressRepo = KoreanImmersionApp.instance.progressRepository
    private val llmRepo = KoreanImmersionApp.instance.llmChatRepository
    private val ttsEngine = KoreanImmersionApp.instance.ttsEngine

    private val speechRepository: SpeechRecognitionRepository =
        AndroidSpeechRecognizerRepositoryImpl(app.applicationContext)

    private val userId = DatabaseSeeder.DEFAULT_USER_ID

    private val _topic = MutableStateFlow<TopicEntity?>(null)
    val topic: StateFlow<TopicEntity?> = _topic.asStateFlow()

    private val _messages = MutableStateFlow<List<LlmChatMessage>>(emptyList())
    val messages: StateFlow<List<LlmChatMessage>> = _messages.asStateFlow()

    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _errorBanner = MutableStateFlow<String?>(null)
    val errorBanner: StateFlow<String?> = _errorBanner.asStateFlow()

    val speechState: StateFlow<SpeechRecognitionState> = speechRepository.state

    private var learnedPhraseTexts: List<String> = emptyList()
    private var nativeLanguageCode: String = "ru"

    init {
        viewModelScope.launch {
            speechRepository.state.collect { state ->
                when (state) {
                    is SpeechRecognitionState.Success -> {
                        val merged = (_inputText.value + " " + state.text).trim()
                        _inputText.value = merged
                        speechRepository.release()
                    }
                    is SpeechRecognitionState.Error -> {
                        _errorBanner.value = state.message
                        speechRepository.release()
                    }
                    else -> Unit
                }
            }
        }
    }

    fun loadTopic(topicId: Long, useEnglishUi: Boolean) {
        nativeLanguageCode = if (useEnglishUi) "en" else "ru"
        viewModelScope.launch {
            val topicEntity = courseRepo.getTopic(topicId)
            _topic.value = topicEntity
            val phrases = courseRepo.getPhrasesByTopicId(topicId)
            val progress = progressRepo.getUserProgress()
            val learnedIds = progress?.learnedPhraseIdsJson?.let { LearnedPhrasesJson.decode(it) }
                ?: emptyList()
            learnedPhraseTexts = phrases
                .filter { it.id in learnedIds }
                .map { it.koreanText }
                .ifEmpty { phrases.map { it.koreanText } }
            progressRepo.enterLlmDialogStage(topicId, topicEntity?.key)
        }
    }

    fun updateInput(text: String) {
        _inputText.value = text
    }

    fun clearError() {
        _errorBanner.value = null
    }

    fun sendMessage() {
        val text = _inputText.value.trim()
        val topicKey = _topic.value?.key ?: return
        if (text.isEmpty() || _isSending.value) return

        val history = _messages.value.takeLast(HISTORY_WINDOW_SIZE)

        _messages.value = _messages.value + LlmChatMessage.User(text)
        _inputText.value = ""
        _isSending.value = true
        _errorBanner.value = null

        viewModelScope.launch {
            when (
                val result = llmRepo.sendUserMessage(
                    userId = userId,
                    userMessage = text,
                    currentTopicKey = topicKey,
                    nativeLanguage = nativeLanguageCode,
                    learnedPhrases = learnedPhraseTexts,
                    history = history
                )
            ) {
                is LlmChatResult.Success -> {
                    val parsed = LlmTeacherReplyParser.parse(
                        raw = result.assistantMessage,
                        nativeLanguage = nativeLanguageCode
                    )
                    _messages.value = _messages.value + LlmChatMessage.Teacher(parsed.displayText)
                    val speechText = LlmTeacherReplyParser.sanitizeForKoreanTts(parsed.koreanPart)
                    if (speechText.isNotBlank()) {
                        ttsEngine.speakAloud(speechText, LocaleTtsEngine.LOCALE_KO)
                    }
                }
                is LlmChatResult.Failure -> {
                    _errorBanner.value = result.message
                }
            }
            _isSending.value = false
        }
    }

    fun startVoiceInput() {
        if (_isSending.value) return
        _errorBanner.value = null
        speechRepository.startListening(Locale.KOREAN.toString())
    }

    fun stopVoiceInput() {
        speechRepository.stopListening()
    }

    fun releaseSpeech() {
        speechRepository.release()
    }

    override fun onCleared() {
        speechRepository.release()
        super.onCleared()
    }

    companion object {
        private const val HISTORY_WINDOW_SIZE = 6
    }
}
