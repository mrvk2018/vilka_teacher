package com.koreanimmersion.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.koreanimmersion.data.local.entity.ExamAttemptEntity
import com.koreanimmersion.data.local.entity.LessonEntity
import com.koreanimmersion.data.local.entity.PhraseEntity
import com.koreanimmersion.data.local.entity.PhraseSrsEntity
import com.koreanimmersion.data.local.entity.TopicEntity
import com.koreanimmersion.data.local.entity.UserLessonProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics ORDER BY orderPriority ASC")
    fun observeAll(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics ORDER BY orderPriority ASC")
    suspend fun getAll(): List<TopicEntity>

    @Query("SELECT * FROM topics WHERE id = :topicId")
    suspend fun getById(topicId: Long): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(topics: List<TopicEntity>)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE topicId = :topicId ORDER BY orderInTopic ASC")
    fun observeByTopic(topicId: Long): Flow<List<LessonEntity>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    suspend fun getById(lessonId: Long): LessonEntity?

    @Query("SELECT * FROM lessons ORDER BY topicId, orderInTopic")
    suspend fun getAll(): List<LessonEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(lessons: List<LessonEntity>)
}

@Dao
interface PhraseDao {
    @Query("SELECT * FROM phrases WHERE lessonId = :lessonId ORDER BY id ASC")
    suspend fun getByLesson(lessonId: Long): List<PhraseEntity>

    @Query("SELECT * FROM phrases WHERE lessonId IN (:lessonIds) ORDER BY lessonId, id")
    suspend fun getByLessons(lessonIds: List<Long>): List<PhraseEntity>

    @Query("SELECT * FROM phrases WHERE id IN (:phraseIds)")
    suspend fun getByIds(phraseIds: List<Long>): List<PhraseEntity>

    @Query("SELECT p.* FROM phrases p INNER JOIN lessons l ON p.lessonId = l.id WHERE l.topicId = :topicId ORDER BY l.orderInTopic, p.id")
    suspend fun getByTopic(topicId: Long): List<PhraseEntity>

    @Query("SELECT * FROM phrases ORDER BY id ASC")
    suspend fun getAll(): List<PhraseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(phrases: List<PhraseEntity>)
}

@Dao
interface UserLessonProgressDao {
    @Query("SELECT * FROM user_lesson_progress WHERE userId = :userId AND lessonId = :lessonId")
    suspend fun get(userId: String, lessonId: Long): UserLessonProgressEntity?

    @Query("SELECT * FROM user_lesson_progress WHERE userId = :userId")
    fun observeAll(userId: String): Flow<List<UserLessonProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: UserLessonProgressEntity)

    @Update
    suspend fun update(progress: UserLessonProgressEntity)
}

@Dao
interface ExamAttemptDao {
    @Query("SELECT * FROM exam_attempts WHERE userId = :userId AND lessonId = :lessonId ORDER BY attemptNumber DESC")
    fun observeByLesson(userId: String, lessonId: Long): Flow<List<ExamAttemptEntity>>

    @Query("SELECT * FROM exam_attempts WHERE userId = :userId ORDER BY date DESC")
    fun observeAll(userId: String): Flow<List<ExamAttemptEntity>>

    @Query("SELECT * FROM exam_attempts WHERE userId = :userId AND lessonId = :lessonId ORDER BY attemptNumber DESC LIMIT 1")
    suspend fun getLatest(userId: String, lessonId: Long): ExamAttemptEntity?

    @Query("SELECT COUNT(*) FROM exam_attempts WHERE userId = :userId AND lessonId = :lessonId")
    suspend fun countAttempts(userId: String, lessonId: Long): Int

    @Insert
    suspend fun insert(attempt: ExamAttemptEntity): Long
}

@Dao
interface PhraseSrsDao {
    @Query(
        """
        SELECT * FROM phrase_srs
        WHERE userId = :userId AND nextReviewDate <= :todayMillis
        ORDER BY nextReviewDate ASC
        """
    )
    suspend fun getDueForReview(userId: String, todayMillis: Long): List<PhraseSrsEntity>

    @Query("SELECT * FROM phrase_srs WHERE userId = :userId AND phraseId = :phraseId")
    suspend fun get(userId: String, phraseId: Long): PhraseSrsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: PhraseSrsEntity)

    @Query("SELECT * FROM phrase_srs WHERE userId = :userId")
    fun observeAll(userId: String): Flow<List<PhraseSrsEntity>>
}

data class LessonWithLatestScore(
    val lessonId: Long,
    val topicId: Long,
    val title: String,
    val orderInTopic: Int,
    val latestScore: Int?,
    val attemptCount: Int,
    val examAvailable: Boolean,
    val timesCompleted: Int
)

@Dao
interface ProgressDao {
    @Transaction
    @Query(
        """
        SELECT
            l.id AS lessonId,
            l.topicId AS topicId,
            l.title AS title,
            l.orderInTopic AS orderInTopic,
            (SELECT ea.score FROM exam_attempts ea
             WHERE ea.userId = :userId AND ea.lessonId = l.id
             ORDER BY ea.attemptNumber DESC LIMIT 1) AS latestScore,
            (SELECT COUNT(*) FROM exam_attempts ea
             WHERE ea.userId = :userId AND ea.lessonId = l.id) AS attemptCount,
            COALESCE(ulp.examAvailable, 0) AS examAvailable,
            COALESCE(ulp.timesCompletedFullPlaythrough, 0) AS timesCompleted
        FROM lessons l
        LEFT JOIN user_lesson_progress ulp
            ON ulp.lessonId = l.id AND ulp.userId = :userId
        ORDER BY l.topicId, l.orderInTopic
        """
    )
    fun observeLessonsWithScores(userId: String): Flow<List<LessonWithLatestScore>>
}
