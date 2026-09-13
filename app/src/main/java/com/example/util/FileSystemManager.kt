package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.media.MediaMetadataRetriever
import android.util.Log
import com.example.model.MediaType
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.math.sin

data class DiscoveredFile(
    val file: File,
    val name: String,
    val extension: String,
    val relativeFolder: String,
    val sizeBytes: Long,
    val hash: String,
    val mediaType: MediaType,
    val durationMs: Long?,
    val bitrateKbps: Int?,
    val sampleRateHz: Int?,
    val dimensions: String?,
    val mediaMeta: String?,
    val lastModified: Long
)

class FileSystemManager(private val context: Context) {

    private val rootDir: File = File(context.filesDir, "media_library")
    private val trashDir: File = File(context.filesDir, ".trash")

    init {
        if (!rootDir.exists()) rootDir.mkdirs()
        if (!trashDir.exists()) trashDir.mkdirs()
    }

    fun getRootDirectory(): File = rootDir
    fun getTrashDirectory(): File = trashDir

    /**
     * Ensures initial physical directory structure and seed files exist on disk.
     */
    fun ensurePhysicalSeedFiles() {
        val folders = listOf(
            "Music",
            "AI generations",
            "Selected / approved",
            "Work in progress",
            "Reject/archive",
            "Export"
        )

        for (folder in folders) {
            val dir = File(rootDir, folder)
            if (!dir.exists()) dir.mkdirs()
        }

        // Seed real playable audio files (WAV audio with genuine RIFF headers and audible synth tones)
        createPlayableAudioFile(
            folderName = "Music",
            fileName = "Llama_Whippin_Intro.wav",
            frequencyHz = 440.0,
            durationSeconds = 6
        )

        createPlayableAudioFile(
            folderName = "Music",
            fileName = "Midnight_Drive_Remaster.wav",
            frequencyHz = 523.25, // C5
            durationSeconds = 8
        )

        // Seed an intentional DUPLICATE for Level 1 (Exact hash match):
        // Same file content copied with different name in Reject/archive folder!
        createExactDuplicateAudio(
            srcFolder = "Music",
            srcFileName = "Midnight_Drive_Remaster.wav",
            destFolder = "Reject/archive",
            destFileName = "Midnight_Drive_Backup_Copy.wav"
        )

        // Seed an intentional DUPLICATE for Level 2 (Filename normalized match):
        createPlayableAudioFile(
            folderName = "Music",
            fileName = "Midnight_Drive_Remaster (1).wav",
            frequencyHz = 523.25,
            durationSeconds = 8
        )

        // Seed real photos / artwork with real decoded bitmaps in AI generations & Selected
        createRealImageFile(
            folderName = "Selected / approved",
            fileName = "Retro_Grid_Sunset.png",
            width = 1200,
            height = 800,
            bgTheme = "retro_sunset"
        )

        // Intentional Photo duplicate (Exact copy in AI generations folder)
        createExactDuplicateImage(
            srcFolder = "Selected / approved",
            srcFileName = "Retro_Grid_Sunset.png",
            destFolder = "AI generations",
            destFileName = "Retro_Sunset_Duplicate_Raw.png"
        )

        createRealImageFile(
            folderName = "AI generations",
            fileName = "Vaporwave_Hologram.png",
            width = 1080,
            height = 1080,
            bgTheme = "vaporwave"
        )

        // Additional stem audio in Work in progress
        createPlayableAudioFile(
            folderName = "Work in progress",
            fileName = "Analog_Synth_Bassline.wav",
            frequencyHz = 110.0, // A2 bass
            durationSeconds = 5
        )

        // Real text document in Export
        createTextFile(
            folderName = "Export",
            fileName = "Elyzareth_Overview.txt",
            content = "ELYZARETH FILE MANAGER\n=====================\n\nFeatures:\n- Purposeful folder coloring (Music, AI generations, Approved, WIP, Archive, Export)\n- Clean mobile file manager view\n- Real filesystem synchronization\n- Zero-memory inline audio preview & full image inspector\n- 3-level duplicate detection: Exact, Filename, Media similarity\n- Instant deletion with 5-second undo recovery\n- Complete inventory export (Scope: Current/Selected/All, Columns: Name, Type, Location, Format: CSV/TXT)\n"
        )

        // Real zip archive in Export
        createRealZipArchive(
            folderName = "Export",
            fileName = "Distribution_Stems_Pack.zip"
        )
    }

