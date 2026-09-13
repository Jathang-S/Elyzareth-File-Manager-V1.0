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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class FileManagerTest {

    private lateinit var database: AppDatabase
    private lateinit var folderDao: FolderDao
    private lateinit var fileDao: FileDao
    private lateinit var repository: FileManagerRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        folderDao = database.folderDao()
        fileDao = database.fileDao()
        repository = FileManagerRepository(folderDao, fileDao)
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun testSeedAndGetAllFiles() = runBlocking {
        AppDatabase.populateInitialData(folderDao, fileDao)

        val folders = repository.foldersFlow.first()
        val files = repository.allFilesFlow.first()

        assertEquals(6, folders.size)
        assertTrue(files.size >= 10)
    }

    @Test
    fun testCustomFolderColoring() = runBlocking {
        val folderId = repository.createFolder("Neon Beats", "#00FF66")
        var folders = repository.foldersFlow.first()
        val folder = folders.find { it.id == folderId }
        assertNotNull(folder)
        assertEquals("#00FF66", folder?.colorHex)

        // Update custom color to Cyber Pink
        repository.updateFolderColor(folderId, "#FF1E76")
        folders = repository.foldersFlow.first()
        val updatedFolder = folders.find { it.id == folderId }
        assertEquals("#FF1E76", updatedFolder?.colorHex)
    }

    @Test
    fun testInstantDeleteWithoutConfirmation() = runBlocking {
        val folderId = repository.createFolder("Audio", "#00FF66")
        val fileId = repository.createFile(
            name = "Test Track",
            extension = "mp3",
            folderId = folderId,
            sizeBytes = 3500000,
            mimeType = "audio/mpeg",
            mediaType = MediaType.AUDIO
        )

        val filesBefore = repository.allFilesFlow.first()
        assertEquals(1, filesBefore.size)

        // Instant deletion
        val deleted = repository.deleteFile(fileId)
        assertNotNull(deleted)
        assertEquals("Test Track.mp3", deleted?.fullName)

        val filesAfter = repository.allFilesFlow.first()
        assertTrue(filesAfter.isEmpty())

        // Test Undo/Restore
        repository.restoreFile(deleted!!)
        val filesRestored = repository.allFilesFlow.first()
        assertEquals(1, filesRestored.size)
        assertEquals("Test Track.mp3", filesRestored[0].fullName)
    }

    @Test
    fun testDragAndDropReorderSortOrders() = runBlocking {
        val folderId = repository.createFolder("Music", "#00FF66")
        val id1 = repository.createFile("Track 1", "mp3", folderId, 1000, "audio/mpeg", MediaType.AUDIO)
        val id2 = repository.createFile("Track 2", "mp3", folderId, 2000, "audio/mpeg", MediaType.AUDIO)
        val id3 = repository.createFile("Track 3", "mp3", folderId, 3000, "audio/mpeg", MediaType.AUDIO)

        var files = repository.allFilesFlow.first()
        assertEquals(3, files.size)
        assertEquals(id1, files[0].id)
        assertEquals(id2, files[1].id)
        assertEquals(id3, files[2].id)

        // Simulate drag & drop reordering (move Track 3 to index 0)
        val reorderedList = listOf(files[2], files[0], files[1])
        repository.updateFileSortOrders(reorderedList)

        val reorderedFiles = repository.allFilesFlow.first()
        assertEquals(id3, reorderedFiles[0].id)
        assertEquals(id1, reorderedFiles[1].id)
        assertEquals(id2, reorderedFiles[2].id)
    }

    @Test
    fun testFileSizeFormatting() {
        assertEquals("500 B", FileItem.formatFileSize(500))
        assertEquals("1.5 KB", FileItem.formatFileSize(1536))
        assertEquals("3.50 MB", FileItem.formatFileSize(3_670_016))
        assertEquals("1.50 GB", FileItem.formatFileSize(1_610_612_736))
    }
}
