package com.koreanimmersion.data.repository

import com.koreanimmersion.core.database.entity.PhraseEntity
import com.koreanimmersion.core.database.entity.TopicEntity
import com.koreanimmersion.data.local.AppDatabase
import kotlinx.coroutines.flow.Flow

class CourseContentRepository(private val db: AppDatabase) {

    fun observeTopics(): Flow<List<TopicEntity>> = db.courseTopicDao().observeAll()

    suspend fun getTopic(topicId: Long): TopicEntity? = db.courseTopicDao().getById(topicId)

    fun observePhrases(topicId: Long): Flow<List<PhraseEntity>> =
        db.coursePhraseDao().observeByTopicId(topicId)

    suspend fun getPhrasesByTopicId(topicId: Long): List<PhraseEntity> =
        db.coursePhraseDao().getByTopicId(topicId)
}
