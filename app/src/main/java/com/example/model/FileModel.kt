package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MediaType {
    AUDIO,
    VIDEO,
    IMAGE,
    DOCUMENT,
    ARCHIVE,
    OTHER;

    companion object {
        fun fromExtension(ext: String): MediaType {
            return when (ext.lowercase()) {
                "mp3", "flac", "wav", "m4a", "aac", "ogg", "wma" -> AUDIO
                "mp4", "mkv", "mov", "avi", "webm", "3gp" -> VIDEO
                "jpg", "jpeg", "png", "webp", "gif", "svg", "bmp" -> IMAGE
                "pdf", "doc", "docx", "txt", "md", "xls", "xlsx", "ppt", "pptx" -> DOCUMENT
                "zip", "rar", "7z", "tar", "gz" -> ARCHIVE
                else -> OTHER
            }
        }
    }
}

data class FileItem(
    val id: Long,
    val name: String,
    val extension: String,
    val folderId: Long,
    val folderName: String = "",
    val folderColorHex: String = "#00FF66",
    val sizeBytes: Long,
    val mimeType: String,
    val mediaType: MediaType,
    val durationMs: Long? = null,
    val mediaMeta: String? = null,
    val filePath: String = "",
    val fileHash: String = "",
    val bitrateKbps: Int? = null,
    val sampleRateHz: Int? = null,
    val dimensions: String? = null,
    val sortOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    val fullName: String
        get() = if (name.endsWith(".$extension", ignoreCase = true)) name else "$name.$extension"

    val formattedSize: String
        get() = formatFileSize(sizeBytes)

    val formattedDuration: String?
        get() = durationMs?.let { ms ->
            val totalSec = ms / 1000
            val min = totalSec / 60
            val sec = totalSec % 60
            String.format(Locale.US, "%d:%02d", min, sec)
        }

    val formattedDate: String
        get() = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(createdAt))

    companion object {
        fun formatFileSize(bytes: Long): String {
            if (bytes <= 0) return "0 B"
            val kb = bytes / 1024.0
            val mb = kb / 1024.0
            val gb = mb / 1024.0
            return when {
                gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
                mb >= 1.0 -> String.format(Locale.US, "%.2f MB", mb)
                kb >= 1.0 -> String.format(Locale.US, "%.1f KB", kb)
                else -> "$bytes B"
            }
        }
    }
}

data class FolderModel(
    val id: Long,
    val name: String,
    val colorHex: String,
    val iconName: String = "folder",
    val sortOrder: Int = 0,
    val fileCount: Int = 0,
    val totalSizeBytes: Long = 0L
)
