package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.ui.theme.PurposefulFolderColors
import com.example.ui.theme.getPurposeForColor

fun parseColor(hex: String, fallback: Color = ElyzarethAccent): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16)
        if (clean.length == 6) {
            Color(colorInt or 0xFF000000)
        } else {
            Color(colorInt)
        }
    } catch (_: Exception) {
        fallback
    }
}

@Composable
fun FolderColorDialog(
    folder: FolderModel,
    onDismiss: () -> Unit,
    onColorSelected: (String) -> Unit
) {
    var selectedColorHex by remember(folder.colorHex) { mutableStateOf(folder.colorHex) }
    val activePurpose = getPurposeForColor(selectedColorHex)

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("folder_color_dialog"),
        containerColor = ElyzarethSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = parseColor(selectedColorHex),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "FOLDER COLOR & ROLE",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = ElyzarethTextPrimary
                    )
                    Text(
                        text = "Color belongs to the folder, not individual files.",
                        fontSize = 11.sp,
                        color = ElyzarethTextSecondary
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Target folder card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElyzarethSurfaceElevated)
                        .border(1.dp, parseColor(selectedColorHex).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(parseColor(selectedColorHex))
                                .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = folder.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = ElyzarethTextPrimary
                            )
                            val purposeLabel = activePurpose?.let { "${it.emoji} ${it.name} • ${it.description}" }
                                ?: "Custom Color: $selectedColorHex"
                            Text(
                                text = purposeLabel,
                                fontSize = 11.sp,
                                color = parseColor(selectedColorHex)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "PURPOSEFUL ROLES:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElyzarethTextMuted,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                // List of Purposeful Colors
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PurposefulFolderColors.forEach { purpose ->
                        val isSelected = selectedColorHex.equals(purpose.hex, ignoreCase = true)
                        val roleColor = parseColor(purpose.hex)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) roleColor.copy(alpha = 0.2f) else ElyzarethSurfaceElevated)
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) roleColor else ElyzarethBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedColorHex = purpose.hex }
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                                .testTag("color_preset_${purpose.hex}")
                        ) {
                            Text(text = purpose.emoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = purpose.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp,
                                    color = if (isSelected) ElyzarethTextPrimary else ElyzarethTextSecondary
                                )
                                Text(
                                    text = purpose.description,
                                    fontSize = 10.sp,
                                    color = ElyzarethTextMuted
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = roleColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onColorSelected(selectedColorHex) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElyzarethAccent,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("apply_folder_color_button")
            ) {
                Text(
                    text = "APPLY TO FOLDER",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "CANCEL",
                    fontFamily = FontFamily.Monospace,
                    color = ElyzarethTextMuted
                )
            }
        }
    )
}

@Composable
fun CreateFolderDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }
    var selectedColorHex by remember { mutableStateOf("#2563EB") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("create_folder_dialog"),
        containerColor = ElyzarethSurface,
        title = {
            Column {
                Text(
                    text = "CREATE NEW FOLDER",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = ElyzarethTextPrimary
                )
                Text(
                    text = "Folder color applies to all contained files",
                    fontSize = 11.sp,
                    color = ElyzarethTextSecondary
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("Folder Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElyzarethPrimary,
                        unfocusedBorderColor = ElyzarethBorder,
                        focusedTextColor = ElyzarethTextPrimary,
                        unfocusedTextColor = ElyzarethTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_folder_name_input")
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "ASSIGN ROLE / COLOR:",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElyzarethTextMuted,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PurposefulFolderColors.forEach { purpose ->
                        val isSelected = selectedColorHex.equals(purpose.hex, ignoreCase = true)
                        val roleColor = parseColor(purpose.hex)

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) roleColor.copy(alpha = 0.2f) else ElyzarethSurfaceElevated)
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) roleColor else ElyzarethBorder,
                                    shape = RoundedCornerShape(6.dp)
                                )
                                .clickable {
                                    selectedColorHex = purpose.hex
                                    if (folderName.isBlank()) {
                                        folderName = purpose.name
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(text = purpose.emoji, fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = purpose.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) ElyzarethTextPrimary else ElyzarethTextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = roleColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (folderName.isNotBlank()) {
                        onCreate(folderName.trim(), selectedColorHex)
                    }
                },
                enabled = folderName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElyzarethAccent,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.testTag("submit_create_folder_button")
            ) {
                Text(
                    text = "CREATE FOLDER",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", fontFamily = FontFamily.Monospace, color = ElyzarethTextMuted)
            }
        }
    )
}
