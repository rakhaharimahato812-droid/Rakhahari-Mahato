package com.example.data.memory.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String,
    val sender: String, // "USER" or "MAHI"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)
