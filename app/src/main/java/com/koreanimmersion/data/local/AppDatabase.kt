package com.koreanimmersion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.koreanimmersion.data.local.dao.ExamAttemptDao
import com.koreanimmersion.data.local.dao.LessonDao
import com.koreanimmersion.data.local.dao.PhraseDao
import com.koreanimmersion.data.local.dao.PhraseSrsDao
import com.koreanimmersion.data.local.dao.ProgressDao
import com.koreanimmersion.data.local.dao.TopicDao
import com.koreanimmersion.data.local.dao.UserLessonProgressDao
import com.koreanimmersion.data.local.entity.ExamAttemptEntity
import com.koreanimmersion.data.local.entity.HangulStagePlaceholderEntity
import com.koreanimmersion.data.local.entity.LessonEntity
import com.koreanimmersion.data.local.entity.PhraseEntity
import com.koreanimmersion.data.local.entity.PhraseSrsEntity
import com.koreanimmersion.data.local.entity.TopicEntity
import com.koreanimmersion.data.local.entity.UserLessonProgressEntity

@Database(
    entities = [
        TopicEntity::class,
        LessonEntity::class,
        PhraseEntity::class,
        UserLessonProgressEntity::class,
        ExamAttemptEntity::class,
        PhraseSrsEntity::class,
        HangulStagePlaceholderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
    abstract fun lessonDao(): LessonDao
    abstract fun phraseDao(): PhraseDao
    abstract fun userLessonProgressDao(): UserLessonProgressDao
    abstract fun examAttemptDao(): ExamAttemptDao
    abstract fun phraseSrsDao(): PhraseSrsDao
    abstract fun progressDao(): ProgressDao
}
