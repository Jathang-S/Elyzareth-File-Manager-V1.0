package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.local.dao.FileDao
import com.example.data.local.dao.FolderDao
import com.example.data.local.entity.FileItemEntity
import com.example.data.local.entity.FolderEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [FolderEntity::class, FileItemEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun fileDao(): FileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "elyzareth_file_manager.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context.applicationContext, scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val appContext: Context,
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        val fsManager = com.example.util.FileSystemManager(appContext)
                        fsManager.ensurePhysicalSeedFiles()
                        populateFromDisk(database.folderDao(), database.fileDao(), fsManager)
                    }
                }
            }
        }

        suspend fun populateFromDisk(
            folderDao: FolderDao,
            fileDao: FileDao,
            fsManager: com.example.util.FileSystemManager
        ) {
            val initialFolders = listOf(
                FolderEntity(id = 1, name = "Music", colorHex = "#2563EB", iconName = "music", sortOrder = 0),
                FolderEntity(id = 2, name = "AI generations", colorHex = "#9333EA", iconName = "auto_awesome", sortOrder = 1),
                FolderEntity(id = 3, name = "Selected / approved", colorHex = "#16A34A", iconName = "check_circle", sortOrder = 2),
                FolderEntity(id = 4, name = "Work in progress", colorHex = "#EA580C", iconName = "construction", sortOrder = 3),
                FolderEntity(id = 5, name = "Reject/archive", colorHex = "#DC2626", iconName = "archive", sortOrder = 4),
                FolderEntity(id = 6, name = "Export", colorHex = "#EAB308", iconName = "file_download", sortOrder = 5)
            )
            folderDao.insertAll(initialFolders)

            val discovered = fsManager.scanFileSystem()
            val folderMap = folderDao.getAllFoldersSync().associateBy { it.name }

            val fileEntities = discovered.mapIndexed { index, disc ->
                val matchedFolderId = folderMap[disc.relativeFolder]?.id ?: 1L
                FileItemEntity(
                    name = disc.name,
                    extension = disc.extension,
                    folderId = matchedFolderId,
                    sizeBytes = disc.sizeBytes,
                    mimeType = when (disc.mediaType) {
                        com.example.model.MediaType.AUDIO -> "audio/${disc.extension}"
                        com.example.model.MediaType.IMAGE -> "image/${disc.extension}"
                        com.example.model.MediaType.VIDEO -> "video/${disc.extension}"
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
                    sortOrder = index
                )
            }
            fileDao.insertAll(fileEntities)
        }

        suspend fun populateInitialData(folderDao: FolderDao, fileDao: FileDao) {
            val initialFolders = listOf(
                FolderEntity(id = 1, name = "Music", colorHex = "#2563EB", iconName = "music", sortOrder = 0),
                FolderEntity(id = 2, name = "AI generations", colorHex = "#9333EA", iconName = "auto_awesome", sortOrder = 1),
                FolderEntity(id = 3, name = "Selected / approved", colorHex = "#16A34A", iconName = "check_circle", sortOrder = 2),
                FolderEntity(id = 4, name = "Work in progress", colorHex = "#EA580C", iconName = "construction", sortOrder = 3),
                FolderEntity(id = 5, name = "Reject/archive", colorHex = "#DC2626", iconName = "archive", sortOrder = 4),
                FolderEntity(id = 6, name = "Export", colorHex = "#EAB308", iconName = "file_download", sortOrder = 5)
            )
            folderDao.insertAll(initialFolders)

            val initialFiles = listOf(
                FileItemEntity(
                    id = 1,
                    name = "DJ Mike Llama - Llama Whippin' Intro",
                    extension = "mp3",
                    folderId = 1,
                    sizeBytes = 3_420_000,
                    mimeType = "audio/mpeg",
                    mediaType = "AUDIO",
                    durationMs = 212_000,
                    mediaMeta = "320 kbps MP3 • 44.1 kHz Stereo",
                    sortOrder = 0
                ),
                FileItemEntity(
                    id = 2,
                    name = "Midnight Drive (Neon Cruise Remaster)",
                    extension = "flac",
                    folderId = 1,
                    sizeBytes = 28_750_000,
                    mimeType = "audio/flac",
                    mediaType = "AUDIO",
                    durationMs = 254_000,
                    mediaMeta = "1411 kbps 24-bit FLAC Hi-Res",
                    sortOrder = 1
                ),
                FileItemEntity(
                    id = 3,
                    name = "AI Generated Dreamscape 4K",
                    extension = "png",
                    folderId = 2,
                    sizeBytes = 14_200_000,
                    mimeType = "image/png",
                    mediaType = "IMAGE",
                    durationMs = null,
                    mediaMeta = "3840x2160 UHD • AI Prompt Seed 4920",
                    sortOrder = 2
                ),
                FileItemEntity(
                    id = 4,
                    name = "Retro_Grid_Sunset_UltraHD",
                    extension = "png",
                    folderId = 3,
                    sizeBytes = 8_910_000,
                    mimeType = "image/png",
                    mediaType = "IMAGE",
                    durationMs = null,
                    mediaMeta = "4096x2160 • Approved Final Cut",
                    sortOrder = 3
                ),
                FileItemEntity(
                    id = 5,
                    name = "Analog Synthesizer Bassline Stems",
                    extension = "wav",
                    folderId = 4,
                    sizeBytes = 42_300_000,
                    mimeType = "audio/wav",
                    mediaType = "AUDIO",
                    durationMs = 186_000,
                    mediaMeta = "24-bit 48kHz Linear PCM Draft",
                    sortOrder = 4
                ),
                FileItemEntity(
                    id = 6,
                    name = "Midnight Drive (Outtake Take 2)",
                    extension = "wav",
                    folderId = 5,
                    sizeBytes = 32_100_000,
                    mimeType = "audio/wav",
                    mediaType = "AUDIO",
                    durationMs = 195_000,
                    mediaMeta = "Archived discard",
                    sortOrder = 5
                ),
                FileItemEntity(
                    id = 7,
                    name = "Elyzareth Specification & Notes",
                    extension = "pdf",
                    folderId = 6,
                    sizeBytes = 2_150_000,
                    mimeType = "application/pdf",
                    mediaType = "DOCUMENT",
                    durationMs = null,
                    mediaMeta = "Ready for export distribution",
                    sortOrder = 6
                ),
                FileItemEntity(
                    id = 8,
                    name = "Vaporwave Hologram Portrait",
                    extension = "jpg",
                    folderId = 2,
                    sizeBytes = 4_350_000,
                    mimeType = "image/jpeg",
                    mediaType = "IMAGE",
                    durationMs = null,
                    mediaMeta = "AI Generated Visual Asset",
                    sortOrder = 7
                ),
                FileItemEntity(
                    id = 9,
                    name = "Sub-Bass 808 One-Shot Collection",
                    extension = "zip",
                    folderId = 6,
                    sizeBytes = 55_400_000,
                    mimeType = "application/zip",
                    mediaType = "ARCHIVE",
                    durationMs = null,
                    mediaMeta = "48 Samples • Compressed ZIP",
                    sortOrder = 8
                ),
                FileItemEntity(
                    id = 10,
                    name = "Track 10 - Hyperdrive Orbit Finale",
                    extension = "mp3",
                    folderId = 1,
                    sizeBytes = 7_820_000,
                    mimeType = "audio/mpeg",
                    mediaType = "AUDIO",
                    durationMs = 318_000,
                    mediaMeta = "320 kbps MP3 VBR Stereo",
                    sortOrder = 9
                )
            )
            fileDao.insertAll(initialFiles)
        }
    }
}