    /**
     * Recursively discovers all files across all nested folders on disk.
     */
    fun scanFileSystem(): List<DiscoveredFile> {
        val result = mutableListOf<DiscoveredFile>()
        if (!rootDir.exists()) return result

        scanDirectoryRecursive(rootDir, "", result)
        return result
    }

    private fun scanDirectoryRecursive(currentDir: File, relativePath: String, outList: MutableList<DiscoveredFile>) {
        val children = currentDir.listFiles() ?: return
        for (file in children) {
            if (file.isDirectory) {
                if (file.name != ".trash") {
                    val subPath = if (relativePath.isEmpty()) file.name else "$relativePath/${file.name}"
                    scanDirectoryRecursive(file, subPath, outList)
                }
            } else {
                val nameWithoutExt = file.nameWithoutExtension
                val ext = file.extension.lowercase()
                val mediaType = MediaType.fromExtension(ext)
                val hash = computeSha256(file)
                val folderName = if (relativePath.isEmpty()) "Root" else relativePath

                var durationMs: Long? = null
                var bitrateKbps: Int? = null
                var sampleRateHz: Int? = null
                var dimensions: String? = null
                var mediaMeta: String? = null

                if (mediaType == MediaType.AUDIO || mediaType == MediaType.VIDEO) {
                    try {
                        val retriever = MediaMetadataRetriever()
                        retriever.setDataSource(file.absolutePath)
                        val durStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                        val bitrateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                        retriever.release()

                        durationMs = durStr?.toLongOrNull()
                        bitrateKbps = bitrateStr?.toIntOrNull()?.let { it / 1000 }
                    } catch (_: Exception) {
                        // Fallback estimate for generated WAV
                        if (ext == "wav") {
                            durationMs = (file.length() / (44100 * 2 * 2)) * 1000L
                            bitrateKbps = 1411
                            sampleRateHz = 44100
                        }
                    }
                    sampleRateHz = sampleRateHz ?: 44100
                    bitrateKbps = bitrateKbps ?: 320
                    mediaMeta = "${bitrateKbps} kbps • ${sampleRateHz} Hz Stereo"
                } else if (mediaType == MediaType.IMAGE) {
                    try {
                        val options = android.graphics.BitmapFactory.Options().apply {
                            inJustDecodeBounds = true
                        }
                        android.graphics.BitmapFactory.decodeFile(file.absolutePath, options)
                        if (options.outWidth > 0 && options.outHeight > 0) {
                            dimensions = "${options.outWidth}x${options.outHeight}"
                            mediaMeta = "$dimensions • ${options.outMimeType ?: "image/$ext"}"
                        }
                    } catch (_: Exception) {
                        mediaMeta = "Image format (.$ext)"
                    }
                } else {
                    mediaMeta = "${file.extension.uppercase()} file"
                }

                outList.add(
                    DiscoveredFile(
                        file = file,
                        name = nameWithoutExt,
                        extension = ext,
                        relativeFolder = folderName,
                        sizeBytes = file.length(),
                        hash = hash,
                        mediaType = mediaType,
                        durationMs = durationMs,
                        bitrateKbps = bitrateKbps,
                        sampleRateHz = sampleRateHz,
                        dimensions = dimensions,
                        mediaMeta = mediaMeta,
                        lastModified = file.lastModified()
                    )
                )
            }
        }
    }

    /**
     * Moves physical file on disk to a target folder directory.
     */
    fun movePhysicalFile(srcFile: File, targetFolderName: String): File? {
        if (!srcFile.exists()) return null
        val targetDir = File(rootDir, targetFolderName)
        if (!targetDir.exists()) targetDir.mkdirs()

        val destFile = File(targetDir, srcFile.name)
        val success = srcFile.renameTo(destFile)
        return if (success) destFile else null
    }

