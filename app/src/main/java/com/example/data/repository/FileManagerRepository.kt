package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.dao.FileDao
import com.example.data.local.dao.FolderDao
import com.example.data.local.entity.FileItemEntity
import com.example.data.local.entity.FolderEntity
import com.example.model.FileItem
import com.example.model.FolderModel
import com.example.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext

class FileManagerRepository(
    private val folderDao: FolderDao,
    private val fileDao: FileDao,
    private val fsManager: com.example.util.FileSystemManager? = null
) {
    val foldersFlow: Flow<List<FolderModel>> = combine(
        folderDao.getAllFolders(),
        fileDao.getAllFiles()
    ) { folderEntities, fileEntities ->
        val fileCounts = fileEntities.groupBy { it.folderId }
        folderEntities.map { entity ->
            val filesInFolder = fileCounts[entity.id] ?: emptyList()
            FolderModel(
                id = entity.id,
                name = entity.name,
                colorHex = entity.colorHex,
                iconName = entity.iconName,
                sortOrder = entity.sortOrder,
                fileCount = filesInFolder.size,
                totalSizeBytes = filesInFolder.sumOf { it.sizeBytes }
            )
        }
    }

    val allFilesFlow: Flow<List<FileItem>> = combine(
        fileDao.getAllFiles(),
        folderDao.getAllFolders()
    ) { fileEntities, folderEntities ->
        val folderMap = folderEntities.associateBy { it.id }
        fileEntities.map { entity ->
            val folder = folderMap[entity.folderId]
            mapEntityToFileItem(entity, folder)
        }
    }

    suspend fun checkAndSeed() = withContext(Dispatchers.IO) {
        if (folderDao.getFolderCount() == 0) {
            if (fsManager != null) {
                fsManager.ensurePhysicalSeedFiles()
                AppDatabase.populateFromDisk(folderDao, fileDao, fsManager)
            } else {
                AppDatabase.populateInitialData(folderDao, fileDao)
            }
        } else if (fsManager != null) {
            reconcileWithFilesystem()
        }
    }

    /**
     * Reconciles Room cache with physical filesystem truth:
     * - Discovers new files on disk across nested directories and indexes them.
     * - Removes database records whose physical files no longer exist.
     * - Updates size and hash changes.
     */
    suspend fun reconcileWithFilesystem() = withContext(Dispatchers.IO) {
        val manager = fsManager ?: return@withContext
        val discovered = manager.scanFileSystem()
        val existingFiles = fileDao.getAllFilesSync()
        val existingFolders = folderDao.getAllFoldersSync()
        val folderMap = existingFolders.associateBy { it.name }

        // Remove DB records if physical file was deleted externally
        for (ef in existingFiles) {
            if (ef.filePath.isNotBlank() && !java.io.File(ef.filePath).exists()) {
                fileDao.deleteFileById(ef.id)
            }
        }

        // Insert or update discovered files
        val existingByPath = fileDao.getAllFilesSync().associateBy { it.filePath }
        val toInsert = mutableListOf<FileItemEntity>()

        for (disc in discovered) {
            val existing = existingByPath[disc.file.absolutePath]
            if (existing == null) {
                val matchedFolderId = folderMap[disc.relativeFolder]?.id ?: 1L
                toInsert.add(
                    FileItemEntity(
                        name = disc.name,
                        extension = disc.extension,
                        folderId = matchedFolderId,
                        sizeBytes = disc.sizeBytes,
                        mimeType = when (disc.mediaType) {
                            MediaType.AUDIO -> "audio/${disc.extension}"
                            MediaType.IMAGE -> "image/${disc.extension}"
                            MediaType.VIDEO -> "video/${disc.extension}"
                            else -> "application/octet-stream"
                        },
                        mediaType = disc.mediaType.name,
                        durationMs = disc.durationMs,
                        mediaMeta = disc.mediaMeta,
                        filePath = disc.file.absolutePath,
                        fileHash = disc.hash,
                        bitrateKbps = disc.bitrateKbps,
                        sampleRateHz = disc.sampleRateHz,
                        dimensions = disc.dimensions,
                        sortOrder = toInsert.size + existingFiles.size
                    )
                )
            }
        }
        if (toInsert.isNotEmpty()) {
            fileDao.insertAll(toInsert)
        }
    }

    suspend fun updateFolderColor(folderId: Long, colorHex: String) = withContext(Dispatchers.IO) {
        folderDao.updateFolderColor(folderId, colorHex)
    }

    suspend fun createFolder(name: String, colorHex: String, iconName: String = "folder"): Long =
        withContext(Dispatchers.IO) {
            val count = folderDao.getFolderCount()
            val id = folderDao.insertFolder(
                FolderEntity(
                    name = name.trim(),
                    colorHex = colorHex,
                    iconName = iconName,
                    sortOrder = count
                )
            )
            // Create physical folder directory
            fsManager?.let { mgr ->
                val dir = java.io.File(mgr.getRootDirectory(), name.trim())
                if (!dir.exists()) dir.mkdirs()
            }
            id
        }

    suspend fun deleteFolder(folderId: Long) = withContext(Dispatchers.IO) {
        folderDao.deleteFolderById(folderId)
    }

    suspend fun createFile(
        name: String,
        extension: String,
        folderId: Long,
        sizeBytes: Long,
        mimeType: String,
        mediaType: MediaType,
        durationMs: Long? = null,
        mediaMeta: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val folder = folderDao.getFolderById(folderId)
        val folderName = folder?.name ?: "Root"

        var physicalPath = ""
        var hash = ""

        if (fsManager != null) {
            val createdFile = if (mediaType == MediaType.AUDIO) {
                fsManager.createPlayableAudioFile(folderName, "$name.$extension", 440.0, 5)
            } else if (mediaType == MediaType.IMAGE) {
                fsManager.createRealImageFile(folderName, "$name.$extension", 800, 600, "vaporwave")
            } else {
                val f = java.io.File(java.io.File(fsManager.getRootDirectory(), folderName), "$name.$extension")
                if (!f.exists()) {
                    f.parentFile?.mkdirs()
                    f.writeText("Winamp File: $name")
                }
                f
            }
            physicalPath = createdFile.absolutePath
        }

        val count = fileDao.getFileCount()
        fileDao.insertFile(
            FileItemEntity(
                name = name.trim(),
                extension = extension.trim().lowercase(),
                folderId = folderId,
                sizeBytes = if (physicalPath.isNotBlank()) java.io.File(physicalPath).length() else sizeBytes,
                mimeType = mimeType,
                mediaType = mediaType.name,
                durationMs = durationMs,
                mediaMeta = mediaMeta,
                filePath = physicalPath,
                fileHash = hash,
                sortOrder = count
            )
        )
    }

    suspend fun moveFile(fileId: Long, targetFolderId: Long) = withContext(Dispatchers.IO) {
        val existing = fileDao.getFileById(fileId) ?: return@withContext
        val targetFolder = folderDao.getFolderById(targetFolderId) ?: return@withContext

        var newPath = existing.filePath
        if (fsManager != null && existing.filePath.isNotBlank()) {
            val srcFile = java.io.File(existing.filePath)
            val moved = fsManager.movePhysicalFile(srcFile, targetFolder.name)
            if (moved != null) {
                newPath = moved.absolutePath
            }
        }

        fileDao.updateFiles(
            listOf(
                existing.copy(
                    folderId = targetFolderId,
                    filePath = newPath
                )
            )
        )
    }

    suspend fun updateFileSortOrders(orderedFiles: List<FileItem>) = withContext(Dispatchers.IO) {
        val updatedEntities = orderedFiles.mapIndexed { index, item ->
            FileItemEntity(
                id = item.id,
                name = item.name,
                extension = item.extension,
                folderId = item.folderId,
                sizeBytes = item.sizeBytes,
                mimeType = item.mimeType,
                mediaType = item.mediaType.name,
                durationMs = item.durationMs,
                mediaMeta = item.mediaMeta,
                filePath = item.filePath,
                fileHash = item.fileHash,
                bitrateKbps = item.bitrateKbps,
                sampleRateHz = item.sampleRateHz,
                dimensions = item.dimensions,
                sortOrder = index,
                createdAt = item.createdAt
            )
        }
        fileDao.updateFiles(updatedEntities)
    }

    suspend fun renameFile(fileId: Long, newNameWithoutExt: String): FileItem? = withContext(Dispatchers.IO) {
        val existing = fileDao.getFileById(fileId) ?: return@withContext null
        val cleanName = newNameWithoutExt.trim()
        if (cleanName.isBlank()) return@withContext null

        var newPath = existing.filePath
        if (fsManager != null && existing.filePath.isNotBlank()) {
            val srcFile = java.io.File(existing.filePath)
            if (srcFile.exists()) {
                val parentDir = srcFile.parentFile
                val destFile = java.io.File(parentDir, "$cleanName.${existing.extension}")
                if (srcFile.renameTo(destFile)) {
                    newPath = destFile.absolutePath
                }
            }
        }

        val updatedEntity = existing.copy(
            name = cleanName,
            filePath = newPath
        )
        fileDao.updateFiles(listOf(updatedEntity))
        val folder = folderDao.getFolderById(updatedEntity.folderId)
        mapEntityToFileItem(updatedEntity, folder)
    }

    suspend fun deleteFile(fileId: Long): FileItem? = withContext(Dispatchers.IO) {
        val existing = fileDao.getFileById(fileId) ?: return@withContext null
        val folder = folderDao.getFolderById(existing.folderId)
        val item = mapEntityToFileItem(existing, folder)

        // Stage physical file in .trash/ for instant 5-second undo support
        var stagedTrashPath = ""
        if (fsManager != null && existing.filePath.isNotBlank()) {
            val srcFile = java.io.File(existing.filePath)
            val staged = fsManager.stagePhysicalDelete(srcFile)
            if (staged != null) {
                stagedTrashPath = staged.absolutePath
            }
        }

        fileDao.deleteFileById(fileId)
        // Return item with filePath pointing to the staged trash file so restore knows where it is
        item.copy(filePath = if (stagedTrashPath.isNotBlank()) stagedTrashPath else item.filePath)
    }

    suspend fun restoreFile(file: FileItem) = withContext(Dispatchers.IO) {
        // If file was staged in trash, move it back physically
        var restoredPath = file.filePath
        if (fsManager != null && file.filePath.contains(".trash")) {
            val trashFile = java.io.File(file.filePath)
            val folder = folderDao.getFolderById(file.folderId)
            val folderName = folder?.name ?: "Synthwave & Beats"
            val targetDir = java.io.File(fsManager.getRootDirectory(), folderName)
            val targetFile = java.io.File(targetDir, file.fullName)
            if (fsManager.restorePhysicalFile(trashFile, targetFile)) {
                restoredPath = targetFile.absolutePath
            }
        }

        fileDao.insertFile(
            FileItemEntity(
                id = file.id,
                name = file.name,
                extension = file.extension,
                folderId = file.folderId,
                sizeBytes = file.sizeBytes,
                mimeType = file.mimeType,
                mediaType = file.mediaType.name,
                durationMs = file.durationMs,
                mediaMeta = file.mediaMeta,
                filePath = restoredPath,
                fileHash = file.fileHash,
                bitrateKbps = file.bitrateKbps,
                sampleRateHz = file.sampleRateHz,
                dimensions = file.dimensions,
                sortOrder = file.sortOrder,
                createdAt = file.createdAt
            )
        )
    }

    private fun mapEntityToFileItem(entity: FileItemEntity, folder: FolderEntity?): FileItem {
        val mediaType = try {
            MediaType.valueOf(entity.mediaType)
        } catch (_: Exception) {
            MediaType.fromExtension(entity.extension)
        }
        return FileItem(
            id = entity.id,
            name = entity.name,
            extension = entity.extension,
            folderId = entity.folderId,
            folderName = folder?.name ?: "Root",
            folderColorHex = folder?.colorHex ?: "#00FF66",
            sizeBytes = entity.sizeBytes,
            mimeType = entity.mimeType,
            mediaType = mediaType,
            durationMs = entity.durationMs,
            mediaMeta = entity.mediaMeta,
            filePath = entity.filePath,
            fileHash = entity.fileHash,
            bitrateKbps = entity.bitrateKbps,
            sampleRateHz = entity.sampleRateHz,
            dimensions = entity.dimensions,
            sortOrder = entity.sortOrder,
            createdAt = entity.createdAt
        )
    }
}
