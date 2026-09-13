package com.example.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileItem
import com.example.ui.theme.WinampBorder
import com.example.ui.theme.WinampElectricCyan
import com.example.ui.theme.WinampNeonAmber
import com.example.ui.theme.WinampNeonGreen
import com.example.ui.theme.WinampSurface
import com.example.ui.theme.WinampSurfaceElevated
import com.example.ui.theme.WinampTextMuted
import com.example.ui.theme.WinampTextPrimary
import java.util.Locale

@Composable
fun InlineAudioPlayer(
    file: FileItem,
    isPlaying: Boolean,
    currentPositionMs: Long,
    durationMs: Long,
    onTogglePlay: () -> Unit,
    onSeekTo: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val effectiveDuration = if (durationMs > 0) durationMs else (file.durationMs ?: 1L).coerceAtLeast(1L)
    val progressFraction = (currentPositionMs.toFloat() / effectiveDuration.toFloat()).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(WinampSurfaceElevated)
            .border(1.dp, WinampBorder, RoundedCornerShape(6.dp))
            .padding(10.dp)
            .testTag("inline_audio_player_${file.id}")
    ) {
        // Equalizer & Track Readout
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Play / Pause Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) WinampNeonGreen else WinampSurface)
                        .border(1.dp, if (isPlaying) WinampNeonGreen else WinampElectricCyan, CircleShape)
                ) {
                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("audio_play_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = if (isPlaying) Color.Black else WinampNeonGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = file.fullName,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = WinampTextPrimary,
                        maxLines = 1
                    )
                    Text(
                        text = "${file.bitrateKbps ?: 320} kbps • ${file.sampleRateHz ?: 44100} Hz Stereo",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = WinampNeonGreen
                    )
                }
            }

            // Time Readout
            Text(
                text = "${formatTime(currentPositionMs)} / ${formatTime(effectiveDuration)}",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = WinampElectricCyan
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Seek Slider
        Slider(
            value = progressFraction,
            onValueChange = { frac ->
                onSeekTo((frac * effectiveDuration).toLong())
            },
            colors = SliderDefaults.colors(
                thumbColor = WinampNeonGreen,
                activeTrackColor = WinampNeonGreen,
                inactiveTrackColor = Color(0xFF1E2838)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(24.dp)
                .testTag("audio_seek_slider")
        )

        // Metadata footer
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "FOLDER: ${file.folderName}",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = WinampTextMuted
            )
            Text(
                text = "STREAMING (ZERO-RAM)",
                fontFamily = FontFamily.Monospace,
                fontSize = 9.sp,
                color = WinampNeonAmber
            )
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSec = ms / 1000
    val min = totalSec / 60
    val sec = totalSec % 60
    return String.format(Locale.US, "%02d:%02d", min, sec)
}
