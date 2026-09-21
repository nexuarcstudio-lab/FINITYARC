package com.example.ui.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MyApplicationTheme

// Design Tokens for Minimalist Monochrome Architecture
val SettingsMonoBackground = Color(0xFFFFFFFF)
val SettingsMonoSurface = Color(0xFFF7F7F8)
val SettingsMonoTextPrimary = Color(0xFF000000)
val SettingsMonoTextSecondary = Color(0xFF757575)
val SettingsMonoBorder = Color(0xFFE5E5EA)
val SettingsMonoDivider = Color(0xFFE5E5EA)

// =========================================================================
// 1. SETTINGS SECTION CONTAINER
// =========================================================================

/**
 * Wraps a group of settings items inside a clean card with a #F7F7F8 surface and 1dp border.
 * Includes a section header label (e.g. "WORKSPACE & PREFERENCES", "DATA MANAGEMENT", "SECURITY")
 * in bold, uppercase, small monospace typography (10sp).
 */
@Composable
fun SettingsSectionContainer(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("settings_section_${title.replace(" ", "_").lowercase()}"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp
            ),
            color = SettingsMonoTextSecondary,
            modifier = Modifier.padding(start = 4.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, SettingsMonoBorder, RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = SettingsMonoSurface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                content()
            }
        }
    }
}

// =========================================================================
// 2. SETTINGS ACTION ROW
// =========================================================================

/**
 * A clickable row for navigation/action items (e.g., "Export Data to CSV", "Clear All Data").
 * Displays a clean vector icon placeholder, title, optional subtitle, and an arrow indicator (ChevronRight).
 * For destructive actions (like "Clear All Data"), allows styling the text with bold high-contrast emphasis.
 */
@Composable
fun SettingsActionRow(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    isDestructive: Boolean = false,
    showChevron: Boolean = true,
    showDivider: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = SettingsMonoBorder, bounded = true),
                    onClick = onClick
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("settings_action_${title.replace(" ", "_").lowercase()}"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDestructive) SettingsMonoTextPrimary else SettingsMonoBackground)
                            .border(1.dp, SettingsMonoBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isDestructive) SettingsMonoBackground else SettingsMonoTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                }

                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isDestructive) FontWeight.Bold else FontWeight.SemiBold,
                            letterSpacing = if (isDestructive) 0.3.sp else 0.sp
                        ),
                        color = SettingsMonoTextPrimary
                    )

                    if (!subtitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = SettingsMonoTextSecondary
                        )
                    }
                }
            }

            if (showChevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open",
                    tint = SettingsMonoTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = SettingsMonoDivider
            )
        }
    }
}

// =========================================================================
// 3. SETTINGS TOGGLE ROW
// =========================================================================

/**
 * Row with title, description, and a minimalist Switch.
 * The switch follows a monochrome theme (black thumb/track when checked, light gray when unchecked).
 */
@Composable
fun SettingsToggleRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    showDivider: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = SettingsMonoBorder, bounded = true),
                    onClick = { onCheckedChange(!checked) }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .testTag("settings_toggle_${title.replace(" ", "_").lowercase()}"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SettingsMonoBackground)
                            .border(1.dp, SettingsMonoBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = SettingsMonoTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                }

                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = SettingsMonoTextPrimary
                    )

                    if (!subtitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = SettingsMonoTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Minimalist Monochrome Switch
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = SettingsMonoBackground,
                    checkedTrackColor = SettingsMonoTextPrimary,
                    checkedBorderColor = SettingsMonoTextPrimary,
                    uncheckedThumbColor = SettingsMonoTextSecondary,
                    uncheckedTrackColor = SettingsMonoBackground,
                    uncheckedBorderColor = SettingsMonoBorder
                ),
                modifier = Modifier.testTag("switch_${title.replace(" ", "_").lowercase()}")
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = SettingsMonoDivider
            )
        }
    }
}

// =========================================================================
// 4. SETTINGS SELECTOR ROW
// =========================================================================

/**
 * Row showing the active selection (e.g., "Currency -> SAR", "Mode -> Personal")
 * with a clean dropdown trigger and monochrome styling.
 */
@Composable
fun SettingsSelectorRow(
    title: String,
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    showDivider: Boolean = true
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = SettingsMonoBorder, bounded = true),
                    onClick = { expanded = true }
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("settings_selector_${title.replace(" ", "_").lowercase()}"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SettingsMonoBackground)
                            .border(1.dp, SettingsMonoBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = SettingsMonoTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                }

                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = SettingsMonoTextPrimary
                    )

                    if (!subtitle.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = SettingsMonoTextSecondary
                        )
                    }
                }
            }

            Box {
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, SettingsMonoBorder, RoundedCornerShape(8.dp)),
                    color = SettingsMonoBackground
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = selectedValue,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = SettingsMonoTextPrimary
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Expand Options",
                            tint = SettingsMonoTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier
                        .background(SettingsMonoBackground)
                        .border(1.dp, SettingsMonoBorder, RoundedCornerShape(10.dp))
                ) {
                    options.forEach { option ->
                        val isSelected = option == selectedValue
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = option,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = SettingsMonoTextPrimary
                                    )
                                    if (isSelected) {
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = SettingsMonoTextPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            },
                            modifier = Modifier.testTag("option_${option.lowercase()}")
                        )
                    }
                }
            }
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = SettingsMonoDivider
            )
        }
    }
}

// =========================================================================
// PREVIEWS
// =========================================================================

@Preview(showBackground = true, name = "Settings Components Preview")
@Composable
fun SettingsComponentsPreview() {
    MyApplicationTheme {
        Column(
            modifier = Modifier
                .background(SettingsMonoBackground)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: Workspace & Preferences
            SettingsSectionContainer(title = "Workspace & Preferences") {
                SettingsSelectorRow(
                    title = "Active Workspace",
                    subtitle = "Switch between personal ledger and corporate team expenses",
                    selectedValue = "Personal",
                    options = listOf("Personal", "Business"),
                    onOptionSelected = {},
                    icon = Icons.Default.WorkOutline
                )
                SettingsSelectorRow(
                    title = "Ledger Currency",
                    subtitle = "Base display currency for calculations",
                    selectedValue = "SAR",
                    options = listOf("SAR", "USD", "EUR", "AED", "GBP"),
                    onOptionSelected = {},
                    icon = Icons.Default.Payments,
                    showDivider = false
                )
            }

            // Section 2: Security
            SettingsSectionContainer(title = "Security") {
                SettingsToggleRow(
                    title = "Biometric Passcode Lock",
                    subtitle = "Require fingerprint or face authentication on entry",
                    checked = true,
                    onCheckedChange = {},
                    icon = Icons.Default.Fingerprint,
                    showDivider = false
                )
            }

            // Section 3: Data Management
            SettingsSectionContainer(title = "Data Management") {
                SettingsActionRow(
                    title = "Export Data to CSV",
                    subtitle = "Generate RFC 4180 offline backup file",
                    onClick = {},
                    icon = Icons.Default.Download
                )
                SettingsActionRow(
                    title = "Import Transactions from CSV",
                    subtitle = "Restore offline ledger from file",
                    onClick = {},
                    icon = Icons.Default.Upload
                )
                SettingsActionRow(
                    title = "Clear All Stored Data",
                    subtitle = "Permanently wipe all transactions from this device",
                    onClick = {},
                    icon = Icons.Default.DeleteForever,
                    isDestructive = true,
                    showChevron = false,
                    showDivider = false
                )
            }
        }
    }
}
