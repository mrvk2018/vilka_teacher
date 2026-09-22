package com.koreanimmersion.ui.viewmodel

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.koreanimmersion.KoreanImmersionApp
import com.koreanimmersion.data.local.entity.ExamAttemptEntity
import com.koreanimmersion.data.local.entity.LessonEntity
import com.koreanimmersion.data.local.entity.TopicEntity
import com.koreanimmersion.data.repository.ContentRepository
import com.koreanimmersion.domain.playback.PlaybackSegment
import com.koreanimmersion.service.AudioPlaybackService
import com.koreanimmersion.service.PlaybackSegmentParcelable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {

    private val contentRepo: ContentRepository = KoreanImmersionApp.instance.contentRepository

    val topics: StateFlow<List<TopicEntity>> = contentRepo.observeTopics()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

class TopicDetailViewModel(app: Application) : AndroidViewModel(app) {

    private val contentRepo = KoreanImmersionApp.instance.contentRepository

    fun observeLessons(topicId: Long) = contentRepo.observeLessons(topicId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getTopic(topicId: Long): TopicEntity? = contentRepo.getTopic(topicId)
}

class LessonPlayerViewModel(app: Application) : AndroidViewModel(app) {

    private val appContext = app.applicationContext
    private val contentRepo = KoreanImmersionApp.instance.contentRepository
    private val progressRepo = KoreanImmersionApp.instance.progressRepository

    private val _uiState = MutableStateFlow(LessonPlayerUiState())
    val uiState: StateFlow<LessonPlayerUiState> = _uiState.asStateFlow()

    private var pendingExamPromptLessonId: Long? = null
    private var screenWasLocked = false

    private val playbackReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                AudioPlaybackService.ACTION_PLAYBACK_STARTED -> {
                    val replay = intent.getBooleanExtra(AudioPlaybackService.EXTRA_REPLAY_LOOP, false)
                    _uiState.value = _uiState.value.copy(isPlaying = true, isReplayLoop = replay)
                }
                AudioPlaybackService.ACTION_PLAYBACK_STOPPED -> {
                    _uiState.value = _uiState.value.copy(isPlaying = false, isReplayLoop = false)
                }
                AudioPlaybackService.ACTION_CYCLE_COMPLETED -> {
                    val lessonId = intent.getLongExtra(AudioPlaybackService.EXTRA_LESSON_ID, -1L)
                    if (lessonId > 0) {
                        viewModelScope.launch {
                            progressRepo.recordFullPlaythrough(lessonId)
                            _uiState.value = _uiState.value.copy(
                                fullPlaythroughsRecorded = _uiState.value.fullPlaythroughsRecorded + 1,
                                examAvailable = true
                            )
                            if (!_uiState.value.isReplayLoop) {
                                offerExamPrompt(lessonId)
                            }
                        }
                    }
                }
                AudioPlaybackService.ACTION_USER_STOP -> {
                    val lessonId = intent.getLongExtra(AudioPlaybackService.EXTRA_LESSON_ID, -1L)
                    if (lessonId > 0) {
                        viewModelScope.launch {
                            val progress = progressRepo.recordStopPressed(lessonId)
                            _uiState.value = _uiState.value.copy(
                                examAvailable = progress.examAvailable
                            )
                            if (progress.examAvailable) {
                                offerExamPrompt(lessonId)
                            }
                        }
                    }
                    _uiState.value = _uiState.value.copy(isPlaying = false, isReplayLoop = false)
                }
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(AudioPlaybackService.ACTION_PLAYBACK_STARTED)
            addAction(AudioPlaybackService.ACTION_PLAYBACK_STOPPED)
            addAction(AudioPlaybackService.ACTION_CYCLE_COMPLETED)
            addAction(AudioPlaybackService.ACTION_USER_STOP)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.registerReceiver(
                appContext, playbackReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            appContext.registerReceiver(playbackReceiver, filter)
        }
    }

    fun loadLesson(lessonId: Long) {
        viewModelScope.launch {
            val lesson = contentRepo.getLesson(lessonId)
            val queue = contentRepo.buildLessonPlaybackQueue(
                lessonId,
                com.koreanimmersion.data.local.DatabaseSeeder.DEFAULT_USER_ID
            )
            val phraseIds = contentRepo.getPhrasesForLesson(lessonId).map { it.id }
            contentRepo.initializeSrsForNewPhrases(
                com.koreanimmersion.data.local.DatabaseSeeder.DEFAULT_USER_ID,
                phraseIds
            )
            val progress = progressRepo.getProgress(lessonId)
            _uiState.value = _uiState.value.copy(
                lessonId = lessonId,
                lessonTitle = lesson?.title,
                segments = queue ?: emptyList(),
                isQueueReady = queue != null && queue.isNotEmpty(),
                examAvailable = progress?.examAvailable == true
            )
        }
    }

    fun startPlayback(replayLoop: Boolean = false) {
        val state = _uiState.value
        val segments = state.segments
        if (segments.isEmpty()) return

        _uiState.value = state.copy(isPlaying = true, isReplayLoop = replayLoop)

        val intent = Intent(appContext, AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_START
            putParcelableArrayListExtra(
                AudioPlaybackService.EXTRA_SEGMENTS,
                ArrayList(segments.map { PlaybackSegmentParcelable.fromDomain(it) })
            )
            putExtra(AudioPlaybackService.EXTRA_LESSON_ID, state.lessonId ?: -1L)
            putExtra(AudioPlaybackService.EXTRA_REPLAY_LOOP, replayLoop)
        }
        ContextCompat.startForegroundService(appContext, intent)
    }

    fun stopPlayback() {
        val intent = Intent(appContext, AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_STOP
        }
        appContext.startService(intent)
    }

    fun onScreenLocked() {
        screenWasLocked = true
        _uiState.value = _uiState.value.copy(showExamPrompt = false)
    }

    fun onScreenUnlocked() {
        if (screenWasLocked) {
            screenWasLocked = false
            pendingExamPromptLessonId?.let { offerExamPrompt(it) }
        }
    }

    private fun offerExamPrompt(lessonId: Long) {
        if (screenWasLocked) {
            pendingExamPromptLessonId = lessonId
            return
        }
        viewModelScope.launch {
            if (progressRepo.shouldShowExamPrompt(lessonId)) {
                _uiState.value = _uiState.value.copy(
                    showExamPrompt = true,
                    examPromptLessonId = lessonId
                )
                pendingExamPromptLessonId = null
            }
        }
    }

    fun startExamFromLesson() {
        val lessonId = _uiState.value.lessonId ?: return
        viewModelScope.launch {
            progressRepo.clearExamPrompt(lessonId)
            _uiState.value = _uiState.value.copy(
                navigateToExamLessonId = lessonId,
                showExamPrompt = false,
                examPromptLessonId = null,
                examAvailable = false
            )
        }
    }

    fun dismissExamPrompt() {
        _uiState.value = _uiState.value.copy(showExamPrompt = false, examPromptLessonId = null)
        pendingExamPromptLessonId = null
    }

    fun acceptExamPrompt() {
        val lessonId = _uiState.value.examPromptLessonId ?: return
        viewModelScope.launch {
            progressRepo.clearExamPrompt(lessonId)
            _uiState.value = _uiState.value.copy(
                showExamPrompt = false,
                examPromptLessonId = null,
                examAvailable = false,
                navigateToExamLessonId = lessonId
            )
            pendingExamPromptLessonId = null
        }
    }

    fun clearNavigateToExam() {
        _uiState.value = _uiState.value.copy(navigateToExamLessonId = null)
    }

    override fun onCleared() {
        appContext.unregisterReceiver(playbackReceiver)
        super.onCleared()
    }

    data class LessonPlayerUiState(
        val lessonId: Long? = null,
        val lessonTitle: String? = null,
        val segments: List<PlaybackSegment> = emptyList(),
        val isQueueReady: Boolean = false,
        val isPlaying: Boolean = false,
        val isReplayLoop: Boolean = false,
        val fullPlaythroughsRecorded: Int = 0,
        val examAvailable: Boolean = false,
        val showExamPrompt: Boolean = false,
        val examPromptLessonId: Long? = null,
        val navigateToExamLessonId: Long? = null
    )
}

class ManualTopicViewModel(app: Application) : AndroidViewModel(app) {

    private val appContext = app.applicationContext
    private val contentRepo = KoreanImmersionApp.instance.contentRepository

    private val _segments = MutableStateFlow<List<PlaybackSegment>>(emptyList())
    val segments: StateFlow<List<PlaybackSegment>> = _segments.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isReplayLoop = MutableStateFlow(false)
    val isReplayLoop: StateFlow<Boolean> = _isReplayLoop.asStateFlow()

    private val _isQueueReady = MutableStateFlow(false)
    val isQueueReady: StateFlow<Boolean> = _isQueueReady.asStateFlow()

    private val playbackReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                AudioPlaybackService.ACTION_PLAYBACK_STARTED -> {
                    _isPlaying.value = true
                    _isReplayLoop.value = intent.getBooleanExtra(
                        AudioPlaybackService.EXTRA_REPLAY_LOOP, false
                    )
                }
                AudioPlaybackService.ACTION_PLAYBACK_STOPPED,
                AudioPlaybackService.ACTION_USER_STOP -> {
                    _isPlaying.value = false
                    _isReplayLoop.value = false
                }
            }
        }
    }

    init {
        val filter = IntentFilter().apply {
            addAction(AudioPlaybackService.ACTION_PLAYBACK_STARTED)
            addAction(AudioPlaybackService.ACTION_PLAYBACK_STOPPED)
            addAction(AudioPlaybackService.ACTION_USER_STOP)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.registerReceiver(
                appContext, playbackReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
            )
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            appContext.registerReceiver(playbackReceiver, filter)
        }
    }

    fun loadTopic(topicId: Long) {
        viewModelScope.launch {
            _isQueueReady.value = false
            _segments.value = contentRepo.buildManualTopicQueue(topicId)
            _isQueueReady.value = _segments.value.isNotEmpty()
        }
    }

    fun startPlayback(replayLoop: Boolean = false) {
        val segments = _segments.value
        if (segments.isEmpty()) return

        _isPlaying.value = true
        _isReplayLoop.value = replayLoop

        val intent = Intent(appContext, AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_START
            putParcelableArrayListExtra(
                AudioPlaybackService.EXTRA_SEGMENTS,
                ArrayList(segments.map { PlaybackSegmentParcelable.fromDomain(it) })
            )
            putExtra(AudioPlaybackService.EXTRA_REPLAY_LOOP, replayLoop)
        }
        ContextCompat.startForegroundService(appContext, intent)
    }

    fun stopPlayback() {
        val intent = Intent(appContext, AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_STOP
        }
        appContext.startService(intent)
    }

    override fun onCleared() {
        appContext.unregisterReceiver(playbackReceiver)
        super.onCleared()
    }
}

