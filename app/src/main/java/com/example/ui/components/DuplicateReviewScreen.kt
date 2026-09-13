package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.example.util.DuplicateGroup
import com.example.util.DuplicateMatchType
import com.example.util.KeepBestRule
import java.io.File
import java.util.Locale

@Composable
fun DuplicateReviewScreen(
    duplicateGroups: List<DuplicateGroup>,
    selectedGroup: DuplicateGroup?,
    currentRule: KeepBestRule,
    playingFileId: Long?,
    isPlaying: Boolean,
    onTogglePlayAudio: (FileItem) -> Unit,
    onPreviewPhoto: (FileItem) -> Unit,
    onSelectRule: (KeepBestRule) -> Unit,
    onToggleKeepFile: (groupId: String, fileId: Long) -> Unit,
    onSelectGroup: (DuplicateGroup) -> Unit,
    onBackFromGroup: () -> Unit,
    onScanLibrary: () -> Unit,
    onDeleteGroupDuplicates: (DuplicateGroup) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRuleMenu by remember { mutableStateOf(false) }

    val totalGroups = duplicateGroups.size
    val totalRedundantFiles = duplicateGroups.sumOf { it.items.size - 1 }.coerceAtLeast(0)
    val totalReclaimableBytes = duplicateGroups.sumOf { group ->
        val toDelete = group.items.filter { it.id in group.userSelectedDeleteIds }
        toDelete.sumOf { it.sizeBytes }
    }
    val reclaimableMb = String.format(Locale.US, "%.1f MB", totalReclaimableBytes / (1024.0 * 1024.0))

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElyzarethBackground)
            .testTag("cleanup_screen")
    ) {
        // Top Header Banner: CLEANUP & TOTAL POSSIBLE DUPLICATE GROUPS
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElyzarethSurface)
                .border(0.5.dp, ElyzarethBorder)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "CLEANUP",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        letterSpacing = 1.sp,
                        color = ElyzarethTextPrimary
                    )
                    Text(
                        text = "$totalGroups POSSIBLE DUPLICATE GROUPS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = ElyzarethAccent
                    )
                }

                // Scan Library button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElyzarethSurfaceElevated)
                        .border(0.5.dp, ElyzarethBorder, RoundedCornerShape(6.dp))
                        .clickable { onScanLibrary() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("scan_library_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Scan",
                            tint = ElyzarethPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "RESCAN",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElyzarethPrimary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Keep Best Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box {
                    OutlinedButton(
                        onClick = { showRuleMenu = true },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = ElyzarethSurfaceElevated,
                            contentColor = ElyzarethPrimary
                        ),
                        modifier = Modifier.testTag("rule_selector_button")
                    ) {
                        Text(
                            text = "KEEP BEST: [ ${currentRule.displayName} ▾ ]",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    DropdownMenu(
                        expanded = showRuleMenu,
                        onDismissRequest = { showRuleMenu = false },
                        modifier = Modifier
                            .background(ElyzarethSurface)
                            .border(1.dp, ElyzarethBorder)
                    ) {
                        KeepBestRule.entries.forEach { rule ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = rule.displayName,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        fontWeight = if (rule == currentRule) FontWeight.Bold else FontWeight.Normal,
                                        color = if (rule == currentRule) ElyzarethPrimary else ElyzarethTextPrimary
                                    )
                                },
                                onClick = {
                                    onSelectRule(rule)
                                    showRuleMenu = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "Reclaim: $reclaimableMb",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = ElyzarethTextMuted
                )
            }
        }

        // Duplicate Groups Comparison Workspace
        if (duplicateGroups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = ElyzarethAccent,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "LIBRARY IS CLEAN",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ElyzarethTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Zero redundant copies detected",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = ElyzarethTextMuted
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(duplicateGroups, key = { it.id }) { group ->
                    CleanupDecisionGroupCard(
                        group = group,
                        isPlaying = isPlaying,
                        playingFileId = playingFileId,
                        onTogglePlayAudio = onTogglePlayAudio,
                        onPreviewPhoto = onPreviewPhoto,
                        onToggleKeepFile = { fileId -> onToggleKeepFile(group.id, fileId) },
                        onDeleteGroup = { onDeleteGroupDuplicates(group) }
                    )
                }
            }
        }
    }
}

/**
 * Individual Decision Group Card (e.g. Deep Roots with stacked candidate copies)
 */
