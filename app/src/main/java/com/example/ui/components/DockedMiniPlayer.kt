package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileItem
import com.example.ui.theme.ElyzarethAccent
import com.example.ui.theme.ElyzarethBorder
import com.example.ui.theme.ElyzarethPrimary
import com.example.ui.theme.ElyzarethSurface
import com.example.ui.theme.ElyzarethSurfaceElevated
import com.example.ui.theme.ElyzarethTextMuted
import com.example.ui.theme.ElyzarethTextPrimary
import com.example.ui.theme.ElyzarethTextSecondary
import java.util.Locale

@Composable
fun DockedMiniPlayer(
    file: FileItem,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onTogglePlay: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onStop: () -> Unit,
    onOpenInfo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveDuration = if (durationMs > 0) durationMs else (file.durationMs ?: 1L).coerceAtLeast(1L)
    val progressFraction = (currentPositionMs.toFloat() / effectiveDuration.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ElyzarethSurface)
            .border(1.dp, ElyzarethBorder, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .testTag("docked_mini_player")
    ) {
        // Label: 🎵 PLAYING
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🎵 PLAYING",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = ElyzarethAccent
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${formatTime(currentPositionMs)} / ${formatTime(effectiveDuration)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = ElyzarethTextMuted
                )
            }

            IconButton(
                onClick = onStop,
                modifier = Modifier
                    .size(22.dp)
                    .testTag("mini_player_close")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Stop",
                    tint = ElyzarethTextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Filename and Play Controls: Deep Roots          ▶  ━━━
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = file.fullName,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ElyzarethTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenInfo() }
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Play / Pause Circle
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (isPlaying) ElyzarethPrimary else ElyzarethSurfaceElevated)
                    .clickable { onTogglePlay() }
                    .testTag("mini_player_toggle_play")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Mini Seek Slider: ━━━●━━━━
        Slider(
            value = progressFraction,
            onValueChange = { frac ->
                onSeekTo((frac * effectiveDuration).toLong())
            },
            colors = SliderDefaults.colors(
                thumbColor = ElyzarethPrimary,
                activeTrackColor = ElyzarethPrimary,
                inactiveTrackColor = ElyzarethBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(18.dp)
                .testTag("mini_player_seek_slider")
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format(Locale.US, "%02d:%02d", min, sec)
}