class ProgressViewModel(app: Application) : AndroidViewModel(app) {

    private val progressRepo = KoreanImmersionApp.instance.progressRepository
    private val contentRepo = KoreanImmersionApp.instance.contentRepository

    val lessonScores = progressRepo.observeLessonScores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun observeExamHistory(lessonId: Long): kotlinx.coroutines.flow.StateFlow<List<ExamAttemptEntity>> =
        progressRepo.observeExamHistory(lessonId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    suspend fun getAllLessons(): List<LessonEntity> = contentRepo.let {
        KoreanImmersionApp.instance.database.lessonDao().getAll()
    }
}

class ExamViewModel(app: Application) : AndroidViewModel(app) {

    private val examRepo = KoreanImmersionApp.instance.examRepository
    private val progressRepo = KoreanImmersionApp.instance.progressRepository

    private val _uiState = MutableStateFlow(ExamUiState())
    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

    fun loadExam(lessonId: Long) {
        viewModelScope.launch {
            val questions = examRepo.buildMcQuestions(lessonId)
            _uiState.value = ExamUiState(
                lessonId = lessonId,
                mcQuestions = questions,
                phase = if (questions.isEmpty()) ExamPhase.RESULT else ExamPhase.MULTIPLE_CHOICE,
                totalCount = questions.size
            )
        }
    }

