package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileItem
import com.example.model.FolderModel
import com.example.ui.theme.ElyzarethBackground
import com.example.ui.theme.ElyzarethBorder
import com.example.ui.theme.ElyzarethPrimary
import com.example.ui.theme.ElyzarethSurface
import com.example.ui.theme.ElyzarethSurfaceElevated
import com.example.ui.theme.ElyzarethSurfaceVariant
import com.example.ui.theme.ElyzarethTextMuted
import com.example.ui.theme.ElyzarethTextPrimary
import com.example.ui.theme.ElyzarethTextSecondary
import com.example.ui.theme.getPurposeForColor

@Composable
fun FoldersScreen(
    folders: List<FolderModel>,
    allFiles: List<FileItem>,
    selectedFolderId: Long?,
    onSelectFolderAndOpenLibrary: (Long) -> Unit,
    onOpenFolderColor: (FolderModel) -> Unit,
    onCreateFolderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalLibraryFiles = allFiles.size
    val totalLibraryBytes = allFiles.sumOf { it.sizeBytes }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ElyzarethBackground)
            .testTag("folders_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElyzarethSurface)
                .border(0.5.dp, ElyzarethBorder)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "FOLDERS",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp,
                    color = ElyzarethTextPrimary
                )
                Text(
                    text = "$totalLibraryFiles files • ${FileItem.formatFileSize(totalLibraryBytes)} total",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = ElyzarethTextMuted
                )
            }

            Button(
                onClick = onCreateFolderClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElyzarethSurfaceElevated,
                    contentColor = ElyzarethPrimary
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier
                    .border(1.dp, ElyzarethPrimary.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                    .testTag("add_folder_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "NEW", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(folders, key = { it.id }) { folder ->
                val folderFiles = allFiles.filter { it.folderId == folder.id }
                val folderBytes = folderFiles.sumOf { it.sizeBytes }
                val color = parseColor(folder.colorHex)
                val purpose = getPurposeForColor(folder.colorHex)
                val isSelected = folder.id == selectedFolderId

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElyzarethSurfaceVariant)
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) color else ElyzarethBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectFolderAndOpenLibrary(folder.id) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Purposeful Color Strip / Badge
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(color)
                    )

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = folder.name,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = ElyzarethTextPrimary
                            )
                            if (purpose != null) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${purpose.emoji} ${purpose.name}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = color
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = "${folderFiles.size} files • ${FileItem.formatFileSize(folderBytes)}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = ElyzarethTextSecondary
                        )
                    }

                    // Color customization button
                    IconButton(
                        onClick = { onOpenFolderColor(folder) },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(ElyzarethSurfaceElevated)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Edit Color",
                            tint = color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
