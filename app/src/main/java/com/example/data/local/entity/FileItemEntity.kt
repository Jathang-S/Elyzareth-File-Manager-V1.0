package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "files",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["folderId"])]
)
data class FileItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val extension: String,
    val folderId: Long,
    val sizeBytes: Long,
    val mimeType: String,
    val mediaType: String, // AUDIO, VIDEO, IMAGE, DOCUMENT, ARCHIVE
    val durationMs: Long? = null,
    val mediaMeta: String? = null, // e.g. "320 kbps MP3", "1080p 60fps", "4000x3000"
    val filePath: String = "",
    val fileHash: String = "",
    val bitrateKbps: Int? = null,
    val sampleRateHz: Int? = null,
    val dimensions: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
