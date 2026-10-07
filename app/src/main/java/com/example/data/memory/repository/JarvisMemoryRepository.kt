package com.example.data.memory.repository

import android.content.Context
import com.example.ai.Message
import com.example.ai.Sender
import com.example.data.memory.db.JarvisMemoryDatabase
import com.example.data.memory.entities.ChatMessageEntity
import com.example.data.memory.entities.UserMemoryFactEntity
import com.example.data.memory.entities.UserProfileEntity
import com.example.data.memory.extractor.MemoryExtractor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class JarvisMemoryRepository(context: Context) {

    private val db = JarvisMemoryDatabase.getDatabase(context)
    private val profileDao = db.userProfileDao()
    private val factDao = db.userMemoryFactDao()
    private val messageDao = db.chatMessageDao()

    val allProfiles: Flow<List<UserProfileEntity>> = profileDao.getAllProfiles()
    val activeProfile: Flow<UserProfileEntity?> = profileDao.getActiveProfile()

    suspend fun getOrCreateActiveProfile(): UserProfileEntity {
        val existing = profileDao.getActiveProfileDirect()
        if (existing != null) return existing

        val all = profileDao.getAllProfiles().firstOrNull() ?: emptyList()
        if (all.isNotEmpty()) {
            val first = all.first()
            profileDao.activateProfile(first.id)
            return first.copy(isActive = true)
        }

        val defaultProfile = UserProfileEntity(
            id = "user_default",
            name = "",
            title = "Sir",
            languagePreference = "bn-BD",
            createdAt = System.currentTimeMillis(),
            lastActiveAt = System.currentTimeMillis(),
            isActive = true
        )
        profileDao.insertOrUpdateProfile(defaultProfile)
        return defaultProfile
    }

    suspend fun createNewProfile(name: String): UserProfileEntity {
        val newId = "user_" + UUID.randomUUID().toString().take(8)
        val newProfile = UserProfileEntity(
            id = newId,
            name = name.trim(),
            title = "User",
            createdAt = System.currentTimeMillis(),
            lastActiveAt = System.currentTimeMillis(),
            isActive = true
        )
        profileDao.deactivateAllProfiles()
        profileDao.insertOrUpdateProfile(newProfile)

        if (name.isNotBlank()) {
            factDao.insertFact(
                UserMemoryFactEntity(
                    userId = newId,
                    category = "NAME",
                    key = "user_name",
                    value = name.trim()
                )
            )
        }
        return newProfile
    }

    suspend fun switchProfile(profileId: String) {
        profileDao.switchActiveProfile(profileId)
    }

    suspend fun updateProfileName(userId: String, name: String) {
        val trimmed = name.trim()
        if (trimmed.isNotBlank()) {
            profileDao.updateProfileName(userId, trimmed)
            factDao.insertFact(
                UserMemoryFactEntity(
                    userId = userId,
                    category = "NAME",
                    key = "user_name",
                    value = trimmed
                )
            )
        }
    }

    suspend fun deleteProfile(profileId: String) {
        factDao.clearFactsForUser(profileId)
        messageDao.deleteMessagesForUser(profileId)
        profileDao.deleteProfile(profileId)

        // Make another profile active if exists
        val remaining = profileDao.getAllProfiles().firstOrNull() ?: emptyList()
        if (remaining.isNotEmpty()) {
            profileDao.activateProfile(remaining.first().id)
        } else {
            getOrCreateActiveProfile()
        }
    }

    fun getMessagesForUser(userId: String): Flow<List<ChatMessageEntity>> {
        return messageDao.getMessagesForUser(userId)
    }

    suspend fun getMessagesForUserDirect(userId: String): List<ChatMessageEntity> {
        return messageDao.getMessagesForUserDirect(userId)
    }

    suspend fun saveMessage(userId: String, sender: Sender, text: String): ChatMessageEntity {
        val entity = ChatMessageEntity(
            userId = userId,
            sender = sender.name,
            text = text,
            timestamp = System.currentTimeMillis()
        )
        messageDao.insertMessage(entity)
        return entity
    }

    suspend fun clearMessagesForUser(userId: String) {
        messageDao.deleteMessagesForUser(userId)
    }

    fun getFactsForUser(userId: String): Flow<List<UserMemoryFactEntity>> {
        return factDao.getFactsForUser(userId)
    }

    suspend fun getFactsForUserDirect(userId: String): List<UserMemoryFactEntity> {
        return factDao.getFactsForUserDirect(userId)
    }

    suspend fun addManualFact(userId: String, category: String, key: String, value: String) {
        factDao.insertFact(
            UserMemoryFactEntity(
                userId = userId,
                category = category,
                key = key,
                value = value
            )
        )
    }

    suspend fun deleteFact(factId: String) {
        factDao.deleteFact(factId)
    }

    /**
     * Processes both user input and AI reply to extract & persist name, facts, tasks, and preferences.
     * Returns the detected name if one was newly extracted.
     */
    suspend fun processAndExtractMemory(userId: String, userText: String, aiReply: String): String? {
        val userExtracted = MemoryExtractor.extractFromUserMessage(userText, userId)
        val aiExtracted = MemoryExtractor.extractFromAiResponse(aiReply, userId)

        var finalExtractedName: String? = null

        // Process extracted name
        val nameCandidate = userExtracted.extractedName ?: aiExtracted.extractedName
        if (!nameCandidate.isNullOrBlank()) {
            updateProfileName(userId, nameCandidate)
            finalExtractedName = nameCandidate
        }

        // Combine and persist facts
        val allFacts = (userExtracted.facts + aiExtracted.facts).filter { it.category != "NAME" || finalExtractedName == null }
        if (allFacts.isNotEmpty()) {
            factDao.insertFacts(allFacts)
        }

        return finalExtractedName
    }

    /**
     * Builds structured recall text for the active user to inject into Gemini prompt context.
     */
    suspend fun getFormattedMemoryContext(userId: String): String {
        val profile = profileDao.getProfileById(userId)
        val facts = factDao.getFactsForUserDirect(userId)

        val sb = StringBuilder()
        val userName = profile?.name.orEmpty()

        if (userName.isNotBlank()) {
            sb.appendLine("- Identified User Name: $userName")
        } else {
            sb.appendLine("- Identified User Name: Not yet provided")
        }

        if (facts.isNotEmpty()) {
            sb.appendLine("- Known Facts, Preferences & Notes:")
            for (f in facts.distinctBy { "${it.category}:${it.value}" }) {
                sb.appendLine("  * [${f.category}] ${f.value}")
            }
        } else {
            sb.appendLine("- Known Facts: No permanent notes recorded yet.")
        }

        return sb.toString().trim()
    }
}
