package com.example.data.memory.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_memory_facts")
data class UserMemoryFactEntity(
    @PrimaryKey
    val id: String = java.util.UUID.randomUUID().toString(),
    val userId: String,
    val category: String, // "NAME", "PREFERENCE", "FACT", "TASK", "NOTE"
    val key: String,
    val value: String,
    val timestamp: Long = System.currentTimeMillis()
)
