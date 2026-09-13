package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.FolderModel
import com.example.ui.theme.ElyzarethAccent
import com.example.ui.theme.ElyzarethBackground
import com.example.ui.theme.ElyzarethBorder
import com.example.ui.theme.ElyzarethBorderHighlight
import com.example.ui.theme.ElyzarethPrimary
import com.example.ui.theme.ElyzarethSurface
import com.example.ui.theme.ElyzarethSurfaceElevated
import com.example.ui.theme.ElyzarethTextMuted
import com.example.ui.theme.ElyzarethTextPrimary
import com.example.ui.theme.ElyzarethTextSecondary
import com.example.ui.theme.getPurposeForColor
import com.example.util.InventoryExporter

@Composable
fun InventoryExportDialog(
    folders: List<FolderModel>,
    currentFolderId: Long?,
    onDismiss: () -> Unit,
    onExport: (
        scope: InventoryExporter.ExportScope,
        selectedFolderId: Long?,
        includeName: Boolean,
        includeType: Boolean,
        includeLocation: Boolean,
        format: InventoryExporter.ExportFormat
    ) -> Unit
) {
    var scope by remember { mutableStateOf(InventoryExporter.ExportScope.ENTIRE_LIBRARY) }
    var chosenFolderId by remember {
        mutableStateOf(currentFolderId ?: folders.firstOrNull()?.id)
    }

    var includeName by remember { mutableStateOf(true) }
    var includeType by remember { mutableStateOf(true) }
    var includeLocation by remember { mutableStateOf(true) }

    var format by remember { mutableStateOf(InventoryExporter.ExportFormat.CSV) }

    val currentFolder = folders.find { it.id == currentFolderId }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ElyzarethSurface)
                .border(1.dp, ElyzarethBorderHighlight, RoundedCornerShape(12.dp))
                .padding(20.dp)
                .testTag("inventory_export_dialog")
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        tint = ElyzarethAccent,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "EXPORT FILE INVENTORY",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ElyzarethTextPrimary
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = ElyzarethTextSecondary
                    )
                }
            }

            // Pro-tip banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(ElyzarethPrimary.copy(alpha = 0.12f))
                    .border(0.5.dp, ElyzarethPrimary.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "For your use case, Entire Library → CSV is probably the killer option.",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = ElyzarethPrimary
                )
            }

            // 1. Scope Section
            Text(
                text = "Scope",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ElyzarethTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ElyzarethSurfaceElevated)
                    .padding(vertical = 4.dp)
            ) {
                // Option 1: Current folder
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { scope = InventoryExporter.ExportScope.CURRENT_FOLDER }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("scope_current_folder")
                ) {
                    RadioButton(
                        selected = scope == InventoryExporter.ExportScope.CURRENT_FOLDER,
                        onClick = { scope = InventoryExporter.ExportScope.CURRENT_FOLDER },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = ElyzarethPrimary,
                            unselectedColor = ElyzarethTextMuted
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Current folder",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = ElyzarethTextPrimary
                        )
                        val folderLabel = if (currentFolder != null) {
                            val purpose = getPurposeForColor(currentFolder.colorHex)
                            val prefix = if (purpose != null) "${purpose.emoji} " else ""
                            "$prefix${currentFolder.name} (${currentFolder.fileCount} files)"
                        } else {
                            "All Folders active"
                        }
                        Text(
                            text = folderLabel,
                            fontSize = 10.sp,
                            color = ElyzarethTextSecondary
                        )
                    }
                }

                // Option 2: Selected folder
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { scope = InventoryExporter.ExportScope.SELECTED_FOLDER }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("scope_selected_folder")
                ) {
                    RadioButton(
                        selected = scope == InventoryExporter.ExportScope.SELECTED_FOLDER,
                        onClick = { scope = InventoryExporter.ExportScope.SELECTED_FOLDER },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = ElyzarethPrimary,
                            unselectedColor = ElyzarethTextMuted
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Selected folder",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ElyzarethTextPrimary
                    )
                }

                // Folder picker strip if Selected folder is active
                if (scope == InventoryExporter.ExportScope.SELECTED_FOLDER) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 48.dp, end = 12.dp, bottom = 8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        folders.forEach { f ->
                            val isChosen = chosenFolderId == f.id
                            val purpose = getPurposeForColor(f.colorHex)
                            val tagColor = parseColor(f.colorHex)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isChosen) tagColor.copy(alpha = 0.25f) else ElyzarethBackground)
                                    .border(
                                        width = if (isChosen) 1.5.dp else 0.5.dp,
                                        color = if (isChosen) tagColor else ElyzarethBorder,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .clickable { chosenFolderId = f.id }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    val emoji = purpose?.emoji ?: "📁"
                                    Text(text = emoji, fontSize = 10.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = f.name,
                                        fontSize = 10.sp,
                                        fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isChosen) ElyzarethTextPrimary else ElyzarethTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Option 3: Entire library (Default)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { scope = InventoryExporter.ExportScope.ENTIRE_LIBRARY }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("scope_entire_library")
                ) {
                    RadioButton(
                        selected = scope == InventoryExporter.ExportScope.ENTIRE_LIBRARY,
                        onClick = { scope = InventoryExporter.ExportScope.ENTIRE_LIBRARY },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = ElyzarethPrimary,
                            unselectedColor = ElyzarethTextMuted
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Entire library",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ElyzarethTextPrimary
                        )
                        Text(
                            text = "Exports all indexed files across all folders",
                            fontSize = 10.sp,
                            color = ElyzarethTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Include Section
            Text(
                text = "Include",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ElyzarethTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ElyzarethSurfaceElevated)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                IncludeCheckboxItem(
                    label = "File name",
                    checked = includeName,
                    onCheckedChange = { includeName = it },
                    tag = "include_file_name"
                )
                IncludeCheckboxItem(
                    label = "File type",
                    checked = includeType,
                    onCheckedChange = { includeType = it },
                    tag = "include_file_type"
                )
                IncludeCheckboxItem(
                    label = "File location",
                    checked = includeLocation,
                    onCheckedChange = { includeLocation = it },
                    tag = "include_file_location"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Format Section
            Text(
                text = "Format",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = ElyzarethTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(ElyzarethSurfaceElevated)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { format = InventoryExporter.ExportFormat.CSV }
                        .padding(vertical = 4.dp)
                        .testTag("format_csv")
                ) {
                    RadioButton(
                        selected = format == InventoryExporter.ExportFormat.CSV,
                        onClick = { format = InventoryExporter.ExportFormat.CSV },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = ElyzarethPrimary,
                            unselectedColor = ElyzarethTextMuted
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "CSV",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (format == InventoryExporter.ExportFormat.CSV) ElyzarethPrimary else ElyzarethTextSecondary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clickable { format = InventoryExporter.ExportFormat.TXT }
                        .padding(vertical = 4.dp)
                        .testTag("format_txt")
                ) {
                    RadioButton(
                        selected = format == InventoryExporter.ExportFormat.TXT,
                        onClick = { format = InventoryExporter.ExportFormat.TXT },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = ElyzarethPrimary,
                            unselectedColor = ElyzarethTextMuted
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TXT",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (format == InventoryExporter.ExportFormat.TXT) ElyzarethPrimary else ElyzarethTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // [ EXPORT ] Button
            Button(
                onClick = {
                    onExport(
                        scope,
                        chosenFolderId,
                        includeName,
                        includeType,
                        includeLocation,
                        format
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElyzarethAccent,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("confirm_export_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "[ EXPORT ]",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun IncludeCheckboxItem(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 2.dp)
            .testTag(tag)
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = ElyzarethAccent,
                checkmarkColor = Color.Black,
                uncheckedColor = ElyzarethBorder
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = ElyzarethTextPrimary
        )
    }
}
