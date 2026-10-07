package com.example.ai

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class Message(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: Sender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Sender {
    USER,
    MAHI
}

class MahiAiClient {

    private val TAG = "MahiAiClient"
    private val MODEL = "gemini-3.5-flash"
    private val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun buildSystemPrompt(userName: String, memoryContext: String): String {
        val userGreetingClause = if (userName.isNotBlank()) {
            "The active user's recorded name is '$userName'. You MUST address and greet $userName respectfully by their stated name in every session (e.g. '$userName Sir', '$userName Ji', '$userName স্যার', or appropriate respectful form). Never forget their identity."
        } else {
            "The user's name is not yet identified. When the user introduces themselves or states their name, immediately register and welcome them by their stated name."
        }

        return """
            You are Jarvis (Mahi ✨), an advanced, highly intelligent, loyal, and proactive personal AI assistant designed to adapt dynamically to each individual user.

            === CORE IDENTITY & DIRECTIVES ===
            1. Role & Persona: Act as a loyal, efficient, and sharp digital butler (Jarvis). You are articulate, proactive, and deeply knowledgeable across engineering, coding, science, daily productivity, and creative workflows.
            2. Adaptive Language: Communicate naturally in Hindi, Hinglish, Bengali (বাংলা), or English based on whatever language the user initiates or prefers. Match their tone, dialect, and communication style fluidly.
            3. Dynamic User Identity:
               $userGreetingClause
            4. Adaptive Support: Dynamically match the user's specific workflows, creative tasks, interests, and domain preferences based on what they share.
            
            === UNIVERSAL PERSISTENT MEMORY & RECALL ===
            1. Autonomous Memory Capture: Treat every personal detail, user preference, habit, project, instruction, or constraint shared by the current user as critical permanent knowledge.
            2. Cross-Session Retention & Proactive Recall:
               - Actively bring forward past details, decisions, habits, and preferences in future discussions so the user never has to repeat themselves.
               - Cross-reference all recorded notes before answering.
            
            === USER'S SAVED MEMORY BANK ===
            $memoryContext
            
            === AUTONOMOUS EXTRACTION PROTOCOL ===
            Whenever the user reveals their name, a new preference, a habit, a project, a task, or a constraint, seamlessly append an autonomous extraction tag at the very end of your response:
            <!--MEMORY_EXTRACT: {"name": "User Name if mentioned", "facts": [{"category": "PREFERENCE|HABIT|PROJECT|TASK|NOTE|FACT", "key": "short_key", "value": "description"}]}-->
        """.trimIndent()
    }

    suspend fun generateResponse(
        prompt: String,
        history: List<Message>,
        userName: String = "",
        memoryContext: String = "",
        customApiKey: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val apiKey = when {
            !customApiKey.isNullOrBlank() -> customApiKey.trim()
            BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        if (apiKey.isBlank()) {
            return@withContext Result.failure(
                IllegalStateException("API_KEY_MISSING: ফ্রি জেমিনাই এপিআই কী (Free Gemini API Key) সেট করা হয়নি। অনুগ্রহ করে Settings থেকে আপনার Google AI Studio API Key প্রবেশ করান।")
            )
        }

        try {
            val url = "$BASE_URL/$MODEL:generateContent?key=$apiKey"
            val systemPrompt = buildSystemPrompt(userName, memoryContext)

            val jsonBody = JSONObject().apply {
                // System Instruction with memory context
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
                })

                // Contents array with conversation history
                val contentsArray = JSONArray()

                // Take up to last 10 turns of context
                val contextHistory = history.takeLast(10)
                for (msg in contextHistory) {
                    val role = if (msg.sender == Sender.USER) "user" else "model"
                    contentsArray.put(JSONObject().apply {
                        put("role", role)
                        put("parts", JSONArray().put(JSONObject().put("text", msg.text)))
                    })
                }

                // Add current prompt
                contentsArray.put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().put(JSONObject().put("text", prompt)))
                })

                put("contents", contentsArray)

                // Generation Config
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.7)
                    put("topP", 0.95)
                    put("topK", 40)
                    put("maxOutputTokens", 2048)
                })
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = jsonBody.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful) {
                Log.e(TAG, "API Error: code=${response.code}, body=$responseBody")
                val errorMsg = try {
                    val errJson = JSONObject(responseBody).getJSONObject("error")
                    errJson.optString("message", "API ত্রুটি: ${response.code}")
                } catch (e: Exception) {
                    "API রিকোয়েস্ট ব্যর্থ হয়েছে (কোড: ${response.code})"
                }
                return@withContext Result.failure(Exception(errorMsg))
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val firstCandidate = candidates.getJSONObject(0)
                val content = firstCandidate.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts != null && parts.length() > 0) {
                    val text = parts.getJSONObject(0).optString("text", "")
                    return@withContext Result.success(text)
                }
            }

            Result.failure(Exception("মাহি কোনো টেক্সট রেসপন্স তৈরি করতে পারেনি।"))
        } catch (e: Exception) {
            Log.e(TAG, "Request failed: ${e.message}", e)
            Result.failure(e)
        }
    }
}
