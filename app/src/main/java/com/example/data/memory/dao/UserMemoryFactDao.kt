package com.example.data.memory.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.memory.entities.UserMemoryFactEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserMemoryFactDao {

    @Query("SELECT * FROM user_memory_facts WHERE userId = :userId ORDER BY timestamp DESC")
    fun getFactsForUser(userId: String): Flow<List<UserMemoryFactEntity>>

    @Query("SELECT * FROM user_memory_facts WHERE userId = :userId ORDER BY timestamp DESC")
    suspend fun getFactsForUserDirect(userId: String): List<UserMemoryFactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFact(fact: UserMemoryFactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacts(facts: List<UserMemoryFactEntity>)

    @Query("DELETE FROM user_memory_facts WHERE id = :id")
    suspend fun deleteFact(id: String)

    @Query("DELETE FROM user_memory_facts WHERE userId = :userId AND `key` = :key")
    suspend fun deleteFactByKey(userId: String, key: String)

    @Query("DELETE FROM user_memory_facts WHERE userId = :userId")
    suspend fun clearFactsForUser(userId: String)
}
