package com.example.ui.hud

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonGold
import kotlin.random.Random

@Composable
fun AudioWaveformVisualizer(
    isActive: Boolean,
    amplitude: Float,
    isListening: Boolean,
    barCount: Int = 24,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    // Subtle idle wave motion
    val idlePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "idle_phase"
    )

    val primaryColor = if (isListening) NeonAmber else JarvisCyan
    val secondaryColor = if (isListening) NeonGold else JarvisCyanLight

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(3.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val centerIndex = barCount / 2f

        for (i in 0 until barCount) {
            // Calculate base multiplier symmetric from center
            val distFromCenter = Math.abs(i - centerIndex) / centerIndex
            val envelope = (1f - distFromCenter * 0.6f).coerceIn(0.2f, 1f)

            val animatedHeight = remember { Animatable(8f) }

            LaunchedEffect(isActive, amplitude, idlePhase) {
                if (isActive) {
                    // Reactive height driven by speech / audio amplitude
                    val randomJitter = (Random.nextFloat() * 0.4f + 0.8f)
                    val target = (amplitude * 44f * envelope * randomJitter).coerceIn(8f, 48f)
                    animatedHeight.animateTo(
                        targetValue = target,
                        animationSpec = tween(durationMillis = 90, easing = LinearEasing)
                    )
                } else {
                    // Gentle breathing idle wave
                    val sine = Math.sin((idlePhase + i * 0.35f).toDouble()).toFloat()
                    val target = 6f + (sine + 1f) * 3f
                    animatedHeight.animateTo(
                        targetValue = target,
                        animationSpec = tween(durationMillis = 200, easing = LinearEasing)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(animatedHeight.value.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                secondaryColor,
                                primaryColor,
                                JarvisBlue
                            )
                        ),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}
