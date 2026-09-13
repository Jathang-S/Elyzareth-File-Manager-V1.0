package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FileItem
import com.example.ui.theme.ElyzarethAccent
import com.example.ui.theme.ElyzarethBackground
import com.example.ui.theme.ElyzarethBorder
import com.example.ui.theme.ElyzarethPrimary
import com.example.ui.theme.ElyzarethSurface
import com.example.ui.theme.ElyzarethSurfaceElevated
import com.example.ui.theme.ElyzarethTextMuted
import com.example.ui.theme.ElyzarethTextPrimary
import com.example.ui.theme.ElyzarethTextSecondary
import kotlin.math.roundToInt

@Composable
fun ElyzarethFileListView(
    files: List<FileItem>,
    selectedFileId: Long?,
    onFileClick: (FileItem) -> Unit,
    onInstantDelete: (Long) -> Unit,
    onReorder: (fromIndex: Int, toIndex: Int) -> Unit,
    onAddFileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val itemHeightPx = with(density) { 56.dp.toPx() }

    var draggedIndex by remember { mutableStateOf<Int?>(null) }
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    var initialDragIndex by remember { mutableIntStateOf(0) }

    if (files.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .fillMaxSize()
                .background(ElyzarethBackground)
                .padding(24.dp)
                .testTag("empty_playlist_view")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ElyzarethSurface)
                    .border(1.dp, ElyzarethBorder, RoundedCornerShape(12.dp))
                    .padding(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = null,
                    tint = ElyzarethPrimary,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "NO FILES IN THIS VIEW",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = ElyzarethTextPrimary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Add assets or switch to another folder using the color bar above.",
                    fontSize = 12.sp,
                    color = ElyzarethTextSecondary
                )
                Spacer(modifier = Modifier.height(18.dp))
                Button(
                    onClick = onAddFileClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElyzarethAccent,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("empty_add_file_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ADD FILE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    } else {
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .background(ElyzarethBackground)
                .testTag("winamp_playlist_list"),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            itemsIndexed(
                items = files,
                key = { _, item -> item.id }
            ) { index, file ->
                val isBeingDragged = draggedIndex == index

                val itemModifier = if (isBeingDragged) {
                    Modifier.offset { IntOffset(0, dragOffsetY.roundToInt()) }
                } else {
                    Modifier
                }

                ElyzarethFileListItem(
                    index = index,
                    file = file,
                    isSelected = file.id == selectedFileId,
                    isDragActive = isBeingDragged,
                    onClick = { onFileClick(file) },
                    onInstantDelete = { onInstantDelete(file.id) },
                    onDragStart = {
                        draggedIndex = index
                        initialDragIndex = index
                        dragOffsetY = 0f
                    },
                    onDragDelta = { deltaY ->
                        dragOffsetY += deltaY
                        val currentIdx = draggedIndex ?: return@ElyzarethFileListItem
                        val offsetSlots = (dragOffsetY / itemHeightPx).toInt()
                        val targetIdx = (currentIdx + offsetSlots).coerceIn(0, files.lastIndex)
                        if (targetIdx != currentIdx) {
                            onReorder(currentIdx, targetIdx)
                            draggedIndex = targetIdx
                            dragOffsetY -= (targetIdx - currentIdx) * itemHeightPx
                        }
                    },
                    onDragEnd = {
                        draggedIndex = null
                        dragOffsetY = 0f
                    },
                    modifier = itemModifier
                )
            }

            // Footer hint
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElyzarethSurfaceElevated)
                        .border(0.5.dp, ElyzarethBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TIP: Tap file to inspect & move • Drag (::) to reorder • (X) to delete with instant Undo",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = ElyzarethTextMuted
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
