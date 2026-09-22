package com.koreanimmersion.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object AppDatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS user_progress (
                    userId TEXT NOT NULL PRIMARY KEY,
                    currentTopicId INTEGER,
                    currentTopicKey TEXT,
                    currentSubStage INTEGER NOT NULL,
                    learnedPhraseIdsJson TEXT NOT NULL
                )
                """.trimIndent()
            )
        }
    }

    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS course_topics (
                    id INTEGER NOT NULL PRIMARY KEY,
                    key TEXT NOT NULL,
                    title_ru TEXT NOT NULL,
                    title_en TEXT NOT NULL,
                    order_index INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS course_phrases (
                    id INTEGER NOT NULL PRIMARY KEY,
                    topic_id INTEGER NOT NULL,
                    korean_text TEXT NOT NULL,
                    russian_translation TEXT NOT NULL,
                    english_translation TEXT NOT NULL,
                    audio_url_or_path TEXT NOT NULL,
                    order_index INTEGER NOT NULL,
                    FOREIGN KEY(topic_id) REFERENCES course_topics(id) ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS index_course_phrases_topic_id ON course_phrases(topic_id)"
            )
        }
    }
}
