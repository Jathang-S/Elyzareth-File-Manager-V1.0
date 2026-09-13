package com.example.ui

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.repository.FileManagerRepository
import com.example.model.FileItem
import com.example.model.FolderModel
import com.example.model.MediaType
import com.example.util.AudioPlaybackState
import com.example.util.AudioPlayerManager
import com.example.util.DuplicateGroup
import com.example.util.DuplicateScanner
import com.example.util.FileSystemManager
import com.example.util.HapticHelper
import com.example.util.InventoryExporter
import com.example.util.KeepBestRule
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortMode(val displayName: String) {
    PLAYLIST("Playlist #"),
    NAME("Name A-Z"),
    SIZE("Size (Largest)"),
    DATE("Recent First")
}

enum class AppMode {
    LIBRARY,
    CLEANUP,
    FOLDERS
}

sealed class FileManagerUiEvent {
    data class ShowSnackbar(
        val message: String,
        val actionLabel: String? = null,
        val onAction: (() -> Unit)? = null
    ) : FileManagerUiEvent()
}

class FileManagerViewModel(application: Application) : AndroidViewModel(application) {
    val fsManager: FileSystemManager = FileSystemManager(application)
    val audioPlayerManager: AudioPlayerManager = AudioPlayerManager(application)
    val hapticHelper: HapticHelper = HapticHelper(application)

    private val repository: FileManagerRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = FileManagerRepository(db.folderDao(), db.fileDao(), fsManager)
        viewModelScope.launch {
            repository.checkAndSeed()
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayerManager.release()
    }

    private val _eventFlow = MutableSharedFlow<FileManagerUiEvent>()
    val eventFlow: SharedFlow<FileManagerUiEvent> = _eventFlow.asSharedFlow()

    val folders: StateFlow<List<FolderModel>> = repository.foldersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _rawFiles = repository.allFilesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _appMode = MutableStateFlow(AppMode.LIBRARY)
    val appMode: StateFlow<AppMode> = _appMode

    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    val selectedFolderId: StateFlow<Long?> = _selectedFolderId

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedMediaType = MutableStateFlow<MediaType?>(null)
    val selectedMediaType: StateFlow<MediaType?> = _selectedMediaType

    private val _sortMode = MutableStateFlow(SortMode.PLAYLIST)
    val sortMode: StateFlow<SortMode> = _sortMode

    private val _selectedFileForInfo = MutableStateFlow<FileItem?>(null)
    val selectedFileForInfo: StateFlow<FileItem?> = _selectedFileForInfo

    private val _folderColorDialogFolder = MutableStateFlow<FolderModel?>(null)
    val folderColorDialogFolder: StateFlow<FolderModel?> = _folderColorDialogFolder

    private val _isCreateFolderDialogOpen = MutableStateFlow(false)
    val isCreateFolderDialogOpen: StateFlow<Boolean> = _isCreateFolderDialogOpen

    private val _isAddFileDialogOpen = MutableStateFlow(false)
    val isAddFileDialogOpen: StateFlow<Boolean> = _isAddFileDialogOpen

    private val _isExportDialogOpen = MutableStateFlow(false)
    val isExportDialogOpen: StateFlow<Boolean> = _isExportDialogOpen

    private val _isVisualizerActive = MutableStateFlow(true)
    val isVisualizerActive: StateFlow<Boolean> = _isVisualizerActive

    // Photo Preview modal state
    private val _selectedPhotoForPreview = MutableStateFlow<FileItem?>(null)
    val selectedPhotoForPreview: StateFlow<FileItem?> = _selectedPhotoForPreview

    // Audio Playback state
    val playbackState: StateFlow<AudioPlaybackState> = audioPlayerManager.playbackState

    // Duplicate Management State
    private val _duplicateRule = MutableStateFlow(KeepBestRule.HIGHEST_QUALITY)
    val duplicateRule: StateFlow<KeepBestRule> = _duplicateRule

