package com.example.data.memory.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.memory.entities.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {

    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp ASC")
    fun getMessagesForUser(userId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE userId = :userId ORDER BY timestamp ASC")
    suspend fun getMessagesForUserDirect(userId: String): List<ChatMessageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_messages WHERE userId = :userId")
    suspend fun deleteMessagesForUser(userId: String)

    @Query("DELETE FROM chat_messages WHERE id = :id")
    suspend fun deleteMessageById(id: String)
}
