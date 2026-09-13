package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Elyzareth Modern Dark File Manager Palette
val ElyzarethBackground = Color(0xFF0B0F17)
val ElyzarethSurface = Color(0xFF131B26)
val ElyzarethSurfaceVariant = Color(0xFF1A2433)
val ElyzarethSurfaceElevated = Color(0xFF222E40)
val ElyzarethBorder = Color(0xFF2E3D52)
val ElyzarethBorderHighlight = Color(0xFF3F5370)

val ElyzarethPrimary = Color(0xFF3B82F6) // Electric Blue
val ElyzarethAccent = Color(0xFF10B981) // Emerald Green
val ElyzarethWarning = Color(0xFFF59E0B) // Amber
val ElyzarethDanger = Color(0xFFEF4444) // Coral Red
val ElyzarethAlertRed = ElyzarethDanger
val ElyzarethPurple = Color(0xFFA855F7) // Royal Purple

val ElyzarethTextPrimary = Color(0xFFF8FAFC)
val ElyzarethTextSecondary = Color(0xFF94A3B8)
val ElyzarethTextMuted = Color(0xFF64748B)

// Aliases for compatibility
val WinampBackground = ElyzarethBackground
val WinampSurface = ElyzarethSurface
val WinampSurfaceVariant = ElyzarethSurfaceVariant
val WinampSurfaceElevated = ElyzarethSurfaceElevated
val WinampBorder = ElyzarethBorder
val WinampBorderHighlight = ElyzarethBorderHighlight

val WinampNeonGreen = ElyzarethAccent
val WinampElectricCyan = ElyzarethPrimary
val WinampNeonAmber = ElyzarethWarning
val WinampHotPink = Color(0xFFEC4899)
val WinampAlertRed = ElyzarethDanger
val WinampPurple = ElyzarethPurple

val WinampTextPrimary = ElyzarethTextPrimary
val WinampTextSecondary = ElyzarethTextSecondary
val WinampTextMuted = ElyzarethTextMuted

/**
 * Purposeful Folder Coloring:
 * Colors define functional folder states and roles, not individual files.
 */
data class PurposefulFolderColor(
    val hex: String,
    val name: String,
    val emoji: String,
    val description: String
)

val PurposefulFolderColors = listOf(
    PurposefulFolderColor(
        hex = "#2563EB",
        name = "Music",
        emoji = "🔵",
        description = "Audio tracks, masters & stems"
    ),
    PurposefulFolderColor(
        hex = "#9333EA",
        name = "AI generations",
        emoji = "🟣",
        description = "AI outputs, synth audio & generated art"
    ),
    PurposefulFolderColor(
        hex = "#16A34A",
        name = "Selected / approved",
        emoji = "🟢",
        description = "Reviewed, selected & approved assets"
    ),
    PurposefulFolderColor(
        hex = "#EA580C",
        name = "Work in progress",
        emoji = "🟠",
        description = "Active working drafts & in-progress edits"
    ),
    PurposefulFolderColor(
        hex = "#DC2626",
        name = "Reject/archive",
        emoji = "🔴",
        description = "Rejected items, backups & cold archive"
    ),
    PurposefulFolderColor(
        hex = "#EAB308",
        name = "Export",
        emoji = "🟡",
        description = "Staged bundles ready for distribution"
    )
)

fun getPurposeForColor(hex: String): PurposefulFolderColor? {
    return PurposefulFolderColors.firstOrNull { it.hex.equals(hex, ignoreCase = true) }
}

// Legacy pair list for backwards compatibility
val FolderColorPresets = PurposefulFolderColors.map {
    it.hex to "${it.emoji} ${it.name}"
}

