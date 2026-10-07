package com.example.ui.hud

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberNavy
import com.example.ui.theme.CyberSurface
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.NeonPink
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun JarvisTelemetryHud(
    isVoiceReady: Boolean,
    isListening: Boolean,
    isSpeaking: Boolean,
    activeLanguage: String,
    userName: String = "",
    memoryCount: Int = 0,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CyberNavy.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
            .border(1.dp, JarvisCyan.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (isVoiceReady) NeonGreen else NeonGold, CircleShape)
                    )
                    Text(
                        text = "MAHI JARVIS PROTOCOL // V2.5",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = JarvisCyanLight,
                        letterSpacing = 1.sp
                    )
                }

                Text(
                    text = "MEMORY ARCHITECTURE",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.sp,
                    color = TextMuted
                )
            }

            // Row 1: Core System
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryItem(
                    label = "AI CORE",
                    value = "GEMINI 3.5",
                    color = JarvisCyan,
                    modifier = Modifier.weight(1f)
                )
                TelemetryItem(
                    label = "VOICE",
                    value = when {
                        activeLanguage.startsWith("bn") -> "BANGLA"
                        activeLanguage.startsWith("hi") -> "HINDI"
                        else -> "ENGLISH"
                    },
                    color = NeonGold,
                    modifier = Modifier.weight(1f)
                )
                TelemetryItem(
                    label = "STATE",
                    value = when {
                        isSpeaking -> "SPEAKING"
                        isListening -> "LISTENING"
                        else -> "ONLINE"
                    },
                    color = if (isListening) NeonGold else if (isSpeaking) JarvisCyanLight else NeonGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            // Row 2: User Identity & Persistent Memory Bank
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TelemetryItem(
                    label = "ACTIVE USER",
                    value = if (userName.isNotBlank()) userName.uppercase() else "IDENTIFYING...",
                    color = if (userName.isNotBlank()) JarvisCyanLight else NeonGold,
                    modifier = Modifier.weight(1.4f)
                )
                TelemetryItem(
                    label = "PERSISTENT MEMORY",
                    value = "$memoryCount RECALL ITEMS",
                    color = NeonPink,
                    modifier = Modifier.weight(1.6f)
                )
            }
        }
    }
}

@Composable
fun TelemetryItem(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(CyberSurface.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                color = TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
        }
    }
}
