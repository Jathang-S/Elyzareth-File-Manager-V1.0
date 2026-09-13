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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.SortMode
import com.example.ui.theme.ElyzarethAccent
import com.example.ui.theme.ElyzarethBorder
import com.example.ui.theme.ElyzarethPrimary
import com.example.ui.theme.ElyzarethSurface
import com.example.ui.theme.ElyzarethSurfaceElevated
import com.example.ui.theme.ElyzarethTextMuted
import com.example.ui.theme.ElyzarethTextPrimary
import com.example.ui.theme.ElyzarethTextSecondary

@Composable
fun ElyzarethHeader(
    totalFilesCount: Int,
    totalSizeBytes: Long,
    isPlaying: Boolean,
    currentSortMode: SortMode,
    onSortModeChange: (SortMode) -> Unit,
    onExportClick: () -> Unit = {},
    onAddFileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var settingsMenuExpanded by remember { mutableStateOf(false) }
    var sortSubMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ElyzarethSurface)
            .border(0.5.dp, ElyzarethBorder)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        // App Title: ELY FILES + Functional Winamp Visualizer + Actions
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "ELY FILES",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    letterSpacing = 1.sp,
                    color = ElyzarethTextPrimary
                )

                Spacer(modifier = Modifier.width(10.dp))

                // Functional spectrum visualization (animates with real playback)
                WinampVisualizer(
                    isPlaying = isPlaying,
                    barCount = 12,
                    modifier = Modifier.clip(RoundedCornerShape(4.dp))
                )
            }

            // Right side: ⇧ EXPORT + +FILE + ⋮
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Compact top action: ⇧ EXPORT
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElyzarethSurfaceElevated)
                        .border(0.5.dp, ElyzarethAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .clickable { onExportClick() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("export_header_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = "Export",
                            tint = ElyzarethAccent,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "⇧ EXPORT",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElyzarethAccent
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Quick +FILE button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(ElyzarethPrimary)
                        .clickable { onAddFileClick() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                        .testTag("add_file_header_button")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add File",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "+FILE",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // ⋮ Overflow Menu
                Box {
                    IconButton(
                        onClick = { settingsMenuExpanded = true },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ElyzarethSurfaceElevated)
                            .border(0.5.dp, ElyzarethBorder, CircleShape)
                            .testTag("header_settings_more")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = ElyzarethTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = settingsMenuExpanded,
                        onDismissRequest = { settingsMenuExpanded = false },
                        modifier = Modifier
                            .background(ElyzarethSurface)
                            .border(1.dp, ElyzarethBorder)
                    ) {
                        // Sort Mode
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Sort,
                                    contentDescription = null,
                                    tint = ElyzarethPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            text = {
                                Text(
                                    text = "Sort: ${currentSortMode.displayName}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = ElyzarethTextPrimary
                                )
                            },
                            onClick = {
                                settingsMenuExpanded = false
                                sortSubMenuExpanded = true
                            }
                        )

                        // Export Inventory option
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.FileUpload,
                                    contentDescription = null,
                                    tint = ElyzarethAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            text = {
                                Text(
                                    text = "Export Inventory...",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp,
                                    color = ElyzarethTextPrimary
                                )
                            },
                            onClick = {
                                settingsMenuExpanded = false
                                onExportClick()
                            }
                        )
                    }

                    // Sort mode sub-menu
                    DropdownMenu(
                        expanded = sortSubMenuExpanded,
                        onDismissRequest = { sortSubMenuExpanded = false },
                        modifier = Modifier
                            .background(ElyzarethSurface)
                            .border(1.dp, ElyzarethBorder)
                    ) {
                        SortMode.entries.forEach { mode ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = mode.displayName,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        fontWeight = if (mode == currentSortMode) FontWeight.Bold else FontWeight.Normal,
                                        color = if (mode == currentSortMode) ElyzarethPrimary else ElyzarethTextPrimary
                                    )
                                },
                                onClick = {
                                    sortSubMenuExpanded = false
                                    onSortModeChange(mode)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
