package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.FileItem
import com.example.ui.theme.WinampBackground
import com.example.ui.theme.WinampBorder
import com.example.ui.theme.WinampElectricCyan
import com.example.ui.theme.WinampNeonAmber
import com.example.ui.theme.WinampNeonGreen
import com.example.ui.theme.WinampSurfaceElevated
import com.example.ui.theme.WinampTextMuted
import com.example.ui.theme.WinampTextPrimary
import java.io.File

@Composable
fun PhotoPreviewDialog(
    photo: FileItem,
    allPhotos: List<FileItem>,
    onDismiss: () -> Unit,
    onNavigateNext: () -> Unit,
    onNavigatePrevious: () -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .testTag("photo_preview_dialog")
        ) {
            // Main Zoomable Photo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(photo.id) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            scale = (scale * zoom).coerceIn(1f, 5f)
                            if (scale > 1f) {
                                offsetX += pan.x
                                offsetY += pan.y
                            } else {
                                offsetX = 0f
                                offsetY = 0f
                            }
                        }
                    }
            ) {
                AsyncImage(
                    model = if (photo.filePath.isNotBlank()) File(photo.filePath) else null,
                    contentDescription = photo.fullName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offsetX,
                            translationY = offsetY
                        )
                )
            }

            // Top Control Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xCC080C10))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter)
            ) {
                Column {
                    Text(
                        text = photo.fullName,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = WinampNeonGreen,
                        maxLines = 1
                    )
                    Text(
                        text = "DIMENSIONS: ${photo.dimensions ?: "Unknown"} • ${photo.formattedSize}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = WinampElectricCyan
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (scale > 1f) {
                        IconButton(
                            onClick = {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Reset Zoom",
                                tint = WinampNeonAmber
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("photo_preview_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }
            }

            // Navigation Arrows (Left / Right)
            if (allPhotos.size > 1) {
                IconButton(
                    onClick = onNavigatePrevious,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 12.dp)
                        .size(44.dp)
                        .background(Color(0x88000000), CircleShape)
                        .testTag("photo_prev_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Image",
                        tint = WinampElectricCyan
                    )
                }

                IconButton(
                    onClick = onNavigateNext,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 12.dp)
                        .size(44.dp)
                        .background(Color(0x88000000), CircleShape)
                        .testTag("photo_next_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Image",
                        tint = WinampElectricCyan
                    )
                }
            }

            // Bottom Full Metadata Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xE6080C10))
                    .border(0.5.dp, WinampBorder)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .align(Alignment.BottomCenter)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "LOCATION: ${photo.folderName}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = WinampTextPrimary
                    )
                    Text(
                        text = "PINCH TO ZOOM (${String.format("%.1f", scale)}x)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = WinampTextMuted
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "MODIFIED: ${photo.formattedDate}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = WinampTextMuted
                    )
                    Text(
                        text = "PATH: ${photo.filePath.takeLast(30)}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = WinampTextMuted
                    )
                }
            }
        }
    }
}
