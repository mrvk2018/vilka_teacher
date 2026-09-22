package com.koreanimmersion.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.koreanimmersion.core.database.entity.TopicEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentTopicDao {
    @Query("SELECT * FROM course_topics ORDER BY order_index ASC")
    fun observeAll(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM course_topics ORDER BY order_index ASC")
    suspend fun getAll(): List<TopicEntity>

    @Query("SELECT * FROM course_topics WHERE id = :topicId LIMIT 1")
    suspend fun getById(topicId: Long): TopicEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(topics: List<TopicEntity>)
}
