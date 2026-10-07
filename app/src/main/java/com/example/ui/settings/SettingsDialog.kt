package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.CyberNavy
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisDarkBlue
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDialog(
    currentApiKey: String,
    currentPitch: Float,
    currentRate: Float,
    currentLanguage: String,
    autoSpeakEnabled: Boolean,
    onSaveApiKey: (String) -> Unit,
    onPitchChange: (Float) -> Unit,
    onRateChange: (Float) -> Unit,
    onLanguageChange: (String) -> Unit,
    onAutoSpeakToggle: (Boolean) -> Unit,
    onTestVoice: () -> Unit,
    onDismiss: () -> Unit
) {
    var apiKeyInput by remember { mutableStateOf(currentApiKey) }
    var showKey by remember { mutableStateOf(false) }
    var pitchVal by remember { mutableFloatStateOf(currentPitch) }
    var rateVal by remember { mutableFloatStateOf(currentRate) }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CyberNavy, RoundedCornerShape(16.dp))
                .border(1.5.dp, JarvisCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "MAHI CONFIGURATION",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = JarvisCyanLight,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "JARVIS অডিও ও এআই সেটিংস",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_settings_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }

                // 1. API Key Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberSurface, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = NeonGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "জেমিনাই এপিআই কী (FREE GEMINI API KEY)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGold
                        )
                    }

                    Text(
                        text = "Google AI Studio থেকে প্রাপ্ত ফ্রি এপিআই কী ব্যবহার করতে পারেন (Configured by Secrets or Custom Key)।",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input"),
                        placeholder = { Text("AIzaSy... (API Key Paste করুন)", color = TextMuted) },
                        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = CyberBlack,
                            unfocusedContainerColor = CyberBlack,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedIndicatorColor = JarvisCyan,
                            unfocusedIndicatorColor = JarvisCyan.copy(alpha = 0.3f)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            onSaveApiKey(apiKeyInput)
                        })
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { showKey = !showKey },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberNavy),
                            modifier = Modifier.testTag("toggle_key_visibility")
                        ) {
                            Text(if (showKey) "লুকান" else "দেখান", fontSize = 11.sp, color = TextSecondary)
                        }

                        Button(
                            onClick = {
                                onSaveApiKey(apiKeyInput)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = JarvisBlue),
                            modifier = Modifier.testTag("save_api_key_btn")
                        ) {
                            Text("সেভ করুন", fontSize = 12.sp, color = TextPrimary)
                        }
                    }
                }

                // 2. Voice & Audio Section (Bangla Female Voice)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberSurface, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = JarvisCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "মাহির নারী কণ্ঠ টিউনিং (BANGLA FEMALE VOICE)",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = JarvisCyanLight
                        )
                    }

                    // Pitch Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("ভয়েস পিচ (Female Pitch)", fontSize = 12.sp, color = TextPrimary)
                            Text(
                                String.format("%.2fx", pitchVal),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = JarvisCyanLight
                            )
                        }
                        Slider(
                            value = pitchVal,
                            onValueChange = {
                                pitchVal = it
                                onPitchChange(it)
                            },
                            valueRange = 0.8f..1.6f,
                            colors = SliderDefaults.colors(
                                thumbColor = JarvisCyan,
                                activeTrackColor = JarvisCyan,
                                inactiveTrackColor = CyberBlack
                            ),
                            modifier = Modifier.testTag("pitch_slider")
                        )
                    }

                    // Speech Rate Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("কথা বলার গতি (Speed)", fontSize = 12.sp, color = TextPrimary)
                            Text(
                                String.format("%.2fx", rateVal),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                color = JarvisCyanLight
                            )
                        }
                        Slider(
                            value = rateVal,
                            onValueChange = {
                                rateVal = it
                                onRateChange(it)
                            },
                            valueRange = 0.7f..1.4f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonGold,
                                activeTrackColor = NeonGold,
                                inactiveTrackColor = CyberBlack
                            ),
                            modifier = Modifier.testTag("rate_slider")
                        )
                    }

                    // Language Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ভাষা (Language):", fontSize = 12.sp, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            val isBn = currentLanguage.startsWith("bn")
                            val isHi = currentLanguage.startsWith("hi")
                            val isEn = !isBn && !isHi

                            Button(
                                onClick = { onLanguageChange("bn-BD") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isBn) JarvisCyan else CyberNavy
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("lang_bn_btn")
                            ) {
                                Text("বাংলা", color = if (isBn) CyberBlack else TextPrimary, fontSize = 10.sp)
                            }
                            Button(
                                onClick = { onLanguageChange("hi-IN") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isHi) JarvisCyan else CyberNavy
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("lang_hi_btn")
                            ) {
                                Text("हिन्दी", color = if (isHi) CyberBlack else TextPrimary, fontSize = 10.sp)
                            }
                            Button(
                                onClick = { onLanguageChange("en-US") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isEn) JarvisCyan else CyberNavy
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("lang_en_btn")
                            ) {
                                Text("English", color = if (isEn) CyberBlack else TextPrimary, fontSize = 10.sp)
                            }
                        }
                    }

                    // Auto Speak Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("অটো স্পিক (Auto-Speak)", fontSize = 12.sp, color = TextPrimary)
                            Text("মাহির উত্তর স্বয়ংক্রিয়ভাবে পড়ে শোনাবে", fontSize = 10.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = autoSpeakEnabled,
                            onCheckedChange = onAutoSpeakToggle,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JarvisCyan,
                                checkedTrackColor = JarvisDarkBlue,
                                uncheckedThumbColor = TextMuted,
                                uncheckedTrackColor = CyberBlack
                            ),
                            modifier = Modifier.testTag("auto_speak_switch")
                        )
                    }

                    // Voice Audition Test Button
                    Button(
                        onClick = onTestVoice,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("test_voice_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = JarvisCyan)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            tint = CyberBlack,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "মাহির কণ্ঠ পরীক্ষা করুন (Test Voice)",
                            color = CyberBlack,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