    /**
     * Renames a physical file on disk.
     */
    fun renamePhysicalFile(srcFile: File, newNameWithExt: String): File? {
        if (!srcFile.exists()) return null
        val destFile = File(srcFile.parentFile, newNameWithExt)
        val success = srcFile.renameTo(destFile)
        return if (success) destFile else null
    }

    /**
     * Staged physical deletion for instant 5-second undo support.
     * Moves physical file to .trash/
     */
    fun stagePhysicalDelete(file: File): File? {
        if (!file.exists()) return null
        val trashName = "${System.currentTimeMillis()}_${file.name}"
        val trashTarget = File(trashDir, trashName)
        val success = file.renameTo(trashTarget)
        return if (success) trashTarget else null
    }

    /**
     * Restores physical file from trash back to its original location.
     */
    fun restorePhysicalFile(trashFile: File, originalTarget: File): Boolean {
        if (!trashFile.exists()) return false
        originalTarget.parentFile?.mkdirs()
        return trashFile.renameTo(originalTarget)
    }

    /**
     * Permanently deletes physical file from trash.
     */
    fun permanentlyDeletePhysicalFile(trashFile: File): Boolean {
        return if (trashFile.exists()) trashFile.delete() else true
    }

    /**
     * Creates a genuine playable PCM audio WAV file on disk.
     */
    fun createPlayableAudioFile(
        folderName: String,
        fileName: String,
        frequencyHz: Double,
        durationSeconds: Int
    ): File {
        val folder = File(rootDir, folderName)
        if (!folder.exists()) folder.mkdirs()
        val file = File(folder, fileName)
        if (file.exists() && file.length() > 0) return file

        val sampleRate = 44100
        val numSamples = sampleRate * durationSeconds
        val numChannels = 2 // Stereo
        val bitsPerSample = 16

        val byteRate = sampleRate * numChannels * (bitsPerSample / 8)
        val blockAlign = numChannels * (bitsPerSample / 8)
        val subChunk2Size = numSamples * numChannels * (bitsPerSample / 8)
        val chunkSize = 36 + subChunk2Size

        val out = FileOutputStream(file)

        // RIFF header
        out.write("RIFF".toByteArray())
        out.write(intToBytesLE(chunkSize))
        out.write("WAVE".toByteArray())

        // fmt chunk
        out.write("fmt ".toByteArray())
        out.write(intToBytesLE(16)) // subchunk1 size
        out.write(shortToBytesLE(1)) // AudioFormat 1 = PCM
        out.write(shortToBytesLE(numChannels.toShort()))
        out.write(intToBytesLE(sampleRate))
        out.write(intToBytesLE(byteRate))
        out.write(shortToBytesLE(blockAlign.toShort()))
        out.write(shortToBytesLE(bitsPerSample.toShort()))

        // data chunk
        out.write("data".toByteArray())
        out.write(intToBytesLE(subChunk2Size))

        // Synthesize a retro melodic synth tone
        val buffer = ByteArray(2 * numChannels)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            // Rich synth tone with fundamental and slight harmonic
            val sampleVal = (sin(2.0 * Math.PI * frequencyHz * t) * 0.7 +
                    sin(2.0 * Math.PI * frequencyHz * 2.0 * t) * 0.3) * 0.5

            val shortSample = (sampleVal * 32767.0).toInt().coerceIn(-32768, 32767).toShort()
            val sampleBytes = shortToBytesLE(shortSample)

            // Left and Right
            buffer[0] = sampleBytes[0]
            buffer[1] = sampleBytes[1]
            buffer[2] = sampleBytes[0]
            buffer[3] = sampleBytes[1]

            out.write(buffer)
        }

