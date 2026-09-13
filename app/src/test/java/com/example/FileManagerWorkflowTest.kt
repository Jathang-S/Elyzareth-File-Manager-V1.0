package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.dao.FileDao
import com.example.data.local.dao.FolderDao
import com.example.data.repository.FileManagerRepository
import com.example.model.FileItem
import com.example.model.MediaType
import com.example.util.DuplicateMatchType
import com.example.util.DuplicateScanner
import com.example.util.FileSystemManager
import com.example.util.InventoryExporter
import com.example.util.KeepBestRule
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FileManagerWorkflowTest {

    private lateinit var context: Context
    private lateinit var database: AppDatabase
    private lateinit var folderDao: FolderDao
    private lateinit var fileDao: FileDao
    private lateinit var fsManager: FileSystemManager
    private lateinit var repository: FileManagerRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        folderDao = database.folderDao()
        fileDao = database.fileDao()
        fsManager = FileSystemManager(context)
        repository = FileManagerRepository(folderDao, fileDao, fsManager)
    }

    @After
    fun teardown() {
        database.close()
        // Clean up test directories
        File(context.filesDir, "media_library").deleteRecursively()
    }

    @Test
    fun testPhysicalFileOperationsAndStagedUndo() = runBlocking {
        // 1. Create Folder
        val folderId = repository.createFolder("Physical Test", "#00FF66")

        // 2. Create physical file on disk through repository
        val fileId = repository.createFile(
            name = "real_track",
            extension = "mp3",
            folderId = folderId,
            sizeBytes = 2048,
            mimeType = "audio/mpeg",
            mediaType = MediaType.AUDIO,
            durationMs = 120000,
            mediaMeta = "320 kbps • 44.1 kHz"
        )

        val files = repository.allFilesFlow.first()
        val createdFile = files.find { it.id == fileId }
        assertNotNull(createdFile)
        val physicalFile = File(createdFile!!.filePath)
        assertTrue("Physical file must exist on disk", physicalFile.exists())

        // 3. Move File to another folder
        val destFolderId = repository.createFolder("Physical Dest", "#FFB000")
        repository.moveFile(fileId, destFolderId)

        val movedFiles = repository.allFilesFlow.first()
        val movedItem = movedFiles.find { it.id == fileId }
        assertNotNull(movedItem)
        assertEquals(destFolderId, movedItem?.folderId)
        val movedPhysicalFile = File(movedItem!!.filePath)
        assertTrue("Moved physical file must exist in new destination folder", movedPhysicalFile.exists())
        assertEquals("Physical Dest", movedPhysicalFile.parentFile?.name)

        // 4. Delete File (Staged in .trash for 5-second Undo)
        val deletedItem = repository.deleteFile(fileId)
        assertNotNull(deletedItem)
        assertFalse("Original location file must not exist after delete", movedPhysicalFile.exists())

        val trashFile = File(deletedItem!!.filePath)
        assertTrue("Deleted file must be staged in .trash directory", trashFile.exists() && trashFile.parentFile?.name == ".trash")

        val filesAfterDelete = repository.allFilesFlow.first()
        assertTrue(filesAfterDelete.none { it.id == fileId })

        // 5. Undo Delete (Restored from .trash back to physical directory)
        repository.restoreFile(deletedItem)
        val restoredFiles = repository.allFilesFlow.first()
        val restoredItem = restoredFiles.find { it.id == fileId }
        assertNotNull(restoredItem)
        val restoredPhysicalFile = File(restoredItem!!.filePath)
        assertTrue("Restored file must be moved back from .trash to physical directory", restoredPhysicalFile.exists())
        assertFalse("Restored file must no longer reside in .trash", trashFile.exists())
    }

    @Test
    fun testReconciliationWithPhysicalFilesystem() = runBlocking {
        // Create an external physical file directly on the disk
        val rawFolder = File(context.filesDir, "media_library/External Sync")
        rawFolder.mkdirs()
        val externalFile = File(rawFolder, "external_sample.jpg")
        externalFile.writeText("Sample image content for testing disk sync")

        // Populate initial folders in DB
        val folderId = repository.createFolder("External Sync", "#FFB000")

        // Run reconciliation
        repository.reconcileWithFilesystem()

        val discoveredFiles = repository.allFilesFlow.first()
        val found = discoveredFiles.find { it.name == "external_sample" }
        assertNotNull("Reconciliation must discover real physical file", found)
        assertEquals("jpg", found?.extension)
        assertEquals(MediaType.IMAGE, found?.mediaType)

        // Now simulate external physical deletion
        externalFile.delete()
        assertFalse(externalFile.exists())

        // Reconcile again -> DB must clean up stale records
        repository.reconcileWithFilesystem()
        val afterSync = repository.allFilesFlow.first()
        assertTrue("Stale database records must be removed if physical file was deleted externally",
            afterSync.none { it.name == "external_sample" })
    }

    @Test
    fun testDuplicateDetectionAllLevels() {
        val dummyFiles = listOf(
            // Group 1: Exact Hash Duplicate
            FileItem(
                id = 1, name = "song_original", extension = "mp3",
                folderId = 1, folderName = "Music", folderColorHex = "#00FF66",
                sizeBytes = 5_000_000, mimeType = "audio/mpeg",
                mediaType = MediaType.AUDIO, createdAt = 1000L, sortOrder = 0,
                filePath = "/dummy/song_original.mp3", fileHash = "HASH_ABC_123",
                bitrateKbps = 320, sampleRateHz = 44100
            ),
            FileItem(
                id = 2, name = "song_copy", extension = "mp3",
                folderId = 1, folderName = "Music", folderColorHex = "#00FF66",
                sizeBytes = 5_000_000, mimeType = "audio/mpeg",
                mediaType = MediaType.AUDIO, createdAt = 2000L, sortOrder = 1,
                filePath = "/dummy/song_copy.mp3", fileHash = "HASH_ABC_123",
                bitrateKbps = 128, sampleRateHz = 44100
            ),
            // Group 2: Normalized Filename Duplicate ("Photo (1).jpg" and "Photo.jpg")
            FileItem(
                id = 3, name = "photo", extension = "jpg",
                folderId = 2, folderName = "Pics", folderColorHex = "#00E5FF",
                sizeBytes = 2_000_000, mimeType = "image/jpeg",
                mediaType = MediaType.IMAGE, createdAt = 1500L, sortOrder = 2,
                filePath = "/dummy/photo.jpg", fileHash = "HASH_DEF",
                dimensions = "1920x1080"
            ),
            FileItem(
                id = 4, name = "photo (1)", extension = "jpg",
                folderId = 2, folderName = "Pics", folderColorHex = "#00E5FF",
                sizeBytes = 2_050_000, mimeType = "image/jpeg",
                mediaType = MediaType.IMAGE, createdAt = 2500L, sortOrder = 3,
                filePath = "/dummy/photo (1).jpg", fileHash = "HASH_GHI",
                dimensions = "1920x1080"
            )
        )

        // Run scanner with Highest Quality rule
        val groups = DuplicateScanner.findDuplicates(dummyFiles, KeepBestRule.HIGHEST_QUALITY)
        assertEquals(2, groups.size)

        // Exact match group
        val exactGroup = groups.find { it.matchType == DuplicateMatchType.EXACT_HASH }
        assertNotNull(exactGroup)
        assertEquals(2, exactGroup?.items?.size)
        // With HIGHEST_QUALITY rule, song_original (320kbps) should be recommended to keep over song_copy (128kbps)
        assertEquals(1L, exactGroup?.recommendedKeepId)
        assertTrue(2L in exactGroup!!.userSelectedDeleteIds)

        // Filename group
        val filenameGroup = groups.find { it.matchType == DuplicateMatchType.FILENAME_NORMALIZED }
        assertNotNull(filenameGroup)
        assertEquals(2, filenameGroup?.items?.size)
    }

    @Test
    fun testInventoryExportCsvAndTxt() {
        val items = listOf(
            FileItem(
                id = 1, name = "Synth_Lead", extension = "wav",
                folderId = 1, folderName = "Stems", folderColorHex = "#00FF66",
                sizeBytes = 10_485_760, mimeType = "audio/wav",
                mediaType = MediaType.AUDIO, createdAt = 1700000000000L, sortOrder = 0,
                filePath = "/storage/Synth_Lead.wav"
            )
        )

        // Test CSV
        val csvFile = InventoryExporter.exportInventory(context, items, InventoryExporter.ExportFormat.CSV, includeExtendedColumns = true)
        assertTrue(csvFile.exists())
        val csvContent = csvFile.readText()
        assertTrue(csvContent.contains("Synth_Lead.wav"))
        assertTrue(csvContent.contains("Stems"))
        assertTrue(csvContent.contains("10.00 MB"))

        // Test TXT
        val txtFile = InventoryExporter.exportInventory(context, items, InventoryExporter.ExportFormat.TXT, includeExtendedColumns = true)
        assertTrue(txtFile.exists())
        val txtContent = txtFile.readText()
        assertTrue(txtContent.contains("FILE INVENTORY EXPORT"))
        assertTrue(txtContent.contains("Synth_Lead.wav"))

        // Test detailed export with user options (Scope, Include Name/Type/Location, Format CSV & TXT)
        val detailedCsv = InventoryExporter.exportInventoryDetailed(
            context = context,
            files = items,
            format = InventoryExporter.ExportFormat.CSV,
            includeName = true,
            includeType = true,
            includeLocation = true,
            includeSize = false,
            includeDate = false,
            scopeTitle = "Entire library"
        )
        assertTrue(detailedCsv.exists())
        val detailedCsvContent = detailedCsv.readText()
        assertTrue(detailedCsvContent.contains("File Name,File Type,File Location"))
        assertTrue(detailedCsvContent.contains("\"Synth_Lead.wav\",\"WAV\",\"Stems\""))

        val detailedTxt = InventoryExporter.exportInventoryDetailed(
            context = context,
            files = items,
            format = InventoryExporter.ExportFormat.TXT,
            includeName = true,
            includeType = true,
            includeLocation = true,
            includeSize = false,
            includeDate = false,
            scopeTitle = "Entire library"
        )
        assertTrue(detailedTxt.exists())
        val detailedTxtContent = detailedTxt.readText()
        assertTrue(detailedTxtContent.contains("Scope: Entire library"))
        assertTrue(detailedTxtContent.contains("Synth_Lead.wav"))
        assertTrue(detailedTxtContent.contains("Stems"))
    }

    @Test
    fun testHighVolumeStressPerformance() = runBlocking {
        // High-volume batch insertion: verify memory and query stability
        val folderId = repository.createFolder("Stress Test Folder", "#00E5FF")
        val batchSize = 1000

        val entities = (1..batchSize).map { i ->
            com.example.data.local.entity.FileItemEntity(
                name = "stress_track_$i",
                extension = "mp3",
                folderId = folderId,
                sizeBytes = (1000L * i),
                mimeType = "audio/mpeg",
                mediaType = MediaType.AUDIO.name,
                filePath = "/storage/stress_track_$i.mp3",
                sortOrder = i
            )
        }

        fileDao.insertAll(entities)
        val count = fileDao.getFileCount()
        assertEquals(batchSize, count)

        val files = repository.allFilesFlow.first()
        assertEquals(batchSize, files.size)
    }

    @Test
    fun testRenamePhysicalFileAndDatabase() = runBlocking {
        val folderId = repository.createFolder("Rename Folder", "#2563EB")
        val fileId = repository.createFile(
            name = "original_track",
            extension = "wav",
            folderId = folderId,
            sizeBytes = 4096,
            mimeType = "audio/wav",
            mediaType = MediaType.AUDIO,
            durationMs = 60000,
            mediaMeta = "24-bit • 48 kHz"
        )

        var file = repository.allFilesFlow.first().find { it.id == fileId }
        assertNotNull(file)
        assertEquals("original_track", file?.name)
        assertTrue(File(file!!.filePath).exists())

        // Rename through repository
        val success = repository.renameFile(fileId, "renamed_master")
        assertTrue(success != null)

        file = repository.allFilesFlow.first().find { it.id == fileId }
        assertNotNull(file)
        assertEquals("renamed_master", file?.name)
        assertEquals("renamed_master.wav", file?.fullName)
        assertTrue(File(file!!.filePath).exists())
        assertFalse(File(context.filesDir, "media_library/Rename Folder/original_track.wav").exists())
    }
}
