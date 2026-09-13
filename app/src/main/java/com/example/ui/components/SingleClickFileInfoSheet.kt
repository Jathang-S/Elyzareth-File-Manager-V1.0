package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileMove
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.model.FileItem
import com.example.model.FolderModel
import com.example.model.MediaType
import com.example.ui.theme.ElyzarethAccent
import com.example.ui.theme.ElyzarethAlertRed
import com.example.ui.theme.ElyzarethBackground
import com.example.ui.theme.ElyzarethBorder
import com.example.ui.theme.ElyzarethPrimary
import com.example.ui.theme.ElyzarethSurface
import com.example.ui.theme.ElyzarethSurfaceElevated
import com.example.ui.theme.ElyzarethSurfaceVariant
import com.example.ui.theme.ElyzarethTextMuted
import com.example.ui.theme.ElyzarethTextPrimary
import com.example.ui.theme.ElyzarethTextSecondary
import com.example.ui.theme.getPurposeForColor
import java.io.File
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SingleClickFileInfoSheet(
    file: FileItem,
    folders: List<FolderModel>,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    isPlaying: Boolean = false,
    currentPlaybackPositionMs: Long = 0L,
    audioDurationMs: Long = 0L,
    onTogglePlayAudio: () -> Unit = {},
    onSeekAudio: (Long) -> Unit = {},
    onOpenPhotoPreview: () -> Unit = {},
    onDismiss: () -> Unit,
    onInstantDelete: (Long) -> Unit,
    onRenameClick: (FileItem) -> Unit = {},
    onMoveToFolder: (Long, Long) -> Unit
) {
    var isMovePickerExpanded by remember { mutableStateOf(false) }
    var isTechnicalInfoExpanded by remember { mutableStateOf(false) }

    val folderColor = parseColor(file.folderColorHex)
    val purpose = getPurposeForColor(file.folderColorHex)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = ElyzarethSurface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 4.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(ElyzarethBorder)
            )
        },
        modifier = Modifier.testTag("file_info_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar: ← FILENAME.EXT
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDismiss() }
                    .padding(vertical = 4.dp)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("close_file_info_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ElyzarethPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = if (file.mediaType == MediaType.IMAGE) "PHOTO PREVIEW" else file.fullName.uppercase(Locale.US),
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ElyzarethTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (file.mediaType) {
                MediaType.AUDIO -> {
                    // ──────────────────────────────────────────
                    // AUDIO PREVIEW SURFACE
                    // ──────────────────────────────────────────

                    // Visualizer Block: ▮▮▮▮▮ AUDIO PREVIEW
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElyzarethBackground)
                            .border(1.dp, ElyzarethBorder, RoundedCornerShape(8.dp))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            WinampVisualizer(
                                isPlaying = isPlaying,
                                barCount = 18,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = if (isPlaying) "PLAYING AUDIO" else "AUDIO PREVIEW",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                color = if (isPlaying) ElyzarethAccent else ElyzarethTextMuted
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scrubber line: ▶ ━━━━━━━━━●━━━━━━ 04:18
                    val activeDur = if (audioDurationMs > 0) audioDurationMs else (file.durationMs ?: 1L)
                    val safeDur = if (activeDur > 0) activeDur else 1L
                    val sliderProgress = (currentPlaybackPositionMs.toFloat() / safeDur.toFloat()).coerceIn(0f, 1f)

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(ElyzarethSurfaceElevated)
                            .border(1.dp, ElyzarethBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        // Play / Pause ▶
                        IconButton(
                            onClick = onTogglePlayAudio,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(ElyzarethPrimary)
                                .testTag("sheet_audio_play_button")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        // Progress slider: ━━━━●━━━━
                        Slider(
                            value = sliderProgress,
                            onValueChange = { frac ->
                                val targetMs = (frac * safeDur).toLong()
                                onSeekAudio(targetMs)
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = ElyzarethPrimary,
                                activeTrackColor = ElyzarethPrimary,
                                inactiveTrackColor = ElyzarethBorder
                            ),
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Time display: 04:18
                        val curSec = (currentPlaybackPositionMs / 1000) % 60
                        val curMin = (currentPlaybackPositionMs / 1000) / 60
                        val totalSec = (safeDur / 1000) % 60
                        val totalMin = (safeDur / 1000) / 60
                        Text(
                            text = "%02d:%02d".format(curMin, curSec) + " / " + "%02d:%02d".format(totalMin, totalSec),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElyzarethPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Metadata Stack: MP3   320 kbps   8.4 MB
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetadataPill(file.extension.uppercase())
                        MetadataPill("${file.bitrateKbps ?: 320} kbps")
                        MetadataPill(file.formattedSize)
                        if (file.sampleRateHz != null) {
                            MetadataPill("${file.sampleRateHz / 1000} kHz")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Breadcrumb: Music / Elyzareth / Selected
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElyzarethSurfaceVariant)
                            .border(0.5.dp, ElyzarethBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(folderColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${file.folderName} / ${file.name}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = ElyzarethTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Command Row: [ MOVE ]  [ RENAME ]  [ INFO ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CommanderActionButton(
                            label = "MOVE",
                            icon = Icons.Default.DriveFileMove,
                            isActive = isMovePickerExpanded,
                            onClick = { isMovePickerExpanded = !isMovePickerExpanded },
                            modifier = Modifier.weight(1f),
                            testTag = "inspector_move_button"
                        )

                        CommanderActionButton(
                            label = "RENAME",
                            icon = Icons.Default.Edit,
                            isActive = false,
                            onClick = { onRenameClick(file) },
                            modifier = Modifier.weight(1f),
                            testTag = "inspector_rename_button"
                        )

                        CommanderActionButton(
                            label = "INFO",
                            icon = Icons.Default.Info,
                            isActive = isTechnicalInfoExpanded,
                            onClick = { isTechnicalInfoExpanded = !isTechnicalInfoExpanded },
                            modifier = Modifier.weight(1f),
                            testTag = "inspector_info_button"
                        )
                    }

                    // Expanded Move Destination Folder Picker
                    AnimatedVisibility(visible = isMovePickerExpanded) {
                        FolderDestinationPicker(
                            folders = folders,
                            currentFolderId = file.folderId,
                            onSelectFolder = { targetFolderId ->
                                onMoveToFolder(file.id, targetFolderId)
                                isMovePickerExpanded = false
                            }
                        )
                    }

                    // Expanded Technical Info Stack
                    AnimatedVisibility(visible = isTechnicalInfoExpanded) {
                        TechnicalMetadataBlock(file = file)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // [ DELETE ] Button
                    Button(
                        onClick = {
                            onInstantDelete(file.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElyzarethAlertRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("instant_delete_button")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DELETE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                MediaType.IMAGE -> {
                    // ──────────────────────────────────────────
                    // PHOTO PREVIEW SURFACE
                    // ──────────────────────────────────────────

                    // Large Photo Display
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .border(1.dp, ElyzarethBorder, RoundedCornerShape(8.dp))
                            .clickable { onOpenPhotoPreview() }
                    ) {
                        AsyncImage(
                            model = if (file.filePath.isNotBlank()) File(file.filePath) else null,
                            contentDescription = file.fullName,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(8.dp)
                                .background(Color(0xDD000000), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = null,
                                tint = ElyzarethPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "FULLSCREEN",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = ElyzarethPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Filename: IMG_3821.jpg
                    Text(
                        text = file.fullName,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = ElyzarethTextPrimary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // 4032 × 3024 • 3.8 MB
                    Text(
                        text = "${file.dimensions ?: "High Resolution"} • ${file.formattedSize}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = ElyzarethPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Breadcrumb: DCIM / Camera
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(ElyzarethSurfaceVariant)
                            .border(0.5.dp, ElyzarethBorder, RoundedCornerShape(6.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(folderColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${file.folderName} / Camera",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = ElyzarethTextSecondary
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Command Row: [ MOVE ]  [ INFO ]  [ DELETE ]
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CommanderActionButton(
                            label = "MOVE",
                            icon = Icons.Default.DriveFileMove,
                            isActive = isMovePickerExpanded,
                            onClick = { isMovePickerExpanded = !isMovePickerExpanded },
                            modifier = Modifier.weight(1f),
                            testTag = "inspector_move_button"
                        )

                        CommanderActionButton(
                            label = "INFO",
                            icon = Icons.Default.Info,
                            isActive = isTechnicalInfoExpanded,
                            onClick = { isTechnicalInfoExpanded = !isTechnicalInfoExpanded },
                            modifier = Modifier.weight(1f),
                            testTag = "inspector_info_button"
                        )

                        Button(
                            onClick = {
                                onInstantDelete(file.id)
                                onDismiss()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElyzarethAlertRed,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("instant_delete_button")
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("DELETE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    // Destination Folder Picker
                    AnimatedVisibility(visible = isMovePickerExpanded) {
                        FolderDestinationPicker(
                            folders = folders,
                            currentFolderId = file.folderId,
                            onSelectFolder = { targetFolderId ->
                                onMoveToFolder(file.id, targetFolderId)
                                isMovePickerExpanded = false
                            }
                        )
                    }

                    // Expanded Technical Info
                    AnimatedVisibility(visible = isTechnicalInfoExpanded) {
                        TechnicalMetadataBlock(file = file)
                    }
                }

                else -> {
                    // Documents and other assets
                    Text(
                        text = file.fullName,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = ElyzarethTextPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${file.extension.uppercase()} • ${file.formattedSize}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = ElyzarethPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CommanderActionButton(
                            label = "MOVE",
                            icon = Icons.Default.DriveFileMove,
                            isActive = isMovePickerExpanded,
                            onClick = { isMovePickerExpanded = !isMovePickerExpanded },
                            modifier = Modifier.weight(1f),
                            testTag = "inspector_move_button"
                        )

                        CommanderActionButton(
                            label = "RENAME",
                            icon = Icons.Default.Edit,
                            isActive = false,
                            onClick = { onRenameClick(file) },
                            modifier = Modifier.weight(1f),
                            testTag = "inspector_rename_button"
                        )
                    }

                    AnimatedVisibility(visible = isMovePickerExpanded) {
                        FolderDestinationPicker(
                            folders = folders,
                            currentFolderId = file.folderId,
                            onSelectFolder = { targetFolderId ->
                                onMoveToFolder(file.id, targetFolderId)
                                isMovePickerExpanded = false
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            onInstantDelete(file.id)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElyzarethAlertRed,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("instant_delete_button")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("DELETE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun MetadataPill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(ElyzarethSurfaceElevated)
            .border(0.5.dp, ElyzarethBorder, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = ElyzarethTextSecondary
        )
    }
}

@Composable
private fun CommanderActionButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isActive) ElyzarethPrimary.copy(alpha = 0.18f) else ElyzarethSurfaceElevated,
            contentColor = if (isActive) ElyzarethPrimary else ElyzarethTextPrimary
        ),
        modifier = modifier
            .height(42.dp)
            .testTag(testTag)
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(15.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FolderDestinationPicker(
    folders: List<FolderModel>,
    currentFolderId: Long,
    onSelectFolder: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ElyzarethSurfaceVariant)
            .border(0.5.dp, ElyzarethBorder, RoundedCornerShape(8.dp))
            .padding(10.dp)
    ) {
        Text(
            text = "SELECT DESTINATION FOLDER:",
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = ElyzarethTextMuted,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            folders.forEach { f ->
                val isCurrent = f.id == currentFolderId
                val color = parseColor(f.colorHex)
                val purpose = getPurposeForColor(f.colorHex)
                val folderLabel = purpose?.let { "${it.emoji} ${f.name}" } ?: f.name
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isCurrent) color.copy(alpha = 0.25f) else ElyzarethSurfaceElevated)
                        .border(
                            width = if (isCurrent) 1.5.dp else 1.dp,
                            color = if (isCurrent) color else ElyzarethBorder,
                            shape = RoundedCornerShape(6.dp)
                        )
                        .clickable {
                            if (!isCurrent) {
                                onSelectFolder(f.id)
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("move_to_folder_${f.id}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(color)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = folderLabel,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrent) ElyzarethTextPrimary else ElyzarethTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun TechnicalMetadataBlock(file: FileItem) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(ElyzarethSurfaceVariant)
            .border(0.5.dp, ElyzarethBorder, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        MetadataTagRow("Path", file.filePath)
        MetadataTagRow("Mime", file.mimeType)
        MetadataTagRow("Modified", file.formattedDate)
        if (file.dimensions != null) {
            MetadataTagRow("Resolution", file.dimensions ?: "")
        }
        if (file.durationMs != null && file.durationMs > 0) {
            MetadataTagRow("Duration Ms", "${file.durationMs} ms")
        }
    }
}

@Composable
private fun MetadataTagRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = ElyzarethTextMuted
        )
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = ElyzarethTextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
