package com.example.data.memory.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.memory.dao.ChatMessageDao
import com.example.data.memory.dao.UserMemoryFactDao
import com.example.data.memory.dao.UserProfileDao
import com.example.data.memory.entities.ChatMessageEntity
import com.example.data.memory.entities.UserMemoryFactEntity
import com.example.data.memory.entities.UserProfileEntity

@Database(
    entities = [
        UserProfileEntity::class,
        UserMemoryFactEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JarvisMemoryDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun userMemoryFactDao(): UserMemoryFactDao
    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisMemoryDatabase? = null

        fun getDatabase(context: Context): JarvisMemoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisMemoryDatabase::class.java,
                    "jarvis_memory.db"
                )
                    .fallbackToDestructiveMigration(true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
