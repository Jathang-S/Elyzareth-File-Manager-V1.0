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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MediaType
import com.example.ui.theme.ElyzarethAccent
import com.example.ui.theme.ElyzarethBackground
import com.example.ui.theme.ElyzarethBorder
import com.example.ui.theme.ElyzarethPrimary
import com.example.ui.theme.ElyzarethSurface
import com.example.ui.theme.ElyzarethSurfaceElevated
import com.example.ui.theme.ElyzarethTextMuted
import com.example.ui.theme.ElyzarethTextPrimary
import com.example.ui.theme.ElyzarethTextSecondary

@Composable
fun SearchBarAndFilterChips(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedMediaType: MediaType?,
    onSelectMediaType: (MediaType?) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ElyzarethBackground)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        // Search Bar: 🔍 Search files...
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "Search files...",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = ElyzarethTextMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = if (searchQuery.isNotEmpty()) ElyzarethPrimary else ElyzarethTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { onSearchQueryChange("") },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear Search",
                            tint = ElyzarethTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            singleLine = true,
            textStyle = TextStyle(
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                color = ElyzarethTextPrimary
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = ElyzarethSurface,
                unfocusedContainerColor = ElyzarethSurface,
                focusedBorderColor = ElyzarethPrimary,
                unfocusedBorderColor = ElyzarethBorder,
                cursorColor = ElyzarethPrimary
            ),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
                .testTag("search_bar_input")
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Filter chips: ALL   🎵 AUDIO   🖼 PHOTO   🎬 VIDEO   📄 DOCS
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChipItem(
                label = "ALL",
                isSelected = selectedMediaType == null,
                accentColor = ElyzarethPrimary,
                onClick = { onSelectMediaType(null) },
                testTag = "filter_chip_all"
            )

            FilterChipItem(
                label = "🎵 AUDIO",
                isSelected = selectedMediaType == MediaType.AUDIO,
                accentColor = ElyzarethPrimary,
                onClick = { onSelectMediaType(MediaType.AUDIO) },
                testTag = "filter_chip_audio"
            )

            FilterChipItem(
                label = "🖼 PHOTO",
                isSelected = selectedMediaType == MediaType.IMAGE,
                accentColor = ElyzarethAccent,
                onClick = { onSelectMediaType(MediaType.IMAGE) },
                testTag = "filter_chip_image"
            )

            FilterChipItem(
                label = "🎬 VIDEO",
                isSelected = selectedMediaType == MediaType.VIDEO,
                accentColor = Color(0xFFF59E0B),
                onClick = { onSelectMediaType(MediaType.VIDEO) },
                testTag = "filter_chip_video"
            )

            FilterChipItem(
                label = "📄 DOCS",
                isSelected = selectedMediaType == MediaType.DOCUMENT,
                accentColor = Color(0xFFA855F7),
                onClick = { onSelectMediaType(MediaType.DOCUMENT) },
                testTag = "filter_chip_doc"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = ElyzarethBorder, thickness = 0.5.dp)
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) accentColor.copy(alpha = 0.2f) else ElyzarethSurfaceElevated)
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) accentColor else ElyzarethBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 5.dp)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            fontSize = 11.sp,
            color = if (isSelected) accentColor else ElyzarethTextSecondary
        )
    }
}