    fun submitMcAnswer(questionIndex: Int, selectedIndex: Int) {
        val state = _uiState.value
        val question = state.mcQuestions.getOrNull(questionIndex) ?: return
        val answer = com.koreanimmersion.domain.exam.ExamAnswerRecord(
            phraseId = question.phraseId,
            selectedIndex = selectedIndex,
            correct = selectedIndex == question.correctIndex
        )
        val answers = state.mcAnswers + answer
        val isLast = questionIndex + 1 >= state.mcQuestions.size
        _uiState.value = state.copy(
            mcAnswers = answers,
            currentMcIndex = questionIndex + 1
        )
        if (isLast) {
            finishExam(answers)
        }
    }

    private fun finishExam(answers: List<com.koreanimmersion.domain.exam.ExamAnswerRecord>) {
        viewModelScope.launch {
            val state = _uiState.value
            val lessonId = state.lessonId ?: return@launch
            val attempt = examRepo.saveAttempt(lessonId, answers)
            progressRepo.clearExamPrompt(lessonId)
            val correct = answers.count { it.correct }
            _uiState.value = state.copy(
                mcAnswers = answers,
                phase = ExamPhase.RESULT,
                finalScore = attempt.score,
                correctCount = correct,
                totalCount = answers.size
            )
        }
    }

    data class ExamUiState(
        val lessonId: Long? = null,
        val mcQuestions: List<com.koreanimmersion.domain.exam.McQuestion> = emptyList(),
        val mcAnswers: List<com.koreanimmersion.domain.exam.ExamAnswerRecord> = emptyList(),
        val currentMcIndex: Int = 0,
        val phase: ExamPhase = ExamPhase.LOADING,
        val finalScore: Int? = null,
        val correctCount: Int? = null,
        val totalCount: Int? = null
    )

    enum class ExamPhase { LOADING, MULTIPLE_CHOICE, RESULT }
}
