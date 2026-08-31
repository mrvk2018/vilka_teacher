package com.koreanimmersion.domain.srs

import com.koreanimmersion.data.local.entity.PhraseSrsEntity
import java.util.concurrent.TimeUnit

/**
 * Упрощённый SM-2: интервалы 1 → 3 → 7 → 14 → 21 → 30 дней.
 * После ~30 дней фраза считается закреплённой (интервал остаётся 30).
 */
object SrsScheduler {

    private val INTERVALS_DAYS = listOf(1, 3, 7, 14, 21, 30)
    const val MASTERED_INTERVAL_DAYS = 30

    fun createInitialEntry(userId: String, phraseId: Long, nowMillis: Long = System.currentTimeMillis()): PhraseSrsEntity {
        return PhraseSrsEntity(
            userId = userId,
            phraseId = phraseId,
            nextReviewDate = nowMillis + daysToMillis(INTERVALS_DAYS.first()),
            currentIntervalDays = INTERVALS_DAYS.first(),
            timesReviewed = 0
        )
    }

    fun scheduleAfterReview(existing: PhraseSrsEntity, nowMillis: Long = System.currentTimeMillis()): PhraseSrsEntity {
        val nextIndex = (INTERVALS_DAYS.indexOf(existing.currentIntervalDays) + 1)
            .coerceAtMost(INTERVALS_DAYS.lastIndex)
        val nextInterval = INTERVALS_DAYS[nextIndex]
        return existing.copy(
            nextReviewDate = nowMillis + daysToMillis(nextInterval),
            currentIntervalDays = nextInterval,
            timesReviewed = existing.timesReviewed + 1
        )
    }

    fun isMastered(entry: PhraseSrsEntity): Boolean =
        entry.currentIntervalDays >= MASTERED_INTERVAL_DAYS && entry.timesReviewed >= INTERVALS_DAYS.lastIndex

    fun daysToMillis(days: Int): Long = TimeUnit.DAYS.toMillis(days.toLong())
}
