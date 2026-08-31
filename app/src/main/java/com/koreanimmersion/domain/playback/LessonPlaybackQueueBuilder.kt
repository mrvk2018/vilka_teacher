package com.koreanimmersion.domain.playback

import com.koreanimmersion.data.local.entity.PhraseEntity

enum class PlaybackSegmentType {
    INTRO,
    PHRASE_CONTEXT,
    PHRASE_AUDIO,
    PHRASE_USER_PAUSE,
    SRS_REVIEW_CONTEXT,
    SRS_REVIEW_AUDIO,
    SRS_USER_PAUSE
}

data class PlaybackSegment(
    val type: PlaybackSegmentType,
    val audioUrl: String? = null,
    val displayText: String? = null,
    val phraseId: Long? = null,
    val pauseDurationMs: Long = 0L
)

/**
 * Строит очередь воспроизведения урока (~15 мин):
 * intro → новые фразы (контекст + аудио + пауза) → SRS-повтор (5 мин блок).
 */
object LessonPlaybackQueueBuilder {

    private const val USER_REPEAT_PAUSE_MS = 4_000L
    private const val SRS_BLOCK_TARGET_MS = 5 * 60 * 1000L

    fun buildLessonQueue(
        introAudioUrl: String,
        newPhrases: List<PhraseEntity>,
        srsPhrases: List<PhraseEntity>
    ): List<PlaybackSegment> {
        val segments = mutableListOf<PlaybackSegment>()

        segments += PlaybackSegment(
            type = PlaybackSegmentType.INTRO,
            audioUrl = introAudioUrl,
            displayText = "Интро: метод фонетического погружения"
        )

        newPhrases.forEach { phrase ->
            segments += PlaybackSegment(
                type = PlaybackSegmentType.PHRASE_CONTEXT,
                displayText = phrase.russianContext,
                phraseId = phrase.id
            )
            segments += PlaybackSegment(
                type = PlaybackSegmentType.PHRASE_AUDIO,
                audioUrl = phrase.audioUrlKorean,
                displayText = phrase.koreanText,
                phraseId = phrase.id
            )
            segments += PlaybackSegment(
                type = PlaybackSegmentType.PHRASE_USER_PAUSE,
                displayText = "Повтори вслух: ${phrase.koreanRomanization}",
                phraseId = phrase.id,
                pauseDurationMs = USER_REPEAT_PAUSE_MS
            )
        }

        if (srsPhrases.isNotEmpty()) {
            segments += PlaybackSegment(
                type = PlaybackSegmentType.SRS_REVIEW_CONTEXT,
                displayText = "Повторение из предыдущих уроков"
            )
            srsPhrases.forEach { phrase ->
                segments += PlaybackSegment(
                    type = PlaybackSegmentType.SRS_REVIEW_AUDIO,
                    audioUrl = phrase.audioUrlKorean,
                    displayText = phrase.koreanText,
                    phraseId = phrase.id
                )
                segments += PlaybackSegment(
                    type = PlaybackSegmentType.SRS_USER_PAUSE,
                    displayText = "Повтори: ${phrase.koreanRomanization}",
                    phraseId = phrase.id,
                    pauseDurationMs = USER_REPEAT_PAUSE_MS
                )
            }
        }

        return segments
    }

    /** Ручной режим: все фразы темы без intro и без SRS-фильтра. */
    fun buildManualTopicQueue(phrases: List<PhraseEntity>): List<PlaybackSegment> {
        return phrases.flatMap { phrase ->
            listOf(
                PlaybackSegment(
                    type = PlaybackSegmentType.PHRASE_CONTEXT,
                    displayText = phrase.russianContext,
                    phraseId = phrase.id
                ),
                PlaybackSegment(
                    type = PlaybackSegmentType.PHRASE_AUDIO,
                    audioUrl = phrase.audioUrlKorean,
                    displayText = phrase.koreanText,
                    phraseId = phrase.id
                ),
                PlaybackSegment(
                    type = PlaybackSegmentType.PHRASE_USER_PAUSE,
                    displayText = "Повтори: ${phrase.koreanRomanization}",
                    phraseId = phrase.id,
                    pauseDurationMs = USER_REPEAT_PAUSE_MS
                )
            )
        }
    }

    fun estimateDurationMs(segments: List<PlaybackSegment>, avgAudioMs: Long = 3_000L): Long {
        return segments.sumOf { seg ->
            when (seg.type) {
                PlaybackSegmentType.PHRASE_USER_PAUSE,
                PlaybackSegmentType.SRS_USER_PAUSE -> seg.pauseDurationMs
                PlaybackSegmentType.PHRASE_CONTEXT,
                PlaybackSegmentType.SRS_REVIEW_CONTEXT -> 2_000L
                else -> if (seg.audioUrl != null) avgAudioMs else 0L
            }
        }.coerceAtMost(SRS_BLOCK_TARGET_MS + newPhraseBlockEstimate(segments, avgAudioMs))
    }

    private fun newPhraseBlockEstimate(segments: List<PlaybackSegment>, avgAudioMs: Long): Long {
        return segments.count {
            it.type == PlaybackSegmentType.PHRASE_AUDIO ||
                it.type == PlaybackSegmentType.PHRASE_USER_PAUSE
        } * (avgAudioMs + USER_REPEAT_PAUSE_MS) / 2
    }
}
