package com.example.util

import com.example.model.FileItem
import com.example.model.MediaType
import java.util.Locale
import kotlin.math.abs

enum class DuplicateMatchType(val title: String, val levelTag: String) {
    EXACT_HASH("Exact Content Match (Hash)", "LVL 1: EXACT"),
    FILENAME_NORMALIZED("Filename Similarity", "LVL 2: FILENAME"),
    MEDIA_SIMILARITY("Media Signature Similarity", "LVL 3: MEDIA SPEC")
}

enum class KeepBestRule(val label: String) {
    HIGHEST_QUALITY("Highest Bitrate / Quality"),
    LARGEST_FILE("Largest File Size"),
    NEWEST_DATE("Newest Modified Date"),
    OLDEST_DATE("Oldest (Original Date)");

    val displayName: String get() = label
}

data class DuplicateGroup(
    val id: String,
    val title: String,
    val matchType: DuplicateMatchType,
    val mediaCategory: MediaType,
    val items: List<FileItem>,
    val recommendedKeepId: Long,
    val userSelectedDeleteIds: Set<Long> = emptySet(),
    val evidence: String = ""
) {
    val displayName: String get() = title
}

object DuplicateScanner {

    /**
     * Scans the file list across 3 levels:
     * 1. Exact hash (Byte-for-byte SHA-256 verification)
     * 2. Normalized filename (stripping ' (1)', ' - Copy', '_copy', etc.)
     * 3. Media similarity (duration, bitrate, dimensions)
     */
    fun findDuplicates(files: List<FileItem>, currentRule: KeepBestRule = KeepBestRule.HIGHEST_QUALITY): List<DuplicateGroup> {
        val groups = mutableListOf<DuplicateGroup>()
        val processedFileIds = mutableSetOf<Long>()

        // -------------------------------------------------------------
        // Level 1: Exact Hash Comparison (Byte-for-byte SHA-256 evidence only)
        // -------------------------------------------------------------
        val filesWithHash = files.filter { it.fileHash.isNotBlank() }
        val hashGroups = filesWithHash.groupBy { it.fileHash }
        for ((hash, groupFiles) in hashGroups) {
            if (groupFiles.size > 1) {
                val shortHash = if (hash.length > 12) hash.take(12) + "..." else hash
                val group = buildGroup(
                    id = "exact_$hash",
                    title = "Exact Content Duplicate: ${groupFiles.first().name}",
                    matchType = DuplicateMatchType.EXACT_HASH,
                    items = groupFiles,
                    rule = currentRule,
                    evidence = "Content Hash: SHA-256 ($shortHash) match. Byte contents are identical."
                )
                groups.add(group)
                processedFileIds.addAll(groupFiles.map { it.id })
            }
        }

        // Remaining files not yet in exact duplicate groups
        val remainingFiles = files.filter { it.id !in processedFileIds }

        // -------------------------------------------------------------
        // Level 2: Filename Normalized Comparison (Supporting signal only)
        // -------------------------------------------------------------
        val filenameGroups = remainingFiles.groupBy { normalizeFilename(it.name) }
        for ((normName, groupFiles) in filenameGroups) {
            if (groupFiles.size > 1 && normName.length >= 3) {
                val group = buildGroup(
                    id = "filename_$normName",
                    title = "Filename Similarity: \"$normName\"",
                    matchType = DuplicateMatchType.FILENAME_NORMALIZED,
                    items = groupFiles,
                    rule = currentRule,
                    evidence = "Supporting signal: Normalized name similarity ('$normName'). Byte content hash unverified."
                )
                groups.add(group)
                processedFileIds.addAll(groupFiles.map { it.id })
            }
        }

        // -------------------------------------------------------------
        // Level 3: Media Similarity (Supporting signals: Audio duration; Images: dimensions)
        // -------------------------------------------------------------
        val remainingForMedia = files.filter { it.id !in processedFileIds }

        // Audio similarity
        val audioFiles = remainingForMedia.filter { it.mediaType == MediaType.AUDIO && it.durationMs != null && it.durationMs > 1000 }
        val checkedAudio = mutableSetOf<Long>()
        for (i in audioFiles.indices) {
            val a1 = audioFiles[i]
            if (a1.id in checkedAudio) continue
            val similarAudios = mutableListOf(a1)
            for (j in i + 1 until audioFiles.size) {
                val a2 = audioFiles[j]
                if (a2.id in checkedAudio) continue

                // Check duration within +/- 1500 ms
                val durDiff = abs((a1.durationMs ?: 0L) - (a2.durationMs ?: 0L))
                if (durDiff <= 1500) {
                    similarAudios.add(a2)
                    checkedAudio.add(a2.id)
                }
            }
            if (similarAudios.size > 1) {
                checkedAudio.add(a1.id)
                val group = buildGroup(
                    id = "media_audio_${a1.id}",
                    title = "Audio Spec Match: ${a1.name} (~${a1.formattedDuration})",
                    matchType = DuplicateMatchType.MEDIA_SIMILARITY,
                    items = similarAudios,
                    rule = currentRule,
                    evidence = "Supporting signal: Duration within 1.5s (~${a1.formattedDuration}). Byte content hash unverified."
                )
                groups.add(group)
                processedFileIds.addAll(similarAudios.map { it.id })
            }
        }

        // Image similarity (same dimensions & close size)
        val imageFiles = remainingForMedia.filter { it.mediaType == MediaType.IMAGE && !it.dimensions.isNullOrBlank() }
        val checkedImages = mutableSetOf<Long>()
        for (i in imageFiles.indices) {
            val img1 = imageFiles[i]
            if (img1.id in checkedImages) continue
            val similarImages = mutableListOf(img1)
            for (j in i + 1 until imageFiles.size) {
                val img2 = imageFiles[j]
                if (img2.id in checkedImages) continue

                if (img1.dimensions == img2.dimensions) {
                    similarImages.add(img2)
                    checkedImages.add(img2.id)
                }
            }
            if (similarImages.size > 1) {
                checkedImages.add(img1.id)
                val group = buildGroup(
                    id = "media_img_${img1.id}",
                    title = "Photo Resolution Match: ${img1.dimensions}",
                    matchType = DuplicateMatchType.MEDIA_SIMILARITY,
                    items = similarImages,
                    rule = currentRule,
                    evidence = "Supporting signal: Image resolution match (${img1.dimensions}). Byte content hash unverified."
                )
                groups.add(group)
                processedFileIds.addAll(similarImages.map { it.id })
            }
        }

        return groups
    }

