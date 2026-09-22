package com.koreanimmersion.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/** Тема курса (подэтап 1 — ознакомление). Таблица отделена от legacy `topics`. */
@Entity(tableName = "course_topics")
data class TopicEntity(
    @PrimaryKey val id: Long,
    @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "title_ru") val titleRu: String,
    @ColumnInfo(name = "title_en") val titleEn: String,
    @ColumnInfo(name = "order_index") val order: Int
)
