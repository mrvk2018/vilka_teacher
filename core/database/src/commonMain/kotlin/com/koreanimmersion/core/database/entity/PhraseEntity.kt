package com.koreanimmersion.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "course_phrases",
    foreignKeys = [
        ForeignKey(
            entity = TopicEntity::class,
            parentColumns = ["id"],
            childColumns = ["topic_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("topic_id")]
)
data class PhraseEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "topic_id") val topicId: Long,
    @ColumnInfo(name = "korean_text") val koreanText: String,
    @ColumnInfo(name = "russian_translation") val russianTranslation: String,
    @ColumnInfo(name = "english_translation") val englishTranslation: String,
    @ColumnInfo(name = "audio_url_or_path") val audioUrlOrPath: String = "",
    @ColumnInfo(name = "order_index") val order: Int
)