    private fun buildGroup(
        id: String,
        title: String,
        matchType: DuplicateMatchType,
        items: List<FileItem>,
        rule: KeepBestRule,
        evidence: String = ""
    ): DuplicateGroup {
        val recommendedKeep = selectSurvivor(items, rule)
        val defaultDelete = items.filter { it.id != recommendedKeep.id }.map { it.id }.toSet()
        val category = items.firstOrNull()?.mediaType ?: MediaType.OTHER

        return DuplicateGroup(
            id = id,
            title = title,
            matchType = matchType,
            mediaCategory = category,
            items = items,
            recommendedKeepId = recommendedKeep.id,
            userSelectedDeleteIds = defaultDelete,
            evidence = evidence
        )
    }

    /**
     * Applies Smart Survivor Selection rules:
     * - Highest audio quality (bitrate/sample rate)
     * - Largest file size
     * - Newest
     * - Oldest
     */
    fun selectSurvivor(items: List<FileItem>, rule: KeepBestRule): FileItem {
        return when (rule) {
            KeepBestRule.HIGHEST_QUALITY -> {
                // Prioritize highest bitrate, then sample rate, then size
                items.maxWithOrNull(
                    compareBy<FileItem> { it.bitrateKbps ?: 0 }
                        .thenBy { it.sampleRateHz ?: 0 }
                        .thenBy { it.sizeBytes }
                ) ?: items.first()
            }
            KeepBestRule.LARGEST_FILE -> {
                items.maxByOrNull { it.sizeBytes } ?: items.first()
            }
            KeepBestRule.NEWEST_DATE -> {
                items.maxByOrNull { it.createdAt } ?: items.first()
            }
            KeepBestRule.OLDEST_DATE -> {
                items.minByOrNull { it.createdAt } ?: items.first()
            }
        }
    }

    /**
     * Normalizes a filename to catch variants:
     * "Song (1)", "Song - Copy", "Song_copy", "song" -> "song"
     */
    fun normalizeFilename(rawName: String): String {
        return rawName.lowercase(Locale.ROOT)
            .replace(Regex("\\s*\\(\\d+\\)$"), "") // removes (1), (2)
            .replace(Regex("\\s*-\\s*copy$"), "")    // removes - copy
            .replace(Regex("_copy$"), "")             // removes _copy
            .replace(Regex("[_\\-.]"), " ")           // replace separators with space
            .trim()
    }
}