        out.flush()
        out.close()
        return file
    }

    private fun createExactDuplicateAudio(srcFolder: String, srcFileName: String, destFolder: String, destFileName: String) {
        val src = File(File(rootDir, srcFolder), srcFileName)
        val dest = File(File(rootDir, destFolder), destFileName)
        if (src.exists() && !dest.exists()) {
            dest.parentFile?.mkdirs()
            src.copyTo(dest, overwrite = true)
        }
    }

    private fun createExactDuplicateImage(srcFolder: String, srcFileName: String, destFolder: String, destFileName: String) {
        val src = File(File(rootDir, srcFolder), srcFileName)
        val dest = File(File(rootDir, destFolder), destFileName)
        if (src.exists() && !dest.exists()) {
            dest.parentFile?.mkdirs()
            src.copyTo(dest, overwrite = true)
        }
    }

    /**
     * Creates a real PNG image file on disk with custom retro art rendering.
     */
    fun createRealImageFile(
        folderName: String,
        fileName: String,
        width: Int,
        height: Int,
        bgTheme: String
    ): File {
        val folder = File(rootDir, folderName)
        if (!folder.exists()) folder.mkdirs()
        val file = File(folder, fileName)
        if (file.exists() && file.length() > 0) return file

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        when (bgTheme) {
            "retro_sunset" -> {
                // Gradient dark to deep purple
                paint.color = AndroidColor.rgb(15, 12, 28)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                // Sun
                paint.color = AndroidColor.rgb(255, 90, 40)
                canvas.drawCircle(width / 2f, height / 2f - 40f, 220f, paint)

                // Retro Grid Lines
                paint.color = AndroidColor.rgb(0, 229, 255)
                paint.strokeWidth = 3f
                for (y in (height / 2)..height step 40) {
                    canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
                }
                for (x in 0..width step 80) {
                    canvas.drawLine(width / 2f, height / 2f, x.toFloat(), height.toFloat(), paint)
                }
            }
            "vaporwave" -> {
                paint.color = AndroidColor.rgb(18, 22, 36)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                paint.color = AndroidColor.rgb(0, 255, 102)
                paint.textSize = 64f
                paint.isFakeBoldText = true
                canvas.drawText("WINAMP v2.9", 80f, 200f, paint)

                paint.color = AndroidColor.rgb(255, 30, 118)
                canvas.drawRect(80f, 240f, (width - 80).toFloat(), (height - 200).toFloat(), paint)

                paint.color = AndroidColor.rgb(0, 229, 255)
                canvas.drawCircle(width / 2f, height / 2f, 180f, paint)
            }
            else -> {
                paint.color = AndroidColor.rgb(20, 28, 40)
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
        }

        val fos = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 95, fos)
        fos.flush()
        fos.close()
        bitmap.recycle()
        return file
    }

    private fun createTextFile(folderName: String, fileName: String, content: String): File {
        val folder = File(rootDir, folderName)
        if (!folder.exists()) folder.mkdirs()
        val file = File(folder, fileName)
        if (!file.exists() || file.length() == 0L) {
            file.writeText(content)
        }
        return file
    }

    private fun createRealZipArchive(folderName: String, fileName: String): File {
        val folder = File(rootDir, folderName)
        if (!folder.exists()) folder.mkdirs()
        val file = File(folder, fileName)
        if (!file.exists() || file.length() == 0L) {
            val zos = ZipOutputStream(FileOutputStream(file))
            val entry = ZipEntry("readme_samples.txt")
            zos.putNextEntry(entry)
            zos.write("SubBass 808 Samples Matrix - 24-bit 48kHz\n".toByteArray())
            zos.closeEntry()
            zos.flush()
            zos.close()
        }
        return file
    }

    private fun computeSha256(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val fis = FileInputStream(file)
            val buffer = ByteArray(8192)
            var n: Int
            while (fis.read(buffer).also { n = it } != -1) {
                digest.update(buffer, 0, n)
            }
            fis.close()
            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            ""
        }
    }

    private fun intToBytesLE(value: Int): ByteArray {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
    }

    private fun shortToBytesLE(value: Short): ByteArray {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(value).array()
    }
}
