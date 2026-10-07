package com.example.data.memory.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.memory.entities.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profiles ORDER BY lastActiveAt DESC")
    fun getAllProfiles(): Flow<List<UserProfileEntity>>

    @Query("SELECT * FROM user_profiles WHERE isActive = 1 LIMIT 1")
    fun getActiveProfile(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profiles WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveProfileDirect(): UserProfileEntity?

    @Query("SELECT * FROM user_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: String): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    @Query("UPDATE user_profiles SET isActive = 0")
    suspend fun deactivateAllProfiles()

    @Query("UPDATE user_profiles SET isActive = 1, lastActiveAt = :timestamp WHERE id = :id")
    suspend fun activateProfile(id: String, timestamp: Long = System.currentTimeMillis())

    @Transaction
    suspend fun switchActiveProfile(id: String) {
        deactivateAllProfiles()
        activateProfile(id)
    }

    @Query("UPDATE user_profiles SET name = :name, lastActiveAt = :timestamp WHERE id = :id")
    suspend fun updateProfileName(id: String, name: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM user_profiles WHERE id = :id")
    suspend fun deleteProfile(id: String)
}
