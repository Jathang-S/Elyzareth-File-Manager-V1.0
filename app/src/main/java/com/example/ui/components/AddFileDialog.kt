package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FolderModel
import com.example.model.MediaType
import com.example.ui.theme.WinampBorder
import com.example.ui.theme.WinampElectricCyan
import com.example.ui.theme.WinampNeonGreen
import com.example.ui.theme.WinampSurface
import com.example.ui.theme.WinampSurfaceElevated
import com.example.ui.theme.WinampTextMuted
import com.example.ui.theme.WinampTextPrimary
import com.example.ui.theme.WinampTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddFileDialog(
    folders: List<FolderModel>,
    currentFolderId: Long?,
    onDismiss: () -> Unit,
    onAddFile: (name: String, ext: String, folderId: Long, sizeBytes: Long, mime: String, type: MediaType, durationMs: Long?, meta: String?) -> Unit
) {
    var fileName by remember { mutableStateOf("") }
    var selectedExt by remember { mutableStateOf("mp3") }
    var selectedFolderId by remember {
        mutableStateOf(currentFolderId ?: folders.firstOrNull()?.id ?: 1L)
    }

    val extensions = listOf("mp3", "flac", "wav", "mp4", "png", "jpg", "pdf", "zip")

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("add_file_dialog"),
        containerColor = WinampSurface,
        title = {
            Text(
                text = "IMPORT MEDIA / FILE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = WinampTextPrimary
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("File Name") },
                    singleLine = true,
                    placeholder = { Text("e.g. Bassline Stems 02") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = WinampElectricCyan,
                        unfocusedBorderColor = WinampBorder,
                        focusedTextColor = WinampTextPrimary,
                        unfocusedTextColor = WinampTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_file_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "FILE EXTENSION:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WinampTextMuted,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    extensions.forEach { ext ->
                        val isSelected = selectedExt == ext
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) WinampElectricCyan else WinampSurfaceElevated)
                                .border(1.dp, if (isSelected) Color.White else WinampBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedExt = ext }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = "." + ext.uppercase(),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.Black else WinampTextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "TARGET FOLDER:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = WinampTextMuted,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    folders.forEach { f ->
                        val isSelected = selectedFolderId == f.id
                        val color = parseColor(f.colorHex)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) color.copy(alpha = 0.3f) else WinampSurfaceElevated)
                                .border(1.dp, if (isSelected) color else WinampBorder, RoundedCornerShape(6.dp))
                                .clickable { selectedFolderId = f.id }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = f.name,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) WinampTextPrimary else WinampTextSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (fileName.isNotBlank()) {
                        val mediaType = MediaType.fromExtension(selectedExt)
                        val size = when (mediaType) {
                            MediaType.AUDIO -> (3_000_000L..15_000_000L).random()
                            MediaType.VIDEO -> (20_000_000L..120_000_000L).random()
                            MediaType.IMAGE -> (1_500_000L..6_000_000L).random()
                            MediaType.DOCUMENT -> (500_000L..3_000_000L).random()
                            else -> (1_000_000L..10_000_000L).random()
                        }
                        val duration = if (mediaType == MediaType.AUDIO || mediaType == MediaType.VIDEO) {
                            (60_000L..360_000L).random()
                        } else null

                        val meta = when (selectedExt) {
                            "mp3" -> "320 kbps MP3 Audio"
                            "flac" -> "1411 kbps Hi-Res FLAC"
                            "wav" -> "24-bit 48kHz WAV"
                            "mp4" -> "1080p 60fps Video"
                            "png" -> "3840x2160 RGBA"
                            "jpg" -> "4032x3024 JPEG"
                            "pdf" -> "Standard Document"
                            "zip" -> "Compressed Archive"
                            else -> "General File"
                        }

                        val mime = when (mediaType) {
                            MediaType.AUDIO -> "audio/$selectedExt"
                            MediaType.VIDEO -> "video/$selectedExt"
                            MediaType.IMAGE -> "image/$selectedExt"
                            MediaType.DOCUMENT -> "application/$selectedExt"
                            else -> "application/octet-stream"
                        }

                        onAddFile(
                            fileName.trim(),
                            selectedExt,
                            selectedFolderId,
                            size,
                            mime,
                            mediaType,
                            duration,
                            meta
                        )
                    }
                },
                enabled = fileName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WinampNeonGreen,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("submit_add_file_button")
            ) {
                Text(
                    text = "ADD TO PLAYLIST",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", fontFamily = FontFamily.Monospace, color = WinampTextMuted)
            }
        }
    )
}
