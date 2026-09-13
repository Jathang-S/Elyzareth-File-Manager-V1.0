package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AddFileDialog
import com.example.ui.components.CreateFolderDialog
import com.example.ui.components.DockedMiniPlayer
import com.example.ui.components.DuplicateReviewScreen
import com.example.ui.components.ElyzarethFileListView
import com.example.ui.components.ElyzarethHeader
import com.example.ui.components.FolderColorDialog
import com.example.ui.components.FolderColorStrip
import com.example.ui.components.FoldersScreen
import com.example.ui.components.InventoryExportDialog
import com.example.ui.components.PhotoPreviewDialog
import com.example.ui.components.RenameFileDialog
import com.example.ui.components.SearchBarAndFilterChips
import com.example.ui.components.SingleClickFileInfoSheet
import com.example.ui.theme.ElyzarethAccent
import com.example.ui.theme.ElyzarethBackground
import com.example.ui.theme.ElyzarethBorder
import com.example.ui.theme.ElyzarethPrimary
import com.example.ui.theme.ElyzarethSurface
import com.example.ui.theme.ElyzarethSurfaceElevated
import com.example.ui.theme.ElyzarethTextMuted
import com.example.ui.theme.ElyzarethTextPrimary
import com.example.ui.theme.ElyzarethTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileManagerScreen(
    viewModel: FileManagerViewModel,
    modifier: Modifier = Modifier
) {
    val composeHaptic = LocalHapticFeedback.current
    val folders by viewModel.folders.collectAsStateWithLifecycle()
    val displayFiles by viewModel.displayFiles.collectAsStateWithLifecycle()
    val selectedFolderId by viewModel.selectedFolderId.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedMediaType by viewModel.selectedMediaType.collectAsStateWithLifecycle()
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()
    val appMode by viewModel.appMode.collectAsStateWithLifecycle()
    val selectedFileForInfo by viewModel.selectedFileForInfo.collectAsStateWithLifecycle()
    val folderColorDialogFolder by viewModel.folderColorDialogFolder.collectAsStateWithLifecycle()
    val isCreateFolderDialogOpen by viewModel.isCreateFolderDialogOpen.collectAsStateWithLifecycle()
    val isAddFileDialogOpen by viewModel.isAddFileDialogOpen.collectAsStateWithLifecycle()
    val isExportDialogOpen by viewModel.isExportDialogOpen.collectAsStateWithLifecycle()
    val isVisualizerActive by viewModel.isVisualizerActive.collectAsStateWithLifecycle()
    val playbackState by viewModel.playbackState.collectAsStateWithLifecycle()
    val duplicateGroups by viewModel.duplicateGroups.collectAsStateWithLifecycle()
    val duplicateRule by viewModel.duplicateRule.collectAsStateWithLifecycle()
    val selectedDuplicateGroup by viewModel.selectedDuplicateGroup.collectAsStateWithLifecycle()
    val renameFileDialogFile by viewModel.renameFileDialogFile.collectAsStateWithLifecycle()
    val selectedPhotoForPreview by viewModel.selectedPhotoForPreview.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is FileManagerUiEvent.ShowSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = event.message,
                        actionLabel = event.actionLabel,
                        duration = SnackbarDuration.Short
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        event.onAction?.invoke()
                    }
                }
            }
        }
    }

    val totalSizeBytes = remember(displayFiles) {
        displayFiles.sumOf { it.sizeBytes }
    }

    val activeAudioFile = displayFiles.find { it.id == playbackState.currentFileId }
        ?: duplicateGroups.flatMap { it.items }.find { it.id == playbackState.currentFileId }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("file_manager_screen"),
        containerColor = ElyzarethBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("snackbar_host")
            )
        },
        bottomBar = {
            // Persistent Player Above Bottom Navigation Bar
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                // Persistent Player (whenever audio is playing or paused with position)
                if (activeAudioFile != null && (playbackState.isPlaying || playbackState.currentPositionMs > 0)) {
                    DockedMiniPlayer(
                        file = activeAudioFile,
                        isPlaying = playbackState.isPlaying,
                        currentPositionMs = playbackState.currentPositionMs,
                        durationMs = playbackState.durationMs,
                        onTogglePlay = { viewModel.togglePlayAudio(activeAudioFile) },
                        onSeekTo = { pos -> viewModel.seekAudio(pos) },
                        onStop = { viewModel.audioPlayerManager.stop() },
                        onOpenInfo = { viewModel.onFileClicked(activeAudioFile) }
                    )
                }

                // Four-Surface Bottom Navigation Bar: LIBRARY | CLEANUP | FOLDERS | EXPORT
                BottomCommanderNavBar(
                    currentMode = appMode,
                    duplicateBadgeCount = duplicateGroups.sumOf { it.items.size - 1 }.takeIf { it > 0 },
                    onSelectMode = { mode ->
                        viewModel.hapticHelper.performClick(composeHaptic)
                        viewModel.setAppMode(mode)
                    },
                    onExportClick = {
                        viewModel.hapticHelper.performClick(composeHaptic)
                        viewModel.openExportDialog()
                    }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ElyzarethBackground)
        ) {
            // Elyzareth Top Header Bar: ELY FILES + Visualizer + Actions
            ElyzarethHeader(
                totalFilesCount = displayFiles.size,
                totalSizeBytes = totalSizeBytes,
                isPlaying = playbackState.isPlaying,
                currentSortMode = sortMode,
                onSortModeChange = { mode ->
                    viewModel.hapticHelper.performClick(composeHaptic)
                    viewModel.setSortMode(mode)
                },
                onExportClick = {
                    viewModel.openExportDialog()
                },
                onAddFileClick = {
                    viewModel.hapticHelper.performClick(composeHaptic)
                    viewModel.openAddFileDialog()
                }
            )

            // Primary Surface Selection
            when (appMode) {
                AppMode.LIBRARY -> {
                    // Surface 1: HOME / LIBRARY
                    Column(modifier = Modifier.weight(1f)) {
                        // Search Bar: 🔍 Search files... and Filter Chips: ALL  🎵 AUDIO  🖼 PHOTO
                        SearchBarAndFilterChips(
                            searchQuery = searchQuery,
                            onSearchQueryChange = { query ->
                                viewModel.setSearchQuery(query)
                            },
                            selectedMediaType = selectedMediaType,
                            onSelectMediaType = { type ->
                                viewModel.hapticHelper.performClick(composeHaptic)
                                viewModel.setMediaTypeFilter(type)
                            }
                        )

                        // Purposeful Folder Color Bar (Color belongs to the folder!)
                        FolderColorStrip(
                            folders = folders,
                            selectedFolderId = selectedFolderId,
                            onSelectFolder = { folderId ->
                                viewModel.hapticHelper.performClick(composeHaptic)
                                viewModel.selectFolder(folderId)
                            },
                            onOpenFolderColor = { folder ->
                                viewModel.hapticHelper.performClick(composeHaptic)
                                viewModel.openFolderColorPicker(folder)
                            },
                            onCreateFolderClick = {
                                viewModel.hapticHelper.performClick(composeHaptic)
                                viewModel.openCreateFolderDialog()
                            }
                        )

                        // File list where filename is the hero
                        ElyzarethFileListView(
                            files = displayFiles,
                            selectedFileId = selectedFileForInfo?.id,
                            onFileClick = { file ->
                                viewModel.hapticHelper.performClick(composeHaptic)
                                viewModel.onFileClicked(file)
                            },
                            onInstantDelete = { fileId ->
                                viewModel.hapticHelper.performDelete(composeHaptic)
                                viewModel.deleteFileInstant(fileId)
                            },
                            onReorder = { fromIndex, toIndex ->
                                viewModel.reorderFiles(fromIndex, toIndex)
                            },
                            onAddFileClick = {
                                viewModel.hapticHelper.performClick(composeHaptic)
                                viewModel.openAddFileDialog()
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                AppMode.CLEANUP -> {
                    // Surface 2: CLEANUP Overview & Surface 3: DUPLICATE REVIEW
                    DuplicateReviewScreen(
                        duplicateGroups = duplicateGroups,
                        selectedGroup = selectedDuplicateGroup,
                        currentRule = duplicateRule,
                        playingFileId = playbackState.currentFileId,
                        isPlaying = playbackState.isPlaying,
                        onTogglePlayAudio = { file -> viewModel.togglePlayAudio(file) },
                        onPreviewPhoto = { photo -> viewModel.openPhotoPreview(photo) },
                        onSelectRule = { rule -> viewModel.setDuplicateRule(rule) },
                        onToggleKeepFile = { groupId, fileId -> viewModel.toggleKeepFileInGroup(groupId, fileId) },
                        onSelectGroup = { group -> viewModel.selectDuplicateGroup(group) },
                        onBackFromGroup = { viewModel.clearSelectedDuplicateGroup() },
                        onScanLibrary = { viewModel.scanLibraryDuplicates() },
                        onDeleteGroupDuplicates = { group -> viewModel.deleteGroupDuplicates(group) },
                        modifier = Modifier.weight(1f)
                    )
                }

                AppMode.FOLDERS -> {
                    // Surface: FOLDERS Command
                    FoldersScreen(
                        folders = folders,
                        allFiles = displayFiles,
                        selectedFolderId = selectedFolderId,
                        onSelectFolderAndOpenLibrary = { folderId ->
                            viewModel.selectFolder(folderId)
                            viewModel.setAppMode(AppMode.LIBRARY)
                        },
                        onOpenFolderColor = { folder ->
                            viewModel.openFolderColorPicker(folder)
                        },
                        onCreateFolderClick = {
                            viewModel.openCreateFolderDialog()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Surface 4: Single-Click File Info Inspector & Actions Sheet
        selectedFileForInfo?.let { file ->
            val isCurrentAudioPlaying = playbackState.currentFileId == file.id && playbackState.isPlaying
            val currentPos = if (playbackState.currentFileId == file.id) playbackState.currentPositionMs else 0L
            val currentDur = if (playbackState.currentFileId == file.id) playbackState.durationMs else (file.durationMs ?: 0L)

            SingleClickFileInfoSheet(
                file = file,
                folders = folders,
                isPlaying = isCurrentAudioPlaying,
                currentPlaybackPositionMs = currentPos,
                audioDurationMs = currentDur,
                onTogglePlayAudio = { viewModel.togglePlayAudio(file) },
                onSeekAudio = { pos -> viewModel.seekAudio(pos) },
                onOpenPhotoPreview = { viewModel.openPhotoPreview(file) },
                onDismiss = {
                    viewModel.hapticHelper.performClick(composeHaptic)
                    viewModel.closeFileInfo()
                },
                onInstantDelete = { fileId ->
                    viewModel.hapticHelper.performDelete(composeHaptic)
                    viewModel.deleteFileInstant(fileId)
                },
                onRenameClick = { targetFile ->
                    viewModel.openRenameFileDialog(targetFile)
                },
                onMoveToFolder = { fileId, targetFolderId ->
                    viewModel.hapticHelper.performDrop(composeHaptic)
                    viewModel.moveFileToFolder(fileId, targetFolderId)
                }
            )
        }

        // Photo Preview Full-screen Dialog
        selectedPhotoForPreview?.let { photo ->
            PhotoPreviewDialog(
                photo = photo,
                allPhotos = displayFiles.filter { it.mediaType == com.example.model.MediaType.IMAGE },
                onDismiss = { viewModel.closePhotoPreview() },
                onNavigateNext = { viewModel.navigatePhoto(forward = true) },
                onNavigatePrevious = { viewModel.navigatePhoto(forward = false) }
            )
        }

        // Rename File Dialog
        renameFileDialogFile?.let { file ->
            RenameFileDialog(
                file = file,
                onDismiss = { viewModel.closeRenameFileDialog() },
                onRename = { fileId, newName ->
                    viewModel.renameFile(fileId, newName)
                }
            )
        }

        // Inventory Export Dialog (Scope, Include Fields, Format)
        if (isExportDialogOpen) {
            InventoryExportDialog(
                folders = folders,
                currentFolderId = selectedFolderId,
                onDismiss = { viewModel.closeExportDialog() },
                onExport = { scope, targetFolderId, includeName, includeType, includeLocation, format ->
                    viewModel.exportInventoryDetailed(
                        scope = scope,
                        targetFolderId = targetFolderId,
                        includeName = includeName,
                        includeType = includeType,
                        includeLocation = includeLocation,
                        format = format
                    )
                }
            )
        }

        // Custom Folder Coloring Dialog
        folderColorDialogFolder?.let { folder ->
            FolderColorDialog(
                folder = folder,
                onDismiss = { viewModel.closeFolderColorPicker() },
                onColorSelected = { newColorHex ->
                    viewModel.hapticHelper.performColorChange(composeHaptic)
                    viewModel.updateFolderColor(folder.id, newColorHex)
                }
            )
        }

        // Create Folder Dialog
        if (isCreateFolderDialogOpen) {
            CreateFolderDialog(
                onDismiss = { viewModel.closeCreateFolderDialog() },
                onCreate = { name, colorHex ->
                    viewModel.hapticHelper.performColorChange(composeHaptic)
                    viewModel.createFolder(name, colorHex)
                }
            )
        }

        // Add File / Media Asset Dialog
        if (isAddFileDialogOpen) {
            AddFileDialog(
                folders = folders,
                currentFolderId = selectedFolderId,
                onDismiss = { viewModel.closeAddFileDialog() },
                onAddFile = { name, ext, folderId, size, mime, type, duration, meta ->
                    viewModel.hapticHelper.performDrop(composeHaptic)
                    viewModel.createFile(name, ext, folderId, size, mime, type, duration, meta)
                }
            )
        }
    }
}

/**
 * Modern Titanium Commander Bottom Navigation Bar
 * Layout: LIBRARY | CLEANUP | FOLDERS | EXPORT
 */
@Composable
private fun BottomCommanderNavBar(
    currentMode: AppMode,
    duplicateBadgeCount: Int?,
    onSelectMode: (AppMode) -> Unit,
    onExportClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(ElyzarethSurface)
            .border(0.5.dp, ElyzarethBorder)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // LIBRARY
        CommanderNavButton(
            label = "LIBRARY",
            testTag = "mode_tab_playlist",
            icon = Icons.Default.LibraryMusic,
            isSelected = currentMode == AppMode.LIBRARY,
            badge = null,
            onClick = { onSelectMode(AppMode.LIBRARY) },
            modifier = Modifier.weight(1f)
        )

        // CLEANUP
        CommanderNavButton(
            label = "CLEANUP",
            testTag = "mode_tab_cleanup",
            icon = Icons.Default.AutoAwesome,
            isSelected = currentMode == AppMode.CLEANUP,
            badge = duplicateBadgeCount?.toString(),
            onClick = { onSelectMode(AppMode.CLEANUP) },
            modifier = Modifier.weight(1f)
        )

        // FOLDERS
        CommanderNavButton(
            label = "FOLDERS",
            testTag = "mode_tab_folders",
            icon = Icons.Default.Folder,
            isSelected = currentMode == AppMode.FOLDERS,
            badge = null,
            onClick = { onSelectMode(AppMode.FOLDERS) },
            modifier = Modifier.weight(1f)
        )

        // EXPORT
        CommanderNavButton(
            label = "EXPORT",
            testTag = "mode_tab_export",
            icon = Icons.Default.FileDownload,
            isSelected = false,
            badge = null,
            onClick = onExportClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun CommanderNavButton(
    label: String,
    testTag: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    badge: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) ElyzarethPrimary.copy(alpha = 0.15f) else ElyzarethSurfaceElevated)
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) ElyzarethPrimary else ElyzarethBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) ElyzarethPrimary else ElyzarethTextSecondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp,
                color = if (isSelected) ElyzarethPrimary else ElyzarethTextSecondary
            )
            if (badge != null) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElyzarethAccent)
                        .padding(horizontal = 5.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = badge,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
