package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.theme.WinampElectricCyan
import com.example.ui.theme.WinampNeonAmber
import com.example.ui.theme.WinampNeonGreen
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WinampVisualizer(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 14
) {
    val transition = rememberInfiniteTransition(label = "winamp_viz")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isPlaying) 650 else 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "phase"
    )

    Canvas(
        modifier = modifier
            .width((barCount * 5).dp)
            .height(20.dp)
    ) {
        val barWidth = 3.dp.toPx()
        val spacing = 2.dp.toPx()

        if (!isPlaying) {
            // Almost static when nothing is playing: subtle resting bars
            for (i in 0 until barCount) {
                val x = i * (barWidth + spacing)
                val restingHeight = (3f + (i % 2) * 1.5f).coerceAtMost(size.height)
                drawRect(
                    color = Color(0xFF223042),
                    topLeft = Offset(x, size.height - restingHeight),
                    size = Size(barWidth, restingHeight)
                )
            }
            return@Canvas
        }

        // Functional live playback animation
        for (i in 0 until barCount) {
            val harmonic1 = sin(phase * 6.28f + i * 0.85f) * 0.45f
            val harmonic2 = sin(phase * 12.56f + i * 1.4f) * 0.25f
            val harmonic3 = cos(phase * 3.14f + i * 0.5f) * 0.3f
            val combined = (harmonic1 + harmonic2 + harmonic3 + 1f) / 2f
            val baseHeight = (combined * (size.height - 3f)).coerceIn(4f, size.height)

            val x = i * (barWidth + spacing)
            val y = size.height - baseHeight

            // Color bands like classic Winamp: Green on bottom, Amber in middle, Cyan on top
            val barColor = when {
                baseHeight / size.height > 0.75f -> WinampNeonAmber
                baseHeight / size.height > 0.45f -> WinampElectricCyan
                else -> WinampNeonGreen
            }

            drawRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, baseHeight)
            )

            // Top peak dot (floating segment)
            drawRect(
                color = Color.White.copy(alpha = 0.9f),
                topLeft = Offset(x, (y - 2.5f).coerceAtLeast(0f)),
                size = Size(barWidth, 2f)
            )
        }
    }
}
