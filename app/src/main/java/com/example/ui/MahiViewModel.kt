package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.MahiAiClient
import com.example.ai.Message
import com.example.ai.Sender
import com.example.data.memory.entities.UserMemoryFactEntity
import com.example.data.memory.entities.UserProfileEntity
import com.example.data.memory.repository.JarvisMemoryRepository
import com.example.voice.MahiVoiceManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

enum class QuickCommand(val title: String, val prompt: String, val icon: String) {
    STATUS("সিস্টেম স্ট্যাটাস", "মাহি, বর্তমান সিস্টেম ডায়াগনস্টিক রিপোর্ট দিন এবং আপনার এআই কোর ও মেমোরি স্ট্যাটাস জানান।", "🚀"),
    WHO_AM_I("আমার তথ্য ও মেমোরি", "মাহি, আমার সম্পর্কে আপনার কাছে কী কী তথ্য, নাম ও নোট সংরক্ষিত আছে তা আমাকে বলুন।", "🧠"),
    SCIENCE("বিজ্ঞান ও মহাবিশ্ব", "মহাবিশ্বের ব্ল্যাকহোল এবং কোয়ান্টাম এনট্যাঙ্গেলমেন্ট সম্পর্কে একটি চমকপ্রদ তথ্য সংক্ষেপে ব্যাখ্যা করুন।", "🌌"),
    CODING("কোডিং সমাধান", "Kotlin অথবা Python-এ একটি কার্যকর ও চমৎকার কোডিং টিপস বা অ্যালগরিদম উদাহরণ দিন।", "💻"),
    WISDOM("আজকের অনুপ্রেরণা", "আজকের দিনটি সুন্দর করার জন্য বাংলায় একটি গভীর অনুপ্রেরণামূলক বার্তা বা দার্শনিক চিন্তা বলুন।", "🌸"),
    JOKE("আনন্দ ও গল্প", "বাংলায় একটি মজার হাস্যকৌতুক বা ছোট চতুর গল্প বলুন।", "😄")
}

class MahiViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("mahi_prefs", Context.MODE_PRIVATE)

    val voiceManager = MahiVoiceManager(application)
    private val aiClient = MahiAiClient()
    val memoryRepository = JarvisMemoryRepository(application)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _activeProfile = MutableStateFlow<UserProfileEntity?>(null)
    val activeProfile: StateFlow<UserProfileEntity?> = _activeProfile.asStateFlow()

    private val _allProfiles = MutableStateFlow<List<UserProfileEntity>>(emptyList())
    val allProfiles: StateFlow<List<UserProfileEntity>> = _allProfiles.asStateFlow()

    private val _memoryFacts = MutableStateFlow<List<UserMemoryFactEntity>>(emptyList())
    val memoryFacts: StateFlow<List<UserMemoryFactEntity>> = _memoryFacts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _customApiKey = MutableStateFlow(prefs.getString("custom_api_key", "") ?: "")
    val customApiKey: StateFlow<String> = _customApiKey.asStateFlow()

    private val _autoSpeak = MutableStateFlow(prefs.getBoolean("auto_speak", true))
    val autoSpeak: StateFlow<Boolean> = _autoSpeak.asStateFlow()

    val isListening: StateFlow<Boolean> = voiceManager.isListening
    val isSpeaking: StateFlow<Boolean> = voiceManager.isSpeaking
    val isVoiceReady: StateFlow<Boolean> = voiceManager.isTtsReady
    val audioAmplitude: StateFlow<Float> = voiceManager.audioAmplitude
    val statusMessage: StateFlow<String> = voiceManager.statusMessage
    val activeLanguage: StateFlow<String> = voiceManager.activeLanguage
    val voicePitch: StateFlow<Float> = voiceManager.pitch
    val speechRate: StateFlow<Float> = voiceManager.speechRate

    private var messageCollectorJob: Job? = null
    private var factCollectorJob: Job? = null

    init {
        // Restore voice preferences
        val savedPitch = prefs.getFloat("voice_pitch", 1.18f)
        val savedRate = prefs.getFloat("voice_rate", 0.95f)
        val savedLang = prefs.getString("voice_lang", "bn-BD") ?: "bn-BD"
        voiceManager.setPitch(savedPitch)
        voiceManager.setSpeechRate(savedRate)
        voiceManager.setLanguage(savedLang)

        // Observe profiles and initialize active user memory
        viewModelScope.launch {
            memoryRepository.allProfiles.collectLatest { profiles ->
                _allProfiles.value = profiles
            }
        }

        viewModelScope.launch {
            val initialActive = memoryRepository.getOrCreateActiveProfile()
            _activeProfile.value = initialActive
            loadUserData(initialActive)
        }

        // Bind Speech Recognizer callback
        voiceManager.onSpeechRecognized = { recognizedText ->
            if (recognizedText.isNotBlank()) {
                sendMessage(recognizedText)
            }
        }
    }

    private fun loadUserData(profile: UserProfileEntity) {
        messageCollectorJob?.cancel()
        factCollectorJob?.cancel()

        // Collect persistent messages from Room
        messageCollectorJob = viewModelScope.launch {
            memoryRepository.getMessagesForUser(profile.id).collectLatest { entities ->
                if (entities.isEmpty()) {
                    // Seed initial personalized greeting if no history exists for this profile
                    val userName = profile.name.trim()
                    val isHi = activeLanguage.value.startsWith("hi")
                    val isEn = activeLanguage.value.startsWith("en")
                    val greeting = when {
                        isHi && userName.isNotBlank() -> "नमस्ते $userName जी! मैं जार्विस (Mahi) हूँ— आपका पर्सनल एआई असिस्टेंट। आपकी प्रोफाइल और मेमोरी सक्रिय है। आज मैं आपकी क्या सेवा कर सकता हूँ?"
                        isHi -> "नमस्ते! मैं जार्विस (Mahi) हूँ— आपका पर्सनल एআই असिस्टेंट। मेरे पास परमानेंट मेमोरी सिस्टम है। आप अपना नाम, काम, आदतें या प्रोजेक्ट्स बताएं, मैं हमेशा याद रखूँगा। कहिए, क्या मदद करूँ?"
                        isEn && userName.isNotBlank() -> "Greetings $userName! I am Jarvis (Mahi ✨), your loyal personal AI butler. Your profile and memory banks are online. How may I assist you today?"
                        isEn -> "Greetings! I am Jarvis (Mahi ✨), your personal AI assistant. Autonomous persistent memory is active. Feel free to state your name, preferences, or tasks. How may I be of service?"
                        userName.isNotBlank() -> "নমস্কার $userName! আমি মাহি ✨— আপনার পার্সোনাল JARVIS এআই অ্যাসিস্ট্যান্ট।\nআপনার প্রোফাইল ও সমস্ত মেমোরি সফলভাবে লোড করা হয়েছে। আজ কীভাবে আপনাকে সাহায্য করতে পারি?"
                        else -> "নমস্কার! আমি মাহি ✨— আপনার পার্সোনাল JARVIS-স্টাইল এআই ডেস্কটপ অ্যাসিস্ট্যান্ট।\nআমার কাছে স্থায়ী পারসিস্টেন্ট মেমোরি আর্কিটেকচার রয়েছে। আপনার নাম, পছন্দ বা কাজের কথা জানালে আমি তা মনে রাখবো। আজ কী সাহায্য করতে পারি?"
                    }
                    memoryRepository.saveMessage(profile.id, Sender.MAHI, greeting)
                } else {
                    _messages.value = entities.map {
                        Message(
                            id = it.id,
                            sender = if (it.sender == "USER") Sender.USER else Sender.MAHI,
                            text = it.text,
                            timestamp = it.timestamp
                        )
                    }
                }
            }
        }

        // Collect persistent memory facts for this user
        factCollectorJob = viewModelScope.launch {
            memoryRepository.getFactsForUser(profile.id).collectLatest { facts ->
                _memoryFacts.value = facts
            }
        }
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        val currentProf = _activeProfile.value ?: return
        if (trimmed.isBlank() || _isLoading.value) return

        _isLoading.value = true
        _errorMessage.value = null

        viewModelScope.launch {
            // Persist User Message to Room immediately
            memoryRepository.saveMessage(currentProf.id, Sender.USER, trimmed)

            // Extract memory eagerly from user input (e.g. immediate name capture)
            val extractedName = memoryRepository.processAndExtractMemory(currentProf.id, trimmed, "")
            val activeName = extractedName ?: currentProf.name
            if (!extractedName.isNullOrBlank() && extractedName != currentProf.name) {
                _activeProfile.value = currentProf.copy(name = extractedName)
            }

            // Retrieve full memory context for AI
            val memoryContext = memoryRepository.getFormattedMemoryContext(currentProf.id)

            val result = aiClient.generateResponse(
                prompt = trimmed,
                history = _messages.value,
                userName = activeName,
                memoryContext = memoryContext,
                customApiKey = _customApiKey.value
            )

            _isLoading.value = false

            result.onSuccess { rawReply ->
                // Clean AI reply of memory tags before persisting and speaking
                val cleanReply = rawReply.replace(Regex("<!--MEMORY_EXTRACT:[\\s\\S]*?-->"), "").trim()

                // Persist any additional memory facts generated by AI
                val aiExtractedName = memoryRepository.processAndExtractMemory(currentProf.id, trimmed, rawReply)
                if (!aiExtractedName.isNullOrBlank() && aiExtractedName != activeName) {
                    _activeProfile.value = _activeProfile.value?.copy(name = aiExtractedName)
                }

                // Persist AI Message to Room
                memoryRepository.saveMessage(currentProf.id, Sender.MAHI, cleanReply)

                if (_autoSpeak.value) {
                    voiceManager.speak(cleanReply)
                }
            }.onFailure { err ->
                val errorText = err.message ?: "অপ্রত্যাশিত ত্রুটি ঘটেছে"
                _errorMessage.value = errorText

                val systemAlert = "⚠️ $errorText\n\n(টিপস: উপরের গিয়ার আইকনে ট্যাপ করে আপনার ফ্রি জেমিনাই API Key যুক্ত করতে পারেন বা নেটওয়ার্ক চেক করুন।)"
                memoryRepository.saveMessage(currentProf.id, Sender.MAHI, systemAlert)
            }
        }
    }

    fun switchProfile(profileId: String) {
        viewModelScope.launch {
            memoryRepository.switchProfile(profileId)
            val updated = memoryRepository.getOrCreateActiveProfile()
            _activeProfile.value = updated
            loadUserData(updated)
        }
    }

    fun createProfile(name: String) {
        viewModelScope.launch {
            val newProfile = memoryRepository.createNewProfile(name)
            _activeProfile.value = newProfile
            loadUserData(newProfile)
        }
    }

    fun updateProfileName(name: String) {
        val current = _activeProfile.value ?: return
        viewModelScope.launch {
            memoryRepository.updateProfileName(current.id, name)
            val updated = current.copy(name = name.trim())
            _activeProfile.value = updated
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            memoryRepository.deleteProfile(profileId)
            val updated = memoryRepository.getOrCreateActiveProfile()
            _activeProfile.value = updated
            loadUserData(updated)
        }
    }

    fun addManualFact(category: String, key: String, value: String) {
        val current = _activeProfile.value ?: return
        viewModelScope.launch {
            memoryRepository.addManualFact(current.id, category, key, value)
        }
    }

    fun deleteFact(factId: String) {
        viewModelScope.launch {
            memoryRepository.deleteFact(factId)
        }
    }

    fun triggerQuickCommand(command: QuickCommand) {
        sendMessage(command.prompt)
    }

    fun startVoiceInput() {
        voiceManager.startListening()
    }

    fun stopVoiceInput() {
        voiceManager.stopListening()
    }

    fun stopSpeaking() {
        voiceManager.stopSpeaking()
    }

    fun speak(text: String) {
        voiceManager.speak(text)
    }

    fun testVoiceAudition() {
        val userName = _activeProfile.value?.name.orEmpty()
        val isHi = activeLanguage.value.startsWith("hi")
        val isEn = activeLanguage.value.startsWith("en")
        val greeting = when {
            isHi && userName.isNotBlank() -> "नमस्ते $userName जी! मैं जার्विस हूँ, आपका पर्सनल एআই असिस्टेंट। आपकी स्थायी मेमोरी सक्रिय है।"
            isHi -> "नमस्ते! मैं जार्विस हूँ, आपका पर्सनल एआई असिस्टेंट। मैं आपकी हर बात और प्राथमिकता को याद रखने के लिए तैयार हूँ।"
            isEn && userName.isNotBlank() -> "Greetings $userName! I am Jarvis, your proactive personal AI assistant. All persistent memory banks are operational."
            isEn -> "Greetings! I am Jarvis, your personal AI assistant. I am ready to serve and remember your instructions."
            userName.isNotBlank() -> "হ্যালো $userName! আমি মাহি ✨। আপনার বিশ্বস্ত এআই সহকারী। আপনার মেমোরি ডাটাবেজ সক্রিয় রয়েছে!"
            else -> "হ্যালো! আমি মাহি ✨। আপনার বিশ্বস্ত এআই সহকারী। আমি উচ্চ বুদ্ধিমত্তা এবং পারসিস্টেন্ট মেমোরিতে আপনার নির্দেশ পালনে প্রস্তুত!"
        }
        voiceManager.speak(greeting)
    }

    fun saveApiKey(newKey: String) {
        val trimmed = newKey.trim()
        _customApiKey.value = trimmed
        prefs.edit().putString("custom_api_key", trimmed).apply()
    }

    fun updatePitch(newPitch: Float) {
        voiceManager.setPitch(newPitch)
        prefs.edit().putFloat("voice_pitch", newPitch).apply()
    }

    fun updateSpeechRate(newRate: Float) {
        voiceManager.setSpeechRate(newRate)
        prefs.edit().putFloat("voice_rate", newRate).apply()
    }

    fun updateLanguage(langCode: String) {
        voiceManager.setLanguage(langCode)
        prefs.edit().putString("voice_lang", langCode).apply()
    }

    fun toggleAutoSpeak(enabled: Boolean) {
        _autoSpeak.value = enabled
        prefs.edit().putBoolean("auto_speak", enabled).apply()
    }

    fun clearHistory() {
        val current = _activeProfile.value ?: return
        viewModelScope.launch {
            memoryRepository.clearMessagesForUser(current.id)
            val userName = current.name.trim()
            val resetMsg = if (userName.isNotBlank()) {
                "কনসোল হিস্ট্রি রিসেট করা হয়েছে, $userName। আপনার সংরক্ষিত মেমোরি ও প্রোফাইল অক্ষুণ্ণ রয়েছে।"
            } else {
                "কনসোল হিস্ট্রি রিসেট করা হয়েছে। আমি মাহি ✨, পরবর্তী নির্দেশের অপেক্ষায় আছি।"
            }
            memoryRepository.saveMessage(current.id, Sender.MAHI, resetMsg)
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.destroy()
    }
}
