package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.model.FileItem
import com.example.ui.theme.ElyzarethAccent
import com.example.ui.theme.ElyzarethAlertRed
import com.example.ui.theme.ElyzarethBackground
import com.example.ui.theme.ElyzarethBorder
import com.example.ui.theme.ElyzarethPrimary
import com.example.ui.theme.ElyzarethSurface
import com.example.ui.theme.ElyzarethSurfaceElevated
import com.example.ui.theme.ElyzarethTextMuted
import com.example.ui.theme.ElyzarethTextPrimary
import com.example.ui.theme.ElyzarethTextSecondary
import com.example.ui.theme.getPurposeForColor
import java.util.Locale

@Composable
fun ElyzarethFileListItem(
    index: Int,
    file: FileItem,
    isSelected: Boolean,
    isDragActive: Boolean,
    onClick: () -> Unit,
    onInstantDelete: () -> Unit,
    onDragStart: () -> Unit,
    onDragDelta: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val folderColor = parseColor(file.folderColorHex)
    val folderPurpose = getPurposeForColor(file.folderColorHex)

    val scale by animateFloatAsState(
        targetValue = if (isDragActive) 1.02f else 1f,
        label = "item_scale"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .zIndex(if (isDragActive) 10f else 1f)
            .shadow(
                elevation = if (isDragActive) 8.dp else 0.dp,
                shape = RoundedCornerShape(8.dp)
            )
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isDragActive -> ElyzarethSurfaceElevated
                    isSelected -> ElyzarethPrimary.copy(alpha = 0.12f)
                    index % 2 == 0 -> ElyzarethSurface
                    else -> ElyzarethBackground
                }
            )
            .border(
                width = if (isDragActive) 1.5.dp else if (isSelected) 1.dp else 0.5.dp,
                color = when {
                    isDragActive -> ElyzarethAccent
                    isSelected -> ElyzarethPrimary
                    else -> ElyzarethBorder
                },
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("file_item_${file.id}")
    ) {
        // Purposeful folder strip on the left edge
        Box(
            modifier = Modifier
                .width(3.5.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(folderColor)
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Index: 01.
        Text(
            text = String.format(Locale.US, "%02d.", index + 1),
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = ElyzarethTextMuted,
            modifier = Modifier.width(26.dp)
        )

        // Ext badge: [WAV]
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(3.dp))
                .background(ElyzarethSurfaceElevated)
                .border(0.5.dp, ElyzarethBorder, RoundedCornerShape(3.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = file.extension.uppercase(),
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = ElyzarethTextSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // HERO FILE INFORMATION: Filename gets priority!
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Filename: Llama_Whippin_Intro
            Text(
                text = file.name,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = ElyzarethTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Subtitle: ● Music (purposeful folder tag)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(folderColor)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = file.folderName,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    color = ElyzarethTextMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Metrics: Duration (0:06) or File Size
        Text(
            text = file.formattedDuration ?: file.formattedSize,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = if (file.formattedDuration != null) ElyzarethPrimary else ElyzarethTextSecondary
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Deliberate × Delete Button (Well-bounded to avoid accidental taps)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(ElyzarethSurfaceElevated)
                .border(0.5.dp, ElyzarethAlertRed.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                .clickable { onInstantDelete() }
                .testTag("quick_delete_${file.id}")
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Instant Delete",
                tint = ElyzarethAlertRed,
                modifier = Modifier.size(15.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Drag Handle (≡) for reordering
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isDragActive) ElyzarethAccent.copy(alpha = 0.2f) else Color.Transparent)
                .pointerInput(file.id) {
                    detectDragGesturesAfterLongPress(
                        onDragStart = { onDragStart() },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onDragDelta(dragAmount.y)
                        },
                        onDragEnd = { onDragEnd() },
                        onDragCancel = { onDragEnd() }
                    )
                }
                .testTag("drag_handle_${file.id}")
        ) {
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag to reorder",
                tint = if (isDragActive) ElyzarethAccent else ElyzarethTextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