@Composable
private fun CleanupDecisionGroupCard(
    group: DuplicateGroup,
    isPlaying: Boolean,
    playingFileId: Long?,
    onTogglePlayAudio: (FileItem) -> Unit,
    onPreviewPhoto: (FileItem) -> Unit,
    onToggleKeepFile: (Long) -> Unit,
    onDeleteGroup: () -> Unit
) {
    val markedDeleteCount = group.userSelectedDeleteIds.size
    val groupReclaimBytes = group.items
        .filter { it.id in group.userSelectedDeleteIds }
        .sumOf { it.sizeBytes }
    val groupReclaimMb = String.format(Locale.US, "%.1f MB", groupReclaimBytes / (1024.0 * 1024.0))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("cleanup_group_${group.id}")
    ) {
        // Group Title (Filename / Group name)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = group.displayName,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ElyzarethTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Reclaim badge
            Text(
                text = "${group.items.size} copies • $groupReclaimMb",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = ElyzarethTextMuted
            )
        }

        // Evidence & Match Type Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (group.matchType == DuplicateMatchType.EXACT_HASH) ElyzarethPrimary.copy(alpha = 0.18f)
                        else ElyzarethSurfaceElevated
                    )
                    .border(
                        0.5.dp,
                        if (group.matchType == DuplicateMatchType.EXACT_HASH) ElyzarethPrimary else ElyzarethBorder,
                        RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = group.matchType.levelTag,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (group.matchType == DuplicateMatchType.EXACT_HASH) ElyzarethPrimary else ElyzarethAccent
                )
            }
            if (group.evidence.isNotBlank()) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = group.evidence,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = ElyzarethTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Comparison Container (Stacked candidates with divider)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(ElyzarethSurface)
                .border(1.dp, ElyzarethBorder, RoundedCornerShape(8.dp))
        ) {
            group.items.forEachIndexed { index, item ->
                val isMarkedForDelete = item.id in group.userSelectedDeleteIds
                val isKeep = !isMarkedForDelete

                if (group.mediaCategory == MediaType.IMAGE) {
                    PhotoComparisonRow(
                        file = item,
                        isKeep = isKeep,
                        onToggle = { onToggleKeepFile(item.id) },
                        onTapThumbnail = { onPreviewPhoto(item) }
                    )
                } else {
                    AudioComparisonRow(
                        file = item,
                        isKeep = isKeep,
                        isPlaying = isPlaying && playingFileId == item.id,
                        onTogglePlay = { onTogglePlayAudio(item) },
                        onToggle = { onToggleKeepFile(item.id) }
                    )
                }

                if (index < group.items.size - 1) {
                    HorizontalDivider(color = ElyzarethBorder, thickness = 0.5.dp)
                }
            }
        }

        // Action under group if duplicates are marked
        if (markedDeleteCount > 0) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElyzarethAlertRed.copy(alpha = 0.15f))
                        .border(1.dp, ElyzarethAlertRed.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                        .clickable { onDeleteGroup() }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .testTag("delete_group_button_${group.id}")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = ElyzarethAlertRed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "DELETE FLAGGED ($markedDeleteCount) • FREE $groupReclaimMb",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElyzarethAlertRed
                        )
                    }
                }
            }
        }
    }
}

/**
 * Audio Candidate Row: ▶ Deep Roots.mp3 | 320 kbps • 8.4 MB | Music/Selected | KEEP ✓ / DELETE
 */
@Composable
private fun AudioComparisonRow(
    file: FileItem,
    isKeep: Boolean,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onToggle: () -> Unit
) {
    val folderColor = parseColor(file.folderColorHex)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Inline Play/Pause Preview Button
        IconButton(
            onClick = onTogglePlay,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (isPlaying) ElyzarethPrimary else ElyzarethSurfaceElevated)
                .border(0.5.dp, ElyzarethBorder, CircleShape)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = if (isPlaying) Color.White else ElyzarethPrimary,
                modifier = Modifier.size(17.dp)
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // File Details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.fullName,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ElyzarethTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${file.bitrateKbps ?: 320} kbps • ${file.formattedSize}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.5.sp,
                color = ElyzarethTextSecondary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(folderColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "${file.folderName}/${file.name}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = ElyzarethTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // KEEP ✓ or DELETE badge
        ComparisonDecisionBadge(isKeep = isKeep, onToggle = onToggle)
    }
}

/**
 * Photo Candidate Row with Actual Thumbnail
 */
@Composable
private fun PhotoComparisonRow(
    file: FileItem,
    isKeep: Boolean,
    onToggle: () -> Unit,
    onTapThumbnail: () -> Unit
) {
    val folderColor = parseColor(file.folderColorHex)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Actual photo thumbnail
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black)
                .border(0.5.dp, ElyzarethBorder, RoundedCornerShape(6.dp))
                .clickable { onTapThumbnail() }
        ) {
            AsyncImage(
                model = if (file.filePath.isNotBlank()) File(file.filePath) else null,
                contentDescription = file.fullName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        // File details
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = file.fullName,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = ElyzarethTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "${file.dimensions ?: ""} • ${file.formattedSize}",
                fontFamily = FontFamily.Monospace,
                fontSize = 11.5.sp,
                color = ElyzarethTextSecondary
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(folderColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "${file.folderName}/Camera",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = ElyzarethTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        ComparisonDecisionBadge(isKeep = isKeep, onToggle = onToggle)
    }
}

/**
 * KEEP ✓ vs DELETE Decision Badge
 */
@Composable
private fun ComparisonDecisionBadge(
    isKeep: Boolean,
    onToggle: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(
                if (isKeep) ElyzarethAccent.copy(alpha = 0.15f)
                else ElyzarethAlertRed.copy(alpha = 0.15f)
            )
            .border(
                width = 1.dp,
                color = if (isKeep) ElyzarethAccent else ElyzarethAlertRed,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onToggle() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = if (isKeep) "KEEP ✓" else "DELETE",
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = if (isKeep) ElyzarethAccent else ElyzarethAlertRed
        )
    }
}
