package com.koreanimmersion.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.koreanimmersion.core.database.entity.UserProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE userId = :userId LIMIT 1")
    suspend fun get(userId: String): UserProgressEntity?

    @Query("SELECT * FROM user_progress WHERE userId = :userId LIMIT 1")
    fun observe(userId: String): Flow<UserProgressEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserProgressEntity)

    @Query("DELETE FROM user_progress WHERE userId = :userId")
    suspend fun delete(userId: String)
}
