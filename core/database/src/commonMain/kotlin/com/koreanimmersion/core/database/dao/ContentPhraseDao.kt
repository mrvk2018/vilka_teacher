package com.koreanimmersion.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.koreanimmersion.core.database.entity.PhraseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentPhraseDao {
    @Query(
        """
        SELECT * FROM course_phrases
        WHERE topic_id = :topicId
        ORDER BY order_index ASC
        """
    )
    fun observeByTopicId(topicId: Long): Flow<List<PhraseEntity>>

    @Query(
        """
        SELECT * FROM course_phrases
        WHERE topic_id = :topicId
        ORDER BY order_index ASC
        """
    )
    suspend fun getByTopicId(topicId: Long): List<PhraseEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(phrases: List<PhraseEntity>)
}
