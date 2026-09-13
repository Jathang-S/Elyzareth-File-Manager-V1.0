package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Icon
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
import com.example.model.FolderModel
import com.example.ui.theme.ElyzarethAccent
import com.example.ui.theme.ElyzarethBackground
import com.example.ui.theme.ElyzarethBorder
import com.example.ui.theme.ElyzarethPrimary
import com.example.ui.theme.ElyzarethSurfaceElevated
import com.example.ui.theme.ElyzarethTextMuted
import com.example.ui.theme.ElyzarethTextPrimary
import com.example.ui.theme.ElyzarethTextSecondary
import com.example.ui.theme.getPurposeForColor

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderColorStrip(
    folders: List<FolderModel>,
    selectedFolderId: Long?,
    onSelectFolder: (Long?) -> Unit,
    onOpenFolderColor: (FolderModel) -> Unit,
    onCreateFolderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(ElyzarethBackground)
            .padding(horizontal = 14.dp, vertical = 4.dp)
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // "ALL FILES" Chip
        val isAllSelected = selectedFolderId == null
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (isAllSelected) ElyzarethPrimary.copy(alpha = 0.2f) else ElyzarethSurfaceElevated)
                .border(
                    width = if (isAllSelected) 1.5.dp else 0.5.dp,
                    color = if (isAllSelected) ElyzarethPrimary else ElyzarethBorder,
                    shape = RoundedCornerShape(6.dp)
                )
                .combinedClickable(
                    onClick = { onSelectFolder(null) }
                )
                .padding(horizontal = 9.dp, vertical = 5.dp)
                .testTag("folder_chip_all")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = if (isAllSelected) ElyzarethPrimary else ElyzarethTextMuted,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "ALL",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isAllSelected) ElyzarethPrimary else ElyzarethTextPrimary
                )
            }
        }

        // Custom Colored Folder Chips (Color belongs to folder!)
        folders.forEach { folder ->
            val isSelected = selectedFolderId == folder.id
            val color = parseColor(folder.colorHex)
            val purpose = getPurposeForColor(folder.colorHex)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) color.copy(alpha = 0.22f) else ElyzarethSurfaceElevated)
                    .border(
                        width = if (isSelected) 1.5.dp else 0.5.dp,
                        color = if (isSelected) color else ElyzarethBorder,
                        shape = RoundedCornerShape(6.dp)
                    )
                .combinedClickable(
                    onClick = { onSelectFolder(folder.id) },
                    onLongClick = { onOpenFolderColor(folder) }
                )
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .testTag("folder_chip_${folder.id}")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (purpose != null) {
                        Text(text = purpose.emoji, fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                    } else {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = folder.name,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) ElyzarethTextPrimary else ElyzarethTextSecondary
                    )
                }
            }
        }

        // "+ NEW" Folder Quick Action
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(ElyzarethSurfaceElevated)
                .border(0.5.dp, ElyzarethBorder, RoundedCornerShape(6.dp))
                .combinedClickable(
                    onClick = onCreateFolderClick
                )
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .testTag("create_folder_chip")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "New Folder",
                    tint = ElyzarethAccent,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "NEW",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElyzarethAccent
                )
            }
        }
    }
}
