package com.koreanimmersion.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.koreanimmersion.KoreanImmersionApp
import com.koreanimmersion.core.database.entity.PhraseEntity
import com.koreanimmersion.core.database.entity.TopicEntity
import com.koreanimmersion.service.PhraseIntroductionPlaybackService
import com.koreanimmersion.service.PhrasePlaybackItemParcelable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PhraseIntroductionViewModel(app: Application) : AndroidViewModel(app) {

    private val appContext = app.applicationContext
    private val courseRepo = KoreanImmersionApp.instance.courseContentRepository

    private val _topic = MutableStateFlow<TopicEntity?>(null)
    val topic: StateFlow<TopicEntity?> = _topic.asStateFlow()

    private val _isStarting = MutableStateFlow(false)
    val isStarting: StateFlow<Boolean> = _isStarting.asStateFlow()

    fun loadTopic(topicId: Long) {
        viewModelScope.launch {
            _topic.value = courseRepo.getTopic(topicId)
        }
    }

    fun observePhrases(topicId: Long): StateFlow<List<PhraseEntity>> =
        courseRepo.observePhrases(topicId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun startAudioLesson(topicId: Long, phrases: List<PhraseEntity>) {
        if (phrases.isEmpty()) return
        viewModelScope.launch {
            _isStarting.value = true
            val parcelables = ArrayList(phrases.map { PhrasePlaybackItemParcelable.fromCoursePhrase(it) })
            val intent = Intent(appContext, PhraseIntroductionPlaybackService::class.java).apply {
                action = PhraseIntroductionPlaybackService.ACTION_START
                putExtra(PhraseIntroductionPlaybackService.EXTRA_TOPIC_ID, topicId)
                putParcelableArrayListExtra(PhraseIntroductionPlaybackService.EXTRA_PHRASES, parcelables)
            }
            ContextCompat.startForegroundService(appContext, intent)
            _isStarting.value = false
        }
    }
}
