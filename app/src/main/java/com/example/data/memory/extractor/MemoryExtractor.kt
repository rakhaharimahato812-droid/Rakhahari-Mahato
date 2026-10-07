package com.example.data.memory.extractor

import com.example.data.memory.entities.UserMemoryFactEntity
import org.json.JSONObject
import java.util.regex.Pattern

data class ExtractedMemory(
    val extractedName: String? = null,
    val facts: List<UserMemoryFactEntity> = emptyList()
)

object MemoryExtractor {

    private val BENGALI_NAME_PATTERNS = listOf(
        Pattern.compile("আমার\\s+নাম(?:\\s+হলো|\\s+হচ্ছে|\\s+হল)?\\s+([\\p{L}\\p{M}]+(?:\\s+[\\p{L}\\p{M}]+)?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("আমাকে\\s+([\\p{L}\\p{M}]+(?:\\s+[\\p{L}\\p{M}]+)?)\\s+(?:ডাকতে\\s+পারো|বলে\\s+ডেকো|বলো)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("আমি\\s+([\\p{L}\\p{M}]+(?:\\s+[\\p{L}\\p{M}]+)?)\\s*(?:।|,|\\s*$)", Pattern.CASE_INSENSITIVE)
    )

    private val HINDI_NAME_PATTERNS = listOf(
        Pattern.compile("मेरा\\s+नाम\\s+([\\p{L}\\p{M}]+)(?:\\s+है)?", Pattern.CASE_INSENSITIVE),
        Pattern.compile("मुझे\\s+([\\p{L}\\p{M}]+)\\s+(?:बुलाओ|कहो|बुला सकते हो)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("मैं\\s+([\\p{L}\\p{M}]+)\\s*(?:हूँ|हु)?\\s*(?:।|,|\\s*$)", Pattern.CASE_INSENSITIVE)
    )

    private val HINGLISH_NAME_PATTERNS = listOf(
        Pattern.compile("(?:mera\\s+naam|mera\\s+name)\\s+([a-zA-Z]+)(?:\\s+hai)?", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?:mujhe)\\s+([a-zA-Z]+)\\s+(?:bulao|kaho|bolo)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?:main|mai)\\s+([a-zA-Z]+)\\s*(?:hoon|hu)?\\s*(?:$|,)", Pattern.CASE_INSENSITIVE)
    )

    private val ENGLISH_NAME_PATTERNS = listOf(
        Pattern.compile("(?:my\\s+name\\s+is|i\\s+am|i'm|call\\s+me)\\s+([a-zA-Z]+(?:\\s+[a-zA-Z]+)?)", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(?:this\\s+is)\\s+([a-zA-Z]+(?:\\s+[a-zA-Z]+)?)\\s*(?:speaking|here)?", Pattern.CASE_INSENSITIVE)
    )

    fun extractFromUserMessage(text: String, userId: String): ExtractedMemory {
        var foundName: String? = null
        val facts = mutableListOf<UserMemoryFactEntity>()
        val trimmed = text.trim()

        // 1. Dynamic Name extraction (Bengali, Hindi, Hinglish, English)
        val allNamePatterns = BENGALI_NAME_PATTERNS + HINDI_NAME_PATTERNS + HINGLISH_NAME_PATTERNS + ENGLISH_NAME_PATTERNS
        for (pattern in allNamePatterns) {
            val matcher = pattern.matcher(trimmed)
            if (matcher.find()) {
                val candidate = matcher.group(1)?.trim()
                if (!candidate.isNullOrBlank() && isValidName(candidate)) {
                    foundName = candidate
                    break
                }
            }
        }

        if (foundName != null) {
            facts.add(
                UserMemoryFactEntity(
                    userId = userId,
                    category = "NAME",
                    key = "user_name",
                    value = foundName
                )
            )
        }

        // 2. Autonomous Memory Capture: Habits, Preferences, Projects, Tasks & Notes (Bengali)
        if (text.contains("মনে রাখো") || text.contains("মনে রেখো") || text.contains("নোট করো")) {
            val clean = text.replace(Regex("^(মনে রাখো|মনে রেখো|নোট করো|প্লিজ)\\s*[:,-]?\\s*"), "").trim()
            if (clean.length > 3) {
                facts.add(
                    UserMemoryFactEntity(
                        userId = userId,
                        category = "NOTE",
                        key = "saved_note",
                        value = clean
                    )
                )
            }
        }

        if (text.contains("আমার প্রিয়") || text.contains("আমার পছন্দ") || text.contains("পছন্দ করি")) {
            facts.add(
                UserMemoryFactEntity(
                    userId = userId,
                    category = "PREFERENCE",
                    key = "user_preference",
                    value = text.trim()
                )
            )
        }

        if (text.contains("আমার অভ্যাস") || text.contains("আমার রুটিন")) {
            facts.add(
                UserMemoryFactEntity(
                    userId = userId,
                    category = "HABIT",
                    key = "user_habit",
                    value = text.trim()
                )
            )
        }

        if (text.contains("আমার প্রজেক্ট") || text.contains("আমার প্রকল্প")) {
            facts.add(
                UserMemoryFactEntity(
                    userId = userId,
                    category = "PROJECT",
                    key = "user_project",
                    value = text.trim()
                )
            )
        }

        if (text.contains("আমার কাজ") || text.contains("করতে হবে") || text.contains("টাস্ক") || text.contains("মনে করিয়ে দাও")) {
            facts.add(
                UserMemoryFactEntity(
                    userId = userId,
                    category = "TASK",
                    key = "pending_task",
                    value = text.trim()
                )
            )
        }

        // 3. Autonomous Memory Capture: Hindi & Hinglish
        val lower = text.lowercase()
        if (text.contains("याद रखो") || text.contains("नोट कर लो") || lower.contains("yaad rakhna") || lower.contains("note kar lo")) {
            val clean = text.replace(Regex("(?i)^(याद रखो|नोट कर लो|yaad rakhna|note kar lo)\\s*[:,-]?\\s*"), "").trim()
            if (clean.length > 3) {
                facts.add(
                    UserMemoryFactEntity(
                        userId = userId,
                        category = "NOTE",
                        key = "saved_note",
                        value = clean
                    )
                )
            }
        }

        if (text.contains("मेरी पसंद") || text.contains("मुझे पसंद है") || lower.contains("meri pasand") || lower.contains("mujhe pasand hai")) {
            facts.add(
                UserMemoryFactEntity(
                    userId = userId,
                    category = "PREFERENCE",
                    key = "user_preference",
                    value = text.trim()
                )
            )
        }

        if (text.contains("मेरी आदत") || text.contains("मेरी दिनचर्या") || lower.contains("meri aadat") || lower.contains("my habit")) {
            facts.add(
                UserMemoryFactEntity(
                    userId = userId,
                    category = "HABIT",
                    key = "user_habit",
                    value = text.trim()
                )
            )
        }

        if (text.contains("मेरा प्रोजेक्ट") || lower.contains("mera project") || lower.contains("my project")) {
            facts.add(
                UserMemoryFactEntity(
                    userId = userId,
                    category = "PROJECT",
                    key = "user_project",
                    value = text.trim()
                )
            )
        }

        if (text.contains("मेरा काम") || text.contains("करना है") || text.contains("टास्क") || lower.contains("mera task") || lower.contains("mera kaam")) {
            facts.add(
                UserMemoryFactEntity(
                    userId = userId,
                    category = "TASK",
                    key = "pending_task",
                    value = text.trim()
                )
            )
        }

        // 4. Autonomous Memory Capture: English
        if (lower.startsWith("remember that") || lower.startsWith("remember this") || lower.startsWith("note that")) {
            val clean = text.replace(Regex("^(?i)(remember that|remember this|note that|note:)\\s*"), "").trim()
            if (clean.length > 3) {
                facts.add(
                    UserMemoryFactEntity(
                        userId = userId,
                        category = "NOTE",
                        key = "saved_note",
                        value = clean
                    )
                )
            }
        }

        if (lower.contains("my favorite") || lower.contains("i like") || lower.contains("i love") || lower.contains("my preference")) {
            facts.add(
                UserMemoryFactEntity(
                    userId = userId,
                    category = "PREFERENCE",
                    key = "user_preference",
                    value = text.trim()
                )
            )
        }

        if (lower.contains("my habit is") || lower.contains("i usually") || lower.contains("my daily routine")) {
            facts.add(
                UserMemoryFactEntity(
                    userId = userId,
                    category = "HABIT",
                    key = "user_habit",
                    value = text.trim()
                )
            )
        }

        if (lower.contains("my project is") || lower.contains("i am working on") || lower.contains("current project")) {
            facts.add(
                UserMemoryFactEntity(
                    userId = userId,
                    category = "PROJECT",
                    key = "user_project",
                    value = text.trim()
                )
            )
        }

        if (lower.contains("remind me to") || lower.contains("my task is") || lower.contains("todo:") || lower.contains("i need to")) {
            facts.add(
                UserMemoryFactEntity(
                    userId = userId,
                    category = "TASK",
                    key = "pending_task",
                    value = text.trim()
                )
            )
        }

        return ExtractedMemory(extractedName = foundName, facts = facts)
    }

    /**
     * Parses optional structured memory extraction block embedded in AI response
     * e.g. <!--MEMORY_EXTRACT: {"name": "...", "facts": [...]}-->
     */
    fun extractFromAiResponse(aiText: String, userId: String): ExtractedMemory {
        val pattern = Pattern.compile("<!--MEMORY_EXTRACT:\\s*(\\{.*?\\})\\s*-->", Pattern.DOTALL)
        val matcher = pattern.matcher(aiText)
        if (matcher.find()) {
            val jsonStr = matcher.group(1) ?: return ExtractedMemory()
            try {
                val json = JSONObject(jsonStr)
                val name = json.optString("name").takeIf { !it.isNullOrBlank() }
                val factsList = mutableListOf<UserMemoryFactEntity>()
                val factsArray = json.optJSONArray("facts")
                if (factsArray != null) {
                    for (i in 0 until factsArray.length()) {
                        val item = factsArray.getJSONObject(i)
                        val cat = item.optString("category", "FACT")
                        val k = item.optString("key", "fact")
                        val v = item.optString("value", "")
                        if (v.isNotBlank()) {
                            factsList.add(
                                UserMemoryFactEntity(
                                    userId = userId,
                                    category = cat,
                                    key = k,
                                    value = v
                                )
                            )
                        }
                    }
                }
                return ExtractedMemory(extractedName = name, facts = factsList)
            } catch (e: Exception) {
                // Ignore parse errors from AI output
            }
        }
        return ExtractedMemory()
    }

    private fun isValidName(candidate: String): Boolean {
        val stopWords = setOf(
            "বলুন", "আছি", "ভালো", "কেমন", "এখানে", "কে", "কি", "কিছু", "না", "হ্যাঁ",
            "नमस्ते", "अच्छा", "हाँ", "नहीं", "क्या", "कौन", "यहाँ", "वहाँ", "ठीक",
            "hello", "hi", "hey", "fine", "good", "okay", "ready", "listening", "speaking", "here", "there",
            "kya", "kaise", "thik", "haan", "nahi"
        )
        return candidate.length in 2..30 && !stopWords.contains(candidate.lowercase())
    }
}
