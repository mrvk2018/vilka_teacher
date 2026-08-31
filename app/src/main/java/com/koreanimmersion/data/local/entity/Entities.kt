package com.koreanimmersion.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val nameDisplay: String,
    val orderPriority: Int,
    val icon: String
)

@Entity(
    tableName = "lessons",
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topicId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("topicId")]
)
data class LessonEntity(
    @PrimaryKey val id: Long,
    val topicId: Long,
    val orderInTopic: Int,
    val title: String,
    val introAudioUrl: String,
    val durationTargetMin: Int = 15
)

@Entity(
    tableName = "phrases",
    foreignKeys = [
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("lessonId")]
)
data class PhraseEntity(
    @PrimaryKey val id: Long,
    val lessonId: Long,
    val koreanText: String,
    val koreanRomanization: String,
    val russianContext: String,
    val russianLiteralMeaning: String? = null,
    val audioUrlKorean: String,
    val formalityLevel: String = "요체"
)

@Entity(
    tableName = "user_lesson_progress",
    primaryKeys = ["userId", "lessonId"],
    foreignKeys = [
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("lessonId")]
)
data class UserLessonProgressEntity(
    val userId: String,
    val lessonId: Long,
    val timesCompletedFullPlaythrough: Int = 0,
    val lastStopPressedAt: Long? = null,
    val examAvailable: Boolean = false
)

@Entity(
    tableName = "exam_attempts",
    foreignKeys = [
        ForeignKey(
            entity = LessonEntity::class,
            parentColumns = ["id"],
            childColumns = ["lessonId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("lessonId"), Index("userId")]
)
data class ExamAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val lessonId: Long,
    val attemptNumber: Int,
    val score: Int,
    val date: Long,
    val answersJson: String
)

@Entity(
    tableName = "phrase_srs",
    primaryKeys = ["userId", "phraseId"],
    foreignKeys = [
        ForeignKey(
            entity = PhraseEntity::class,
            parentColumns = ["id"],
            childColumns = ["phraseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("phraseId"), Index("nextReviewDate")]
)
data class PhraseSrsEntity(
    val userId: String,
    val phraseId: Long,
    val nextReviewDate: Long,
    val currentIntervalDays: Int,
    val timesReviewed: Int = 0
)

/** Задел на Этап 2 — изучение Хангыль, пока не используется. */
@Entity(tableName = "hangul_stage_placeholder")
data class HangulStagePlaceholderEntity(
    @PrimaryKey val id: Long = 1,
    val enabled: Boolean = false
)
