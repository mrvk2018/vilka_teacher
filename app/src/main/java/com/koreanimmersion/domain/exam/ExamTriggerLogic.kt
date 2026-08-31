package com.koreanimmersion.domain.exam

import com.koreanimmersion.data.local.entity.UserLessonProgressEntity

/**
 * Триггер экзамена: ОБА условия одновременно —
 * (a) полное прослушивание хотя бы 1 раз
 * (b) пользователь нажал Stop
 *
 * Диалог показывается только при разблокировке экрана, если оба флага выставлены
 * и пользователь явно нажал Stop (не во время replay loop).
 */
object ExamTriggerLogic {

    data class ExamPromptState(
        val shouldShowPrompt: Boolean,
        val lessonId: Long?,
        val reason: String? = null
    )

    fun evaluateAfterStop(
        progress: UserLessonProgressEntity?,
        stopPressedAt: Long
    ): ExamPromptState {
        if (progress == null) {
            return ExamPromptState(false, null, "no_progress")
        }
        val hasFullPlaythrough = progress.timesCompletedFullPlaythrough >= 1
        val stopWasPressed = progress.lastStopPressedAt != null &&
            progress.lastStopPressedAt == stopPressedAt

        return if (hasFullPlaythrough && stopWasPressed && progress.examAvailable) {
            ExamPromptState(true, progress.lessonId)
        } else {
            ExamPromptState(
                shouldShowPrompt = false,
                lessonId = progress.lessonId,
                reason = when {
                    !hasFullPlaythrough -> "no_full_playthrough"
                    !stopWasPressed -> "stop_not_recorded"
                    !progress.examAvailable -> "exam_not_marked_available"
                    else -> null
                }
            )
        }
    }

    /**
     * Вызывается при завершении полного цикла урока (intro → phrases → srs review).
     * НЕ показывает диалог — только инкрементирует счётчик.
     * examAvailable выставляется только при Stop, не при автоматическом завершении цикла в replay.
     */
    fun onFullPlaythroughCompleted(progress: UserLessonProgressEntity?): UserLessonProgressEntity {
        val base = progress ?: return UserLessonProgressEntity(
            userId = "",
            lessonId = 0,
            timesCompletedFullPlaythrough = 1
        )
        return base.copy(
            timesCompletedFullPlaythrough = base.timesCompletedFullPlaythrough + 1
        )
    }

    /**
     * Вызывается при нажатии Stop пользователем.
     * examAvailable = true только если уже было хотя бы одно полное прослушивание.
     */
    fun onStopPressed(
        progress: UserLessonProgressEntity?,
        userId: String,
        lessonId: Long,
        timestamp: Long = System.currentTimeMillis()
    ): UserLessonProgressEntity {
        val base = progress ?: UserLessonProgressEntity(userId = userId, lessonId = lessonId)
        val examAvailable = base.timesCompletedFullPlaythrough >= 1
        return base.copy(
            lastStopPressedAt = timestamp,
            examAvailable = examAvailable
        )
    }

    /**
     * После показа диалога или начала экзамена — сброс examAvailable,
     * чтобы не показывать повторно при разблокировке.
     */
    fun clearExamAvailable(progress: UserLessonProgressEntity): UserLessonProgressEntity {
        return progress.copy(examAvailable = false)
    }
}
