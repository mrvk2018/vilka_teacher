package com.koreanimmersion.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.koreanimmersion.core.database.model.LearningSubStage

/**
 * Агрегированный прогресс пользователя по текущей теме и этапу.
 * Заученные фразы — JSON-массив ID ([10101, 130104, …]) для совместимости с Python-скриптами.
 */
@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val userId: String,
    /** ID темы в контенте (1 — магазин, 5 — такси, 6 — аэропорт, …). */
    val currentTopicId: Long? = null,
    /** Стабильный ключ темы (shop, taxi, airport, …) для обмена с backend/скриптами. */
    val currentTopicKey: String? = null,
    /** @see LearningSubStage.code */
    val currentSubStage: Int = LearningSubStage.INTRODUCTION.code,
    val learnedPhraseIdsJson: String = "[]"
)
