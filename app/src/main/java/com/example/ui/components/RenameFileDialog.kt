package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.FileItem
import com.example.ui.theme.ElyzarethBackground
import com.example.ui.theme.ElyzarethBorder
import com.example.ui.theme.ElyzarethBorderHighlight
import com.example.ui.theme.ElyzarethPrimary
import com.example.ui.theme.ElyzarethSurface
import com.example.ui.theme.ElyzarethTextMuted
import com.example.ui.theme.ElyzarethTextPrimary
import com.example.ui.theme.ElyzarethTextSecondary

@Composable
fun RenameFileDialog(
    file: FileItem,
    onDismiss: () -> Unit,
    onRename: (Long, String) -> Unit
) {
    var newName by remember { mutableStateOf(file.name) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ElyzarethSurface)
                .border(1.dp, ElyzarethBorderHighlight, RoundedCornerShape(12.dp))
                .padding(20.dp)
                .testTag("rename_file_dialog")
        ) {
            Text(
                text = "RENAME FILE",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 1.sp,
                color = ElyzarethTextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Extension (.${file.extension}) will be preserved",
                fontSize = 12.sp,
                color = ElyzarethTextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newName,
                onValueChange = { newName = it },
                label = { Text("File Name", color = ElyzarethTextSecondary) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = ElyzarethTextPrimary,
                    unfocusedTextColor = ElyzarethTextPrimary,
                    focusedBorderColor = ElyzarethPrimary,
                    unfocusedBorderColor = ElyzarethBorder,
                    focusedContainerColor = ElyzarethBackground,
                    unfocusedContainerColor = ElyzarethBackground
                ),
                trailingIcon = {
                    Text(
                        text = ".${file.extension}",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = ElyzarethTextSecondary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rename_input_field")
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ElyzarethTextSecondary),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("CANCEL", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = {
                        if (newName.isNotBlank()) {
                            onRename(file.id, newName.trim())
                        }
                    },
                    enabled = newName.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ElyzarethPrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.testTag("rename_confirm_button")
                ) {
                    Text("RENAME", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
