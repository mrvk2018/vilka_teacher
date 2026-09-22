package com.koreanimmersion.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.koreanimmersion.KoreanImmersionApp
import com.koreanimmersion.core.database.entity.PhraseEntity
import com.koreanimmersion.core.database.entity.TopicEntity
import com.koreanimmersion.data.speech.AndroidSpeechRecognizerRepositoryImpl
import com.koreanimmersion.domain.playback.PhraseAudioPlayer
import com.koreanimmersion.domain.speech.PronunciationEvaluator
import com.koreanimmersion.domain.speech.SpeechRecognitionRepository
import com.koreanimmersion.domain.speech.SpeechRecognitionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

class PhraseSpeakingViewModel(app: Application) : AndroidViewModel(app) {

    private val courseRepo = KoreanImmersionApp.instance.courseContentRepository
    private val progressRepo = KoreanImmersionApp.instance.progressRepository
    private val ttsCache = KoreanImmersionApp.instance.phraseTtsCacheManager

    private val speechRepository: SpeechRecognitionRepository =
        AndroidSpeechRecognizerRepositoryImpl(app.applicationContext)

    private val audioPlayer = PhraseAudioPlayer(app.applicationContext)

    private val _topic = MutableStateFlow<TopicEntity?>(null)
    val topic: StateFlow<TopicEntity?> = _topic.asStateFlow()

    private val _phrases = MutableStateFlow<List<PhraseEntity>>(emptyList())
    val phraseCount: StateFlow<Int> = _phrases.map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    private val _currentIndex = MutableStateFlow(0)

    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    val currentPhrase: StateFlow<PhraseEntity?> = combine(_phrases, _currentIndex) { list, idx ->
        list.getOrNull(idx)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _feedback = MutableStateFlow<SpeakingFeedback>(SpeakingFeedback.None)
    val feedback: StateFlow<SpeakingFeedback> = _feedback.asStateFlow()

    val speechState: StateFlow<SpeechRecognitionState> = speechRepository.state

    private val _learnedCount = MutableStateFlow(0)
    val learnedCount: StateFlow<Int> = _learnedCount.asStateFlow()

    init {
        viewModelScope.launch {
            progressRepo.observeUserProgress().collect { progress ->
                val ids = progress?.learnedPhraseIdsJson?.let {
                    com.koreanimmersion.core.database.LearnedPhrasesJson.decode(it)
                } ?: emptyList()
                _learnedCount.value = ids.size
            }
        }
        viewModelScope.launch {
            speechRepository.state.collect { state ->
                when (state) {
                    is SpeechRecognitionState.Success -> evaluateRecognition(
                        state.text,
                        state.alternates
                    )
                    is SpeechRecognitionState.Idle -> Unit
                    is SpeechRecognitionState.Listening -> _feedback.value = SpeakingFeedback.None
                    is SpeechRecognitionState.Error -> _feedback.value = SpeakingFeedback.Error(state.message)
                }
            }
        }
    }

    fun loadTopic(topicId: Long) {
        viewModelScope.launch {
            _topic.value = courseRepo.getTopic(topicId)
            _phrases.value = courseRepo.getPhrasesByTopicId(topicId)
            _currentIndex.value = 0
            _feedback.value = SpeakingFeedback.None
        }
    }

    fun playCurrentPhrase() {
        val phrase = _phrases.value.getOrNull(_currentIndex.value) ?: return
        viewModelScope.launch {
            val url = phrase.audioUrlOrPath.takeIf { it.isNotBlank() }
                ?: ttsCache.ensureCachedKo(phrase.id, phrase.koreanText)?.toString()
            url?.let { audioPlayer.play(it) }
        }
    }

    fun startListeningKorean() {
        _feedback.value = SpeakingFeedback.None
        speechRepository.startListening(Locale.KOREAN.toString())
    }

    fun stopListening() {
        speechRepository.stopListening()
    }

    fun resetSpeechIdle() {
        speechRepository.release()
    }

    private fun evaluateRecognition(recognized: String, alternates: List<String>) {
        val phrase = _phrases.value.getOrNull(_currentIndex.value) ?: return
        val candidates = (listOf(recognized) + alternates).filter { it.isNotBlank() }.distinct()
        var bestPercent = 0
        var passed = false
        candidates.forEach { candidate ->
            when (val result = PronunciationEvaluator.evaluate(phrase.koreanText, candidate)) {
                PronunciationEvaluator.Result.Success -> passed = true
                is PronunciationEvaluator.Result.TryAgain ->
                    bestPercent = maxOf(bestPercent, result.similarityPercent)
            }
        }
        if (passed) {
            _feedback.value = SpeakingFeedback.Success
            viewModelScope.launch {
                val topic = _topic.value
                progressRepo.markCoursePhraseLearned(
                    topicId = topic?.id ?: phrase.topicId,
                    topicKey = topic?.key,
                    phraseId = phrase.id
                )
            }
        } else {
            _feedback.value = SpeakingFeedback.TryAgain(bestPercent)
        }
        speechRepository.release()
    }

    fun goToNextPhrase() {
        val next = _currentIndex.value + 1
        if (next < _phrases.value.size) {
            _currentIndex.value = next
            _feedback.value = SpeakingFeedback.None
        }
    }

    fun repeatCurrentPhrase() {
        _feedback.value = SpeakingFeedback.None
    }

    override fun onCleared() {
        speechRepository.release()
        audioPlayer.release()
        super.onCleared()
    }

    sealed interface SpeakingFeedback {
        data object None : SpeakingFeedback
        data object Success : SpeakingFeedback
        data class TryAgain(val similarityPercent: Int) : SpeakingFeedback
        data class Error(val message: String) : SpeakingFeedback
    }
}
