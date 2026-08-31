package com.koreanimmersion.data.repository

import com.koreanimmersion.data.local.AppDatabase
import com.koreanimmersion.data.local.DatabaseSeeder
import com.koreanimmersion.data.local.dao.LessonWithLatestScore
import com.koreanimmersion.data.local.entity.ExamAttemptEntity
import com.koreanimmersion.data.local.entity.LessonEntity
import com.koreanimmersion.data.local.entity.PhraseEntity
import com.koreanimmersion.data.local.entity.PhraseSrsEntity
import com.koreanimmersion.data.local.entity.TopicEntity
import com.koreanimmersion.data.local.entity.UserLessonProgressEntity
import com.koreanimmersion.domain.exam.ExamAnswerRecord
import com.koreanimmersion.domain.exam.ExamScoring
import com.koreanimmersion.domain.exam.ExamTriggerLogic
import com.koreanimmersion.domain.exam.McQuestion
import com.koreanimmersion.domain.playback.LessonPlaybackQueueBuilder
import com.koreanimmersion.domain.playback.PlaybackSegment
import com.koreanimmersion.domain.srs.SrsScheduler
import kotlinx.coroutines.flow.Flow

class ContentRepository(private val db: AppDatabase) {

    fun observeTopics(): Flow<List<TopicEntity>> = db.topicDao().observeAll()

    fun observeLessons(topicId: Long) = db.lessonDao().observeByTopic(topicId)

    suspend fun getTopic(topicId: Long): TopicEntity? = db.topicDao().getById(topicId)

    suspend fun getLesson(lessonId: Long): LessonEntity? = db.lessonDao().getById(lessonId)

    suspend fun getPhrasesForLesson(lessonId: Long): List<PhraseEntity> =
        db.phraseDao().getByLesson(lessonId)

    suspend fun getPhrasesForTopic(topicId: Long): List<PhraseEntity> =
        db.phraseDao().getByTopic(topicId)

    suspend fun getDueSrsPhrases(userId: String): List<PhraseEntity> {
        val due = db.phraseSrsDao().getDueForReview(userId, System.currentTimeMillis())
        if (due.isEmpty()) return emptyList()
        return db.phraseDao().getByIds(due.map { it.phraseId })
    }

    suspend fun buildLessonPlaybackQueue(lessonId: Long, userId: String): List<PlaybackSegment>? {
        val lesson = getLesson(lessonId) ?: return null
        val newPhrases = getPhrasesForLesson(lessonId)
        val srsPhrases = getDueSrsPhrases(userId)
        return LessonPlaybackQueueBuilder.buildLessonQueue(
            introAudioUrl = lesson.introAudioUrl,
            newPhrases = newPhrases,
            srsPhrases = srsPhrases
        )
    }

    suspend fun buildManualTopicQueue(topicId: Long): List<PlaybackSegment> {
        val phrases = getPhrasesForTopic(topicId)
        return LessonPlaybackQueueBuilder.buildManualTopicQueue(phrases)
    }

    suspend fun initializeSrsForNewPhrases(userId: String, phraseIds: List<Long>) {
        phraseIds.forEach { phraseId ->
            val existing = db.phraseSrsDao().get(userId, phraseId)
            if (existing == null) {
                db.phraseSrsDao().upsert(SrsScheduler.createInitialEntry(userId, phraseId))
            }
        }
    }

    suspend fun markSrsReviewed(userId: String, phraseId: Long) {
        val existing = db.phraseSrsDao().get(userId, phraseId) ?: return
        db.phraseSrsDao().upsert(SrsScheduler.scheduleAfterReview(existing))
    }
}

class ProgressRepository(private val db: AppDatabase) {

    private val userId get() = DatabaseSeeder.DEFAULT_USER_ID

    fun observeLessonScores(): Flow<List<LessonWithLatestScore>> =
        db.progressDao().observeLessonsWithScores(userId)

    fun observeExamHistory(lessonId: Long) =
        db.examAttemptDao().observeByLesson(userId, lessonId)

    suspend fun getProgress(lessonId: Long): UserLessonProgressEntity? =
        db.userLessonProgressDao().get(userId, lessonId)

    suspend fun recordFullPlaythrough(lessonId: Long) {
        val current = getProgress(lessonId)
        val updated = ExamTriggerLogic.onFullPlaythroughCompleted(
            current?.copy(userId = userId, lessonId = lessonId)
                ?: UserLessonProgressEntity(userId = userId, lessonId = lessonId)
        )
        db.userLessonProgressDao().upsert(updated)
    }

    suspend fun recordStopPressed(lessonId: Long): UserLessonProgressEntity {
        val updated = ExamTriggerLogic.onStopPressed(getProgress(lessonId), userId, lessonId)
        db.userLessonProgressDao().upsert(updated)
        return updated
    }

    suspend fun clearExamPrompt(lessonId: Long) {
        val current = getProgress(lessonId) ?: return
        db.userLessonProgressDao().upsert(ExamTriggerLogic.clearExamAvailable(current))
    }

    suspend fun shouldShowExamPrompt(lessonId: Long, stopPressedAt: Long): Boolean {
        val progress = getProgress(lessonId) ?: return false
        return ExamTriggerLogic.evaluateAfterStop(progress, stopPressedAt).shouldShowPrompt
    }

    fun observeExamHistory(lessonId: Long) =
        db.examAttemptDao().observeByLesson(userId, lessonId)
}

class ExamRepository(private val db: AppDatabase) {

    private val userId get() = DatabaseSeeder.DEFAULT_USER_ID

    suspend fun buildMcQuestions(lessonId: Long): List<McQuestion> {
        val phrases = db.phraseDao().getByLesson(lessonId)
        val allPhrases = db.phraseDao().getByLessons(
            db.lessonDao().getAll().map { it.id }
        )
        return phrases.map { target ->
            val distractors = allPhrases
                .filter { it.id != target.id }
                .shuffled()
                .take(3)
                .map { it.russianContext }
            val options = (distractors + target.russianContext).shuffled()
            McQuestion(
                phraseId = target.id,
                questionText = "Когда используешь: «${target.koreanText}»?",
                options = options,
                correctIndex = options.indexOf(target.russianContext)
            )
        }
    }

    suspend fun saveAttempt(
        lessonId: Long,
        answers: List<ExamAnswerRecord>
    ): ExamAttemptEntity {
        val correct = answers.count { it.correct }
        val score = ExamScoring.scoreFromMcAnswers(correct, answers.size)
        val attemptNumber = db.examAttemptDao().countAttempts(userId, lessonId) + 1
        val entity = ExamAttemptEntity(
            userId = userId,
            lessonId = lessonId,
            attemptNumber = attemptNumber,
            score = score,
            date = System.currentTimeMillis(),
            answersJson = ExamScoring.answersToJson(answers)
        )
        db.examAttemptDao().insert(entity)
        return entity
    }

    fun observeAllAttempts(): Flow<List<ExamAttemptEntity>> =
        db.examAttemptDao().observeAll(userId)
}
