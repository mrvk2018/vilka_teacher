package com.koreanimmersion.domain.playback

enum class PlaybackMode {
    LESSON,
    MANUAL_TOPIC,
    REPLAY_LOOP
}

data class PlaybackSessionState(
    val lessonId: Long? = null,
    val topicId: Long? = null,
    val mode: PlaybackMode = PlaybackMode.LESSON,
    val isReplayLoop: Boolean = false,
    val currentSegmentIndex: Int = 0,
    val segments: List<PlaybackSegment> = emptyList(),
    val isPlaying: Boolean = false,
    val hasCompletedFullCycleThisSession: Boolean = false,
    /** Полные прослушивания в текущей replay-сессии (для инкремента только при полном цикле). */
    val fullCyclesCompletedInSession: Int = 0
)
