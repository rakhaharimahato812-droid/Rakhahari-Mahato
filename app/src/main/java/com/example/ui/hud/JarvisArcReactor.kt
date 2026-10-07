package com.example.ui.hud

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBlack
import com.example.ui.theme.JarvisBlue
import com.example.ui.theme.JarvisCyan
import com.example.ui.theme.JarvisCyanGlow
import com.example.ui.theme.JarvisCyanLight
import com.example.ui.theme.JarvisDarkBlue
import com.example.ui.theme.NeonAmber
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonPink
import kotlin.math.cos
import kotlin.math.sin

enum class CoreState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING
}

@Composable
fun JarvisArcReactor(
    state: CoreState,
    amplitude: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "jarvis_core")

    // Slow continuous outer rotation
    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == CoreState.THINKING) 3000 else 12000, easing = LinearEasing)
        ),
        label = "outer_rot"
    )

    // Counter rotation for inner ring
    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (state == CoreState.THINKING) 2500 else 8000, easing = LinearEasing)
        ),
        label = "inner_rot"
    )

    // Breathing pulse for core glow
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val coreGlowColor = when (state) {
        CoreState.IDLE -> JarvisCyan
        CoreState.LISTENING -> NeonAmber
        CoreState.THINKING -> NeonPink
        CoreState.SPEAKING -> JarvisCyanLight
    }

    val stateText = when (state) {
        CoreState.IDLE -> "MAHI • ONLINE"
        CoreState.LISTENING -> "LISTENING..."
        CoreState.THINKING -> "PROCESSING..."
        CoreState.SPEAKING -> "SPEAKING..."
    }

    Box(
        modifier = modifier
            .size(240.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2f - 12.dp.toPx()

            // Dynamic amplitude effect
            val ampBoost = if (state == CoreState.LISTENING || state == CoreState.SPEAKING) {
                amplitude * 18.dp.toPx()
            } else 0f

            // 1. Outermost Ambient Halo Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreGlowColor.copy(alpha = 0.35f * pulse),
                        coreGlowColor.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.05f + ampBoost
                ),
                radius = radius * 1.05f + ampBoost,
                center = center
            )

            // 2. Outermost Static HUD Ring
            drawCircle(
                color = JarvisCyan.copy(alpha = 0.25f),
                radius = radius,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 3. Outer Rotating Dashed Ring with Segmented Ticks
            rotate(outerRotation, pivot = center) {
                drawCircle(
                    color = JarvisCyan.copy(alpha = 0.65f),
                    radius = radius - 4.dp.toPx(),
                    center = center,
                    style = Stroke(
                        width = 2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 16f, 8f, 16f), 0f)
                    )
                )

                // 24 Angular tick marks
                val tickCount = 24
                for (i in 0 until tickCount) {
                    val angle = (i * 360f / tickCount) * (Math.PI / 180f)
                    val isMajor = i % 6 == 0
                    val tickLen = if (isMajor) 10.dp.toPx() else 4.dp.toPx()
                    val tickAlpha = if (isMajor) 0.9f else 0.4f
                    val strokeW = if (isMajor) 2.5.dp.toPx() else 1.dp.toPx()

                    val startX = center.x + ((radius - tickLen) * cos(angle)).toFloat()
                    val startY = center.y + ((radius - tickLen) * sin(angle)).toFloat()
                    val endX = center.x + (radius * cos(angle)).toFloat()
                    val endY = center.y + (radius * sin(angle)).toFloat()

                    drawLine(
                        color = (if (isMajor) coreGlowColor else JarvisCyan).copy(alpha = tickAlpha),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY),
                        strokeWidth = strokeW,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 4. Middle Arc Reactor Segments (Clockwise & Counter Clockwise arcs)
            rotate(innerRotation, pivot = center) {
                val midRadius = radius * 0.72f
                val arcStroke = Stroke(
                    width = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )

                // Arc 1
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(JarvisCyan, JarvisBlue, JarvisCyanLight),
                        center = center
                    ),
                    startAngle = 0f,
                    sweepAngle = 70f,
                    useCenter = false,
                    style = arcStroke,
                    topLeft = Offset(center.x - midRadius, center.y - midRadius),
                    size = Size(midRadius * 2, midRadius * 2)
                )

                // Arc 2
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(JarvisCyan, JarvisBlue, JarvisCyanLight),
                        center = center
                    ),
                    startAngle = 120f,
                    sweepAngle = 70f,
                    useCenter = false,
                    style = arcStroke,
                    topLeft = Offset(center.x - midRadius, center.y - midRadius),
                    size = Size(midRadius * 2, midRadius * 2)
                )

                // Arc 3
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(JarvisCyan, JarvisBlue, JarvisCyanLight),
                        center = center
                    ),
                    startAngle = 240f,
                    sweepAngle = 70f,
                    useCenter = false,
                    style = arcStroke,
                    topLeft = Offset(center.x - midRadius, center.y - midRadius),
                    size = Size(midRadius * 2, midRadius * 2)
                )
            }

            // 5. Secondary Counter-spinning dotted Ring
            rotate(outerRotation * -1.5f, pivot = center) {
                val dotRadius = radius * 0.54f
                drawCircle(
                    color = coreGlowColor.copy(alpha = 0.5f),
                    radius = dotRadius,
                    center = center,
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f), 0f)
                    )
                )
            }

            // 6. Central Arc Reactor Core with Gradient and Dynamic Pulse
            val coreRadius = (radius * 0.38f) * (if (state == CoreState.THINKING) pulse else 1.0f) + (ampBoost * 0.5f)

            // Inner dark base
            drawCircle(
                color = CyberBlack,
                radius = coreRadius,
                center = center
            )

            // Center radial glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        coreGlowColor,
                        JarvisDarkBlue,
                        Color.Transparent
                    ),
                    center = center,
                    radius = coreRadius * 1.2f
                ),
                radius = coreRadius,
                center = center
            )

            // Core boundary border
            drawCircle(
                color = coreGlowColor.copy(alpha = 0.9f),
                radius = coreRadius,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Center HUD Text & Indicator
        Box(
            modifier = Modifier.padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "✨ MAHI",
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                color = JarvisCyanLight,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 2.sp
            )
        }
    }
}