    private val _duplicateGroups = MutableStateFlow<List<DuplicateGroup>>(emptyList())
    val duplicateGroups: StateFlow<List<DuplicateGroup>> = _duplicateGroups

    // Currently drilled-down duplicate group for surface 3 review
    private val _selectedDuplicateGroup = MutableStateFlow<DuplicateGroup?>(null)
    val selectedDuplicateGroup: StateFlow<DuplicateGroup?> = _selectedDuplicateGroup

    // Rename dialog state
    private val _renameFileDialogFile = MutableStateFlow<FileItem?>(null)
    val renameFileDialogFile: StateFlow<FileItem?> = _renameFileDialogFile

    private var recentlyDeletedFiles = mutableListOf<FileItem>()

    init {
        // Automatically calculate duplicate groups whenever raw files change
        viewModelScope.launch {
            _rawFiles.collect { files ->
                refreshDuplicates(files, _duplicateRule.value)
            }
        }
    }

    private fun refreshDuplicates(files: List<FileItem>, rule: KeepBestRule) {
        _duplicateGroups.value = DuplicateScanner.findDuplicates(files, rule)
    }

    // Combined filtered & sorted files for playlist view
    val displayFiles: StateFlow<List<FileItem>> = combine(
        _rawFiles,
        _selectedFolderId,
        _searchQuery,
        _selectedMediaType,
        _sortMode
    ) { files, folderId, query, mediaType, sort ->
        var list = files

        // Filter by folder
        if (folderId != null) {
            list = list.filter { it.folderId == folderId }
        }

        // Filter by search query
        if (query.isNotBlank()) {
            val q = query.trim().lowercase()
            list = list.filter {
                it.name.lowercase().contains(q) ||
                it.extension.lowercase().contains(q) ||
                it.folderName.lowercase().contains(q) ||
                (it.mediaMeta?.lowercase()?.contains(q) == true)
            }
        }

        // Filter by media type
        if (mediaType != null) {
            list = list.filter { it.mediaType == mediaType }
        }

        // Sort
        when (sort) {
            SortMode.PLAYLIST -> list.sortedBy { it.sortOrder }
            SortMode.NAME -> list.sortedBy { it.name.lowercase() }
            SortMode.SIZE -> list.sortedByDescending { it.sizeBytes }
            SortMode.DATE -> list.sortedByDescending { it.createdAt }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setAppMode(mode: AppMode) {
        hapticHelper.performClick()
        _appMode.value = mode
    }

    fun selectFolder(folderId: Long?) {
        hapticHelper.performClick()
        _selectedFolderId.value = folderId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setMediaTypeFilter(type: MediaType?) {
        hapticHelper.performClick()
        _selectedMediaType.value = if (_selectedMediaType.value == type) null else type
    }

    fun setSortMode(mode: SortMode) {
        hapticHelper.performClick()
        _sortMode.value = mode
    }

    fun toggleVisualizer() {
        hapticHelper.performClick()
        _isVisualizerActive.value = !_isVisualizerActive.value
    }

    // Audio Playback
    fun togglePlayAudio(file: FileItem) {
        hapticHelper.performClick()
        audioPlayerManager.togglePlay(file)
    }

    fun seekAudio(positionMs: Long) {
        audioPlayerManager.seekTo(positionMs)
    }

    // Photo Preview
    fun openPhotoPreview(file: FileItem) {
        hapticHelper.performClick()
        _selectedPhotoForPreview.value = file
    }

    fun closePhotoPreview() {
        _selectedPhotoForPreview.value = null
    }

    fun navigatePhoto(forward: Boolean) {
        val photos = _rawFiles.value.filter { it.mediaType == MediaType.IMAGE }
        if (photos.isEmpty()) return
        val current = _selectedPhotoForPreview.value ?: return
        val idx = photos.indexOfFirst { it.id == current.id }
        if (idx == -1) return

        val newIdx = if (forward) {
            (idx + 1) % photos.size
        } else {
            (idx - 1 + photos.size) % photos.size
        }
        hapticHelper.performClick()
        _selectedPhotoForPreview.value = photos[newIdx]
    }

    // Duplicate actions
    fun setDuplicateRule(rule: KeepBestRule) {
        hapticHelper.performClick()
        _duplicateRule.value = rule
        refreshDuplicates(_rawFiles.value, rule)
    }

    fun toggleKeepFileInGroup(groupId: String, fileId: Long) {
        hapticHelper.performClick()
        _duplicateGroups.value = _duplicateGroups.value.map { group ->
            if (group.id == groupId) {
                val newSelected = group.userSelectedDeleteIds.toMutableSet()
                if (fileId in newSelected) {
                    newSelected.remove(fileId)
                } else {
                    newSelected.add(fileId)
                }
                group.copy(userSelectedDeleteIds = newSelected)
            } else {
                group
            }
        }
    }

    fun deleteMarkedDuplicates() {
        val toDeleteIds = _duplicateGroups.value.flatMap { it.userSelectedDeleteIds }
        if (toDeleteIds.isEmpty()) return

        hapticHelper.performDelete()
        viewModelScope.launch {
            val deletedItems = mutableListOf<FileItem>()
            for (id in toDeleteIds) {
                val deleted = repository.deleteFile(id)
                if (deleted != null) {
                    deletedItems.add(deleted)
                }
            }
            recentlyDeletedFiles = deletedItems
            _eventFlow.emit(
                FileManagerUiEvent.ShowSnackbar(
                    message = "Deleted ${deletedItems.size} duplicates",
                    actionLabel = "UNDO",
                    onAction = { undoBatchDelete() }
                )
            )
        }
    }

    private fun undoBatchDelete() {
        val toRestore = recentlyDeletedFiles.toList()
        if (toRestore.isEmpty()) return
        hapticHelper.performUndo()
        viewModelScope.launch {
            for (item in toRestore) {
                repository.restoreFile(item)
            }
            recentlyDeletedFiles.clear()
            _eventFlow.emit(FileManagerUiEvent.ShowSnackbar("Restored ${toRestore.size} files"))
        }
    }

    fun selectDuplicateGroup(group: DuplicateGroup?) {
        hapticHelper.performClick()
        _selectedDuplicateGroup.value = group
    }

    fun clearSelectedDuplicateGroup() {
        hapticHelper.performClick()
        _selectedDuplicateGroup.value = null
    }

    fun scanLibraryDuplicates() {
        hapticHelper.performClick()
        viewModelScope.launch {
            val files = _rawFiles.value
            _duplicateGroups.value = DuplicateScanner.findDuplicates(files, _duplicateRule.value)
            val totalDuplicates = _duplicateGroups.value.sumOf { it.items.size - 1 }
            _eventFlow.emit(FileManagerUiEvent.ShowSnackbar("Found ${_duplicateGroups.value.size} duplicate groups ($totalDuplicates redundant files)"))
        }
    }

    fun deleteGroupDuplicates(group: DuplicateGroup) {
        val toDeleteIds = group.userSelectedDeleteIds.toList()
        if (toDeleteIds.isEmpty()) return

        hapticHelper.performDelete()
        viewModelScope.launch {
            val deletedItems = mutableListOf<FileItem>()
            for (id in toDeleteIds) {
                val deleted = repository.deleteFile(id)
                if (deleted != null) {
                    deletedItems.add(deleted)
                }
            }
            recentlyDeletedFiles = deletedItems
            _selectedDuplicateGroup.value = null
            _eventFlow.emit(
                FileManagerUiEvent.ShowSnackbar(
                    message = "Deleted ${deletedItems.size} duplicates from ${group.displayName}",
                    actionLabel = "UNDO",
                    onAction = { undoBatchDelete() }
                )
            )
        }
    }

    fun openRenameFileDialog(file: FileItem) {
        hapticHelper.performClick()
        _renameFileDialogFile.value = file
    }

    fun closeRenameFileDialog() {
        _renameFileDialogFile.value = null
    }

    fun renameFile(fileId: Long, newNameWithoutExt: String) {
        viewModelScope.launch {
            val renamed = repository.renameFile(fileId, newNameWithoutExt)
            if (renamed != null) {
                if (_selectedFileForInfo.value?.id == fileId) {
                    _selectedFileForInfo.value = renamed
                }
                _eventFlow.emit(FileManagerUiEvent.ShowSnackbar("Renamed to ${renamed.fullName}"))
            } else {
                _eventFlow.emit(FileManagerUiEvent.ShowSnackbar("Could not rename file"))
            }
            _renameFileDialogFile.value = null
        }
    }

    // Single click on file row: opens inspector
    fun onFileClicked(file: FileItem) {
        hapticHelper.performClick()
        _selectedFileForInfo.value = file
    }

    fun closeFileInfo() {
        _selectedFileForInfo.value = null
    }

    /**
     * Delete file INSTANTLY without confirmation dialog.
     * Triggers double-pulse haptic feedback and offers immediate 5s Undo snackbar.
     */
    fun deleteFileInstant(fileId: Long) {
        hapticHelper.performDelete()
        viewModelScope.launch {
            val deleted = repository.deleteFile(fileId)
            if (deleted != null) {
                recentlyDeletedFiles = mutableListOf(deleted)
                if (_selectedFileForInfo.value?.id == fileId) {
                    _selectedFileForInfo.value = null
                }
                _eventFlow.emit(
                    FileManagerUiEvent.ShowSnackbar(
                        message = "Deleted ${deleted.fullName}",
                        actionLabel = "UNDO",
                        onAction = { undoDelete() }
                    )
                )
            }
        }
    }

    fun undoDelete() {
        val toRestore = recentlyDeletedFiles.firstOrNull() ?: return
        hapticHelper.performUndo()
        viewModelScope.launch {
            repository.restoreFile(toRestore)
            recentlyDeletedFiles.clear()
            _eventFlow.emit(
                FileManagerUiEvent.ShowSnackbar(
                    message = "Restored ${toRestore.fullName}"
                )
            )
        }
    }

    /**
     * Drag-and-drop playlist reordering
     */
    fun reorderFiles(fromIndex: Int, toIndex: Int) {
        val currentList = displayFiles.value.toMutableList()
        if (fromIndex !in currentList.indices || toIndex !in currentList.indices || fromIndex == toIndex) {
            return
        }
        val item = currentList.removeAt(fromIndex)
        currentList.add(toIndex, item)
        hapticHelper.performReorderTick()

        viewModelScope.launch {
            repository.updateFileSortOrders(currentList)
        }
    }

    fun moveFileToFolder(fileId: Long, targetFolderId: Long) {
        hapticHelper.performDrop()
        viewModelScope.launch {
            repository.moveFile(fileId, targetFolderId)
            _selectedFileForInfo.value?.let { current ->
                if (current.id == fileId) {
                    val folder = folders.value.find { it.id == targetFolderId }
                    _selectedFileForInfo.value = current.copy(
                        folderId = targetFolderId,
                        folderName = folder?.name ?: current.folderName,
                        folderColorHex = folder?.colorHex ?: current.folderColorHex
                    )
                }
            }
        }
    }

    // Inventory Export
    fun openExportDialog() {
        hapticHelper.performClick()
        _isExportDialogOpen.value = true
    }

    fun closeExportDialog() {
        _isExportDialogOpen.value = false
    }

    fun exportInventory(format: InventoryExporter.ExportFormat, includeExtended: Boolean) {
        exportInventoryDetailed(
            scope = InventoryExporter.ExportScope.ENTIRE_LIBRARY,
            targetFolderId = null,
            includeName = true,
            includeType = true,
            includeLocation = true,
            format = format
        )
    }

    fun exportInventoryDetailed(
        scope: InventoryExporter.ExportScope,
        targetFolderId: Long?,
        includeName: Boolean,
        includeType: Boolean,
        includeLocation: Boolean,
        format: InventoryExporter.ExportFormat
    ) {
        _isExportDialogOpen.value = false
        hapticHelper.performClick()
        val context = getApplication<Application>()
        viewModelScope.launch {
            val allFiles = _rawFiles.value
            val filesToExport = when (scope) {
                InventoryExporter.ExportScope.ENTIRE_LIBRARY -> allFiles
                InventoryExporter.ExportScope.CURRENT_FOLDER -> {
                    val currentId = _selectedFolderId.value
                    if (currentId != null) {
                        allFiles.filter { it.folderId == currentId }
                    } else {
                        allFiles
                    }
                }
                InventoryExporter.ExportScope.SELECTED_FOLDER -> {
                    if (targetFolderId != null) {
                        allFiles.filter { it.folderId == targetFolderId }
                    } else {
                        allFiles
                    }
                }
            }

            val scopeTitle = when (scope) {
                InventoryExporter.ExportScope.ENTIRE_LIBRARY -> "Entire Library"
                InventoryExporter.ExportScope.CURRENT_FOLDER -> {
                    folders.value.find { it.id == _selectedFolderId.value }?.name ?: "All Files"
                }
                InventoryExporter.ExportScope.SELECTED_FOLDER -> {
                    folders.value.find { it.id == targetFolderId }?.name ?: "Selected"
                }
            }

            val file = InventoryExporter.exportInventoryDetailed(
                context = context,
                files = filesToExport,
                format = format,
                includeName = includeName,
                includeType = includeType,
                includeLocation = includeLocation,
                includeSize = false,
                includeDate = false,
                scopeTitle = scopeTitle
            )
            val shareIntent = InventoryExporter.createShareIntent(context, file)
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            val chooser = Intent.createChooser(shareIntent, "Share File Inventory").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            _eventFlow.emit(FileManagerUiEvent.ShowSnackbar("Exported ${filesToExport.size} files to ${file.name}"))
        }
    }

    fun openFolderColorPicker(folder: FolderModel) {
        hapticHelper.performClick()
        _folderColorDialogFolder.value = folder
    }

    fun closeFolderColorPicker() {
        _folderColorDialogFolder.value = null
    }

    fun updateFolderColor(folderId: Long, colorHex: String) {
        hapticHelper.performColorChange()
        viewModelScope.launch {
            repository.updateFolderColor(folderId, colorHex)
            _folderColorDialogFolder.value = null
        }
    }

    fun openCreateFolderDialog() {
        hapticHelper.performClick()
        _isCreateFolderDialogOpen.value = true
    }

    fun closeCreateFolderDialog() {
        _isCreateFolderDialogOpen.value = false
    }

    fun createFolder(name: String, colorHex: String) {
        if (name.isBlank()) return
        hapticHelper.performColorChange()
        viewModelScope.launch {
            repository.createFolder(name, colorHex)
            _isCreateFolderDialogOpen.value = false
        }
    }

    fun deleteFolder(folderId: Long) {
        hapticHelper.performDelete()
        viewModelScope.launch {
            repository.deleteFolder(folderId)
            if (_selectedFolderId.value == folderId) {
                _selectedFolderId.value = null
            }
        }
    }

    fun openAddFileDialog() {
        hapticHelper.performClick()
        _isAddFileDialogOpen.value = true
    }

    fun closeAddFileDialog() {
        _isAddFileDialogOpen.value = false
    }

    fun createFile(
        name: String,
        extension: String,
        folderId: Long,
        sizeBytes: Long,
        mimeType: String,
        mediaType: MediaType,
        durationMs: Long? = null,
        mediaMeta: String? = null
    ) {
        if (name.isBlank()) return
        hapticHelper.performDrop()
        viewModelScope.launch {
            repository.createFile(
                name = name,
                extension = extension,
                folderId = folderId,
                sizeBytes = sizeBytes,
                mimeType = mimeType,
                mediaType = mediaType,
                durationMs = durationMs,
                mediaMeta = mediaMeta
            )
            _isAddFileDialogOpen.value = false
            _eventFlow.emit(FileManagerUiEvent.ShowSnackbar("Added $name.$extension to playlist"))
        }
    }
}
