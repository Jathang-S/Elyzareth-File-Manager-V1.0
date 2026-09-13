package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.model.FileItem
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object InventoryExporter {

    enum class ExportFormat {
        CSV, TXT
    }

    enum class ExportScope {
        CURRENT_FOLDER,
        SELECTED_FOLDER,
        ENTIRE_LIBRARY
    }

    /**
     * Primary detailed inventory export function supporting customizable columns and scope.
     */
    fun exportInventoryDetailed(
        context: Context,
        files: List<FileItem>,
        format: ExportFormat,
        includeName: Boolean = true,
        includeType: Boolean = true,
        includeLocation: Boolean = true,
        includeSize: Boolean = false,
        includeDate: Boolean = false,
        scopeTitle: String = "Entire Library"
    ): File {
        val exportDir = File(context.filesDir, "media_library/Export")
        if (!exportDir.exists()) exportDir.mkdirs()

        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "elyzareth_inventory_$timeStamp.${format.name.lowercase()}"
        val targetFile = File(exportDir, fileName)

        val sb = StringBuilder()

        when (format) {
            ExportFormat.CSV -> {
                val headers = mutableListOf<String>()
                if (includeName) headers.add("File Name")
                if (includeType) headers.add("File Type")
                if (includeLocation) headers.add("File Location")
                if (includeSize) headers.add("File Size")
                if (includeDate) headers.add("Modified Date")

                sb.append(headers.joinToString(",")).append("\n")

                for (f in files) {
                    val row = mutableListOf<String>()
                    if (includeName) row.add("\"${escapeCsv(f.fullName)}\"")
                    if (includeType) row.add("\"${f.extension.uppercase()}\"")
                    if (includeLocation) row.add("\"${escapeCsv(f.folderName)}\"")
                    if (includeSize) row.add("\"${f.formattedSize}\"")
                    if (includeDate) {
                        val date = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(f.createdAt))
                        row.add("\"$date\"")
                    }
                    sb.append(row.joinToString(",")).append("\n")
                }
            }
            ExportFormat.TXT -> {
                sb.append("ELYZARETH FILE INVENTORY EXPORT\n")
                sb.append("Scope: $scopeTitle\n")
                sb.append("Generated: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n")
                sb.append("Total Assets: ${files.size}\n")
                sb.append("=".repeat(88)).append("\n")

                val headerCols = mutableListOf<String>()
                if (includeName) headerCols.add(String.format(Locale.US, "%-38s", "File Name"))
                if (includeType) headerCols.add(String.format(Locale.US, "%-6s", "Type"))
                if (includeLocation) headerCols.add(String.format(Locale.US, "%-22s", "File Location"))
                if (includeSize) headerCols.add(String.format(Locale.US, "%-10s", "Size"))
                if (includeDate) headerCols.add("Date")

                sb.append(headerCols.joinToString(" | ")).append("\n")
                sb.append("-".repeat(88)).append("\n")

                for (f in files) {
                    val rowCols = mutableListOf<String>()
                    if (includeName) rowCols.add(String.format(Locale.US, "%-38s", f.fullName.take(38)))
                    if (includeType) rowCols.add(String.format(Locale.US, "%-6s", f.extension.uppercase().take(6)))
                    if (includeLocation) rowCols.add(String.format(Locale.US, "%-22s", f.folderName.take(22)))
                    if (includeSize) rowCols.add(String.format(Locale.US, "%-10s", f.formattedSize.take(10)))
                    if (includeDate) {
                        val date = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(f.createdAt))
                        rowCols.add(date)
                    }
                    sb.append(rowCols.joinToString(" | ")).append("\n")
                }
                sb.append("=".repeat(88)).append("\n")
            }
        }

        targetFile.writeText(sb.toString())
        return targetFile
    }

    /**
     * Backward-compatible overload for existing calls.
     */
    fun exportInventory(
        context: Context,
        files: List<FileItem>,
        format: ExportFormat,
        includeExtendedColumns: Boolean = false
    ): File {
        return exportInventoryDetailed(
            context = context,
            files = files,
            format = format,
            includeName = true,
            includeType = true,
            includeLocation = true,
            includeSize = includeExtendedColumns,
            includeDate = includeExtendedColumns,
            scopeTitle = "Entire Library"
        )
    }

    private fun escapeCsv(value: String): String {
        return value.replace("\"", "\"\"")
    }

    /**
     * Creates a share Intent for the exported file.
     */
    fun createShareIntent(context: Context, file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = if (file.extension == "csv") "text/csv" else "text/plain"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}
