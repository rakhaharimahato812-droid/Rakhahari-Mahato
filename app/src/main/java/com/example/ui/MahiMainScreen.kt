package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.chat.ChatMessageCard
import com.example.ui.hud.AudioWaveformVisualizer
import com.example.ui.hud.CoreState
import com.example.ui.hud.JarvisArcReactor
import com.example.ui.hud.JarvisTelemetryHud
import com.example.ui.memory.UserMemoryDialog
import com.example.ui.settings.SettingsDialog
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberNavy
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisDarkBlue
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MahiMainScreen(
    viewModel: MahiViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val messages by viewModel.messages.collectAsStateWithLifecycle()
    val activeProfile by viewModel.activeProfile.collectAsStateWithLifecycle()
    val allProfiles by viewModel.allProfiles.collectAsStateWithLifecycle()
    val memoryFacts by viewModel.memoryFacts.collectAsStateWithLifecycle()

    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val isVoiceReady by viewModel.isVoiceReady.collectAsStateWithLifecycle()
    val audioAmplitude by viewModel.audioAmplitude.collectAsStateWithLifecycle()
    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
    val activeLanguage by viewModel.activeLanguage.collectAsStateWithLifecycle()
    val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
    val autoSpeak by viewModel.autoSpeak.collectAsStateWithLifecycle()
    val voicePitch by viewModel.voicePitch.collectAsStateWithLifecycle()
    val speechRate by viewModel.speechRate.collectAsStateWithLifecycle()

    var showSettings by remember { mutableStateOf(false) }
    var showMemoryDialog by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Permission launcher for microphone
    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startVoiceInput()
        } else {
            Toast.makeText(context, "ভয়েস ব্যবহারের জন্য মাইক্রোফোন অনুমতি প্রয়োজন", Toast.LENGTH_SHORT).show()
        }
    }

    val onMicClick: () -> Unit = {
        if (isListening) {
            viewModel.stopVoiceInput()
        } else {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                viewModel.startVoiceInput()
            } else {
                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val coreState = when {
        isListening -> CoreState.LISTENING
        isLoading -> CoreState.THINKING
        isSpeaking -> CoreState.SPEAKING
        else -> CoreState.IDLE
    }

    val activeUserName = activeProfile?.name.orEmpty()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CyberBlack,
        topBar = {
            TopAppBarHud(
                activeLanguage = activeLanguage,
                isVoiceReady = isVoiceReady,
                activeUserName = activeUserName,
                memoryCount = memoryFacts.size,
                onOpenMemory = { showMemoryDialog = true },
                onTestVoice = { viewModel.testVoiceAudition() },
                onClearHistory = { viewModel.clearHistory() },
                onOpenSettings = { showSettings = true }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isWideScreen = maxWidth >= 760.dp

            if (isWideScreen) {
                // Desktop / Landscape Wide Layout: Split Screen
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Panel: Arc Reactor Core & Telemetry Controls
                    Box(
                        modifier = Modifier
                            .width(360.dp)
                            .fillMaxHeight()
                            .background(CyberNavy.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .border(1.dp, JarvisCyan.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "HOLOGRAPHIC CORE",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = JarvisCyanLight,
                                letterSpacing = 2.sp
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Glowing Arc Reactor
                            JarvisArcReactor(
                                state = coreState,
                                amplitude = audioAmplitude,
                                modifier = Modifier.size(200.dp),
                                onClick = onMicClick
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // Reactive Waveform Equalizer
                            AudioWaveformVisualizer(
                                isActive = isListening || isSpeaking,
                                amplitude = audioAmplitude,
                                isListening = isListening,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            // Status Pill with identified user badge
                            StatusPill(
                                statusMessage = if (activeUserName.isNotBlank() && coreState == CoreState.IDLE) {
                                    "$activeUserName • স্মৃতি সক্রিয় (Memory Online)"
                                } else {
                                    statusMessage
                                },
                                coreState = coreState
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Voice Actions
                            VoiceControlBar(
                                isListening = isListening,
                                isSpeaking = isSpeaking,
                                onMicClick = onMicClick,
                                onStopSpeaking = { viewModel.stopSpeaking() }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Telemetry HUD with user & memory status
                            JarvisTelemetryHud(
                                isVoiceReady = isVoiceReady,
                                isListening = isListening,
                                isSpeaking = isSpeaking,
                                activeLanguage = activeLanguage,
                                userName = activeUserName,
                                memoryCount = memoryFacts.size
                            )
                        }
                    }

                    // Right Panel: Terminal Feed & Console Input
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        QuickCommandsRow(
                            onCommandClick = { viewModel.triggerQuickCommand(it) },
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // Message Feed
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .background(CyberNavy.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                                .border(1.dp, JarvisCyan.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                .padding(10.dp)
                        ) {
                            LazyColumn(
                                state = listState,
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(messages, key = { it.id }) { msg ->
                                    ChatMessageCard(
                                        message = msg,
                                        isSpeakingThis = isSpeaking && msg.sender == com.example.ai.Sender.MAHI,
                                        onSpeakClick = { viewModel.speak(it) }
                                    )
                                }

                                if (isLoading) {
                                    item {
                                        ThinkingIndicator()
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Futuristic Input Console
                        FuturisticInputConsole(
                            textInput = textInput,
                            isLoading = isLoading,
                            isListening = isListening,
                            activeUserName = activeUserName,
                            onTextChange = { textInput = it },
                            onSend = {
                                if (textInput.isNotBlank()) {
                                    viewModel.sendMessage(textInput)
                                    textInput = ""
                                }
                            },
                            onMicClick = onMicClick
                        )
                    }
                }
            } else {
                // Mobile / Portrait Compact Layout
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Compact Arc Reactor & Waveform Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CyberNavy.copy(alpha = 0.65f), RoundedCornerShape(16.dp))
                            .border(1.dp, JarvisCyan.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                            .padding(vertical = 10.dp, horizontal = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatusPill(
                                statusMessage = if (activeUserName.isNotBlank() && coreState == CoreState.IDLE) {
                                    "$activeUserName • মেমোরি রেডি"
                                } else {
                                    statusMessage
                                },
                                coreState = coreState
                            )

                            if (isSpeaking) {
                                Button(
                                    onClick = { viewModel.stopSpeaking() },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Stop,
                                        contentDescription = "Stop",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("থামান", fontSize = 11.sp, color = Color.White)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        JarvisArcReactor(
                            state = coreState,
                            amplitude = audioAmplitude,
                            modifier = Modifier.size(140.dp),
                            onClick = onMicClick
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        AudioWaveformVisualizer(
                            isActive = isListening || isSpeaking,
                            amplitude = audioAmplitude,
                            isListening = isListening,
                            barCount = 18,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    QuickCommandsRow(
                        onCommandClick = { viewModel.triggerQuickCommand(it) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    // Message Feed
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(messages, key = { it.id }) { msg ->
                                ChatMessageCard(
                                    message = msg,
                                    isSpeakingThis = isSpeaking && msg.sender == com.example.ai.Sender.MAHI,
                                    onSpeakClick = { viewModel.speak(it) }
                                )
                            }

                            if (isLoading) {
                                item {
                                    ThinkingIndicator()
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    FuturisticInputConsole(
                        textInput = textInput,
                        isLoading = isLoading,
                        isListening = isListening,
                        activeUserName = activeUserName,
                        onTextChange = { textInput = it },
                        onSend = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendMessage(textInput)
                                textInput = ""
                            }
                        },
                        onMicClick = onMicClick
                    )
                }
            }
        }
    }

    if (showMemoryDialog) {
        UserMemoryDialog(
            activeProfile = activeProfile,
            allProfiles = allProfiles,
            memoryFacts = memoryFacts,
            onSwitchProfile = { viewModel.switchProfile(it) },
            onCreateProfile = { viewModel.createProfile(it) },
            onUpdateName = { viewModel.updateProfileName(it) },
            onDeleteProfile = { viewModel.deleteProfile(it) },
            onDeleteFact = { viewModel.deleteFact(it) },
            onAddFact = { cat, k, v -> viewModel.addManualFact(cat, k, v) },
            onDismiss = { showMemoryDialog = false }
        )
    }

    if (showSettings) {
        SettingsDialog(
            currentApiKey = customApiKey,
            currentPitch = voicePitch,
            currentRate = speechRate,
            currentLanguage = activeLanguage,
            autoSpeakEnabled = autoSpeak,
            onSaveApiKey = {
                viewModel.saveApiKey(it)
                Toast.makeText(context, "API Key সংরক্ষিত হয়েছে", Toast.LENGTH_SHORT).show()
            },
            onPitchChange = { viewModel.updatePitch(it) },
            onRateChange = { viewModel.updateSpeechRate(it) },
            onLanguageChange = { viewModel.updateLanguage(it) },
            onAutoSpeakToggle = { viewModel.toggleAutoSpeak(it) },
            onTestVoice = { viewModel.testVoiceAudition() },
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
fun TopAppBarHud(
    activeLanguage: String,
    isVoiceReady: Boolean,
    activeUserName: String,
    memoryCount: Int,
    onOpenMemory: () -> Unit,
    onTestVoice: () -> Unit,
    onClearHistory: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberNavy)
            .border(1.dp, JarvisCyan.copy(alpha = 0.2f))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(if (isVoiceReady) NeonGreen else NeonGold, CircleShape)
            )
            Column {
                Text(
                    text = "MAHI ✨ // JARVIS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = JarvisCyanLight,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "DESKTOP AI ASSISTANT",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = TextSecondary
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Memory & Profile Selector Button
            Box(
                modifier = Modifier
                    .background(CyberSurface, RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        if (activeUserName.isNotBlank()) JarvisCyanLight else JarvisCyan.copy(alpha = 0.35f),
                        RoundedCornerShape(8.dp)
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenMemory() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("appbar_memory_bank_btn")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Psychology,
                        contentDescription = "Memory Bank",
                        tint = if (activeUserName.isNotBlank()) JarvisCyanLight else NeonGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (activeUserName.isNotBlank()) activeUserName else "মেমোরি ($memoryCount)",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = if (activeUserName.isNotBlank()) JarvisCyanLight else TextSecondary,
                        maxLines = 1
                    )
                }
            }

            // Audition Voice Button
            IconButton(
                onClick = onTestVoice,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("appbar_test_voice_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = "Test voice",
                    tint = JarvisCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Clear history
            IconButton(
                onClick = onClearHistory,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("appbar_clear_history_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Clear history",
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Settings
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("appbar_settings_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = JarvisCyanLight,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun StatusPill(
    statusMessage: String,
    coreState: CoreState
) {
    val pillBg = when (coreState) {
        CoreState.IDLE -> CyberSurface
        CoreState.LISTENING -> NeonAmber.copy(alpha = 0.2f)
        CoreState.THINKING -> NeonPink.copy(alpha = 0.2f)
        CoreState.SPEAKING -> JarvisCyan.copy(alpha = 0.2f)
    }

    val pillColor = when (coreState) {
        CoreState.IDLE -> TextSecondary
        CoreState.LISTENING -> NeonAmber
        CoreState.THINKING -> NeonPink
        CoreState.SPEAKING -> JarvisCyanLight
    }

    Box(
        modifier = Modifier
            .background(pillBg, RoundedCornerShape(20.dp))
            .border(1.dp, pillColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = statusMessage,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            color = pillColor,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun VoiceControlBar(
    isListening: Boolean,
    isSpeaking: Boolean,
    onMicClick: () -> Unit,
    onStopSpeaking: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            onClick = onMicClick,
            modifier = Modifier
                .weight(1f)
                .height(46.dp)
                .testTag("voice_input_toggle_btn"),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isListening) NeonAmber else JarvisCyan
            ),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Voice Input",
                tint = CyberBlack,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isListening) "শোনা বন্ধ করুন" else "কথা বলুন (Voice)",
                color = CyberBlack,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        if (isSpeaking) {
            Button(
                onClick = onStopSpeaking,
                modifier = Modifier
                    .height(46.dp)
                    .testTag("stop_voice_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop Voice",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("থামান", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun QuickCommandsRow(
    onCommandClick: (QuickCommand) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickCommand.values().forEach { cmd ->
            Box(
                modifier = Modifier
                    .background(CyberSurface.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                    .border(1.dp, JarvisCyan.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onCommandClick(cmd) }
                    .padding(horizontal = 10.dp, vertical = 6.dp)
                    .testTag("quick_cmd_${cmd.name}")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = cmd.icon, fontSize = 12.sp)
                    Text(
                        text = cmd.title,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuturisticInputConsole(
    textInput: String,
    isLoading: Boolean,
    isListening: Boolean,
    activeUserName: String = "",
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onMicClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CyberNavy, RoundedCornerShape(14.dp))
            .border(1.5.dp, JarvisCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Mic Button
        IconButton(
            onClick = onMicClick,
            modifier = Modifier
                .size(42.dp)
                .background(
                    if (isListening) NeonAmber else CyberSurface,
                    CircleShape
                )
                .border(
                    1.dp,
                    if (isListening) NeonGold else JarvisCyan.copy(alpha = 0.4f),
                    CircleShape
                )
                .testTag("console_mic_btn")
        ) {
            Icon(
                imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Voice Input",
                tint = if (isListening) CyberBlack else JarvisCyan,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Text field
        OutlinedTextField(
            value = textInput,
            onValueChange = onTextChange,
            modifier = Modifier
                .weight(1f)
                .testTag("console_text_input"),
            placeholder = {
                Text(
                    text = when {
                        isListening -> "শুনছি... কথা বলুন..."
                        activeUserName.isNotBlank() -> "$activeUserName, বাংলায় কিছু বলুন..."
                        else -> "আমার নাম..., অথবা কোনো কাজ বা প্রশ্ন বলুন..."
                    },
                    color = TextMuted,
                    fontSize = 13.sp
                )
            },
            singleLine = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() })
        )

        Spacer(modifier = Modifier.width(4.dp))

        // Send Button
        IconButton(
            onClick = onSend,
            enabled = textInput.isNotBlank() && !isLoading,
            modifier = Modifier
                .size(42.dp)
                .background(
                    if (textInput.isNotBlank() && !isLoading) JarvisCyan else CyberSurface,
                    CircleShape
                )
                .testTag("console_send_btn")
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = JarvisCyanLight,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint = if (textInput.isNotBlank()) CyberBlack else TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ThinkingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(CyberNavy.copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .border(1.dp, NeonPink.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = NeonPink,
                strokeWidth = 2.dp
            )
            Text(
                text = "মাহি চিন্তা করছে এবং স্মৃতি মিলিয়ে নিচ্ছে... (Mahi Recall & Thinking)",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = JarvisCyanLight
            )
        }
    }
}
