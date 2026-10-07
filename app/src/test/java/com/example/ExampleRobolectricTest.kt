package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.ai.Sender
import com.example.data.memory.extractor.MemoryExtractor
import com.example.data.memory.repository.JarvisMemoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Mahi AI", appName)
    }

    @Test
    fun `extract user name and notes from bengali and english messages`() {
        val bengaliExtraction = MemoryExtractor.extractFromUserMessage("আমার নাম রাখাহরি", "user_1")
        assertEquals("রাখাহরি", bengaliExtraction.extractedName)

        val englishExtraction = MemoryExtractor.extractFromUserMessage("My name is Rakhahari", "user_1")
        assertEquals("Rakhahari", englishExtraction.extractedName)

        val hindiExtraction = MemoryExtractor.extractFromUserMessage("मेरा नाम रोहन है", "user_1")
        assertEquals("रोहन", hindiExtraction.extractedName)

        val hinglishExtraction = MemoryExtractor.extractFromUserMessage("mera naam Alex hai", "user_1")
        assertEquals("Alex", hinglishExtraction.extractedName)

        val preferenceExtraction = MemoryExtractor.extractFromUserMessage("আমার প্রিয় প্রোগ্রামিং ভাষা কোটলিন", "user_1")
        assertTrue(preferenceExtraction.facts.any { it.category == "PREFERENCE" })

        val habitExtraction = MemoryExtractor.extractFromUserMessage("meri aadat hai subah jaldi uthna", "user_1")
        assertTrue(habitExtraction.facts.any { it.category == "HABIT" })

        val projectExtraction = MemoryExtractor.extractFromUserMessage("mera project Android automation par hai", "user_1")
        assertTrue(projectExtraction.facts.any { it.category == "PROJECT" })
    }

    @Test
    fun `persistent memory repository stores and reloads multi-user profile and messages`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repository = JarvisMemoryRepository(context)

        // 1. Initial active profile
        val activeProfile = repository.getOrCreateActiveProfile()
        assertNotNull(activeProfile)

        // 2. Name update and fact extraction
        repository.updateProfileName(activeProfile.id, "রাখাহরি")
        val updatedName = repository.getOrCreateActiveProfile().name
        assertEquals("রাখাহরি", updatedName)

        // 3. Save message
        repository.saveMessage(activeProfile.id, Sender.USER, "হ্যালো মাহি")
        repository.saveMessage(activeProfile.id, Sender.MAHI, "নমস্কার রাখাহরি! কীভাবে সাহায্য করতে পারি?")

        val messages = repository.getMessagesForUserDirect(activeProfile.id)
        assertEquals(2, messages.size)
        assertEquals("হ্যালো মাহি", messages[0].text)

        // 4. Memory context formatting
        val memoryContext = repository.getFormattedMemoryContext(activeProfile.id)
        assertTrue(memoryContext.contains("রাখাহরি"))

        // 5. Multi-user profile creation
        val newProfile = repository.createNewProfile("Alice")
        assertEquals("Alice", newProfile.name)
        val allProfiles = repository.allProfiles.first()
        assertTrue(allProfiles.size >= 2)
    }
}
