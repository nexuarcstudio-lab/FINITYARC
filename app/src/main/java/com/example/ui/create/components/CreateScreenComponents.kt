package com.example.ui.create.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExpenseStatus
import com.example.model.TransactionType
import com.example.model.WorkspaceType
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.abs

// Design Tokens for Minimalist Monochrome Architecture
val MonoBackground = Color(0xFFFFFFFF)
val MonoSurface = Color(0xFFF7F7F8)
val MonoTextPrimary = Color(0xFF000000)
val MonoBorder = Color(0xFFE5E5EA)
val MonoTextSubtle = Color(0xFF757575)

// =========================================================================
// 1. HERO AMOUNT DISPLAY
// =========================================================================

/**
 * Clean, centered currency display.
 * Shows formatted major and minor digits (e.g., "0.00") in large clean typography (42sp light/regular)
 * with a sleek "SAR" badge cleanly placed, and a smooth segmented pill toggle right above.
 */
@Composable
fun HeroAmountDisplay(
    rawMinorUnitsString: String,
    currencyCode: String,
    transactionType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    modifier: Modifier = Modifier
) {
    val minorUnits = rawMinorUnitsString.toLongOrNull() ?: 0L
    val isNegative = minorUnits < 0L
    val absoluteValue = abs(minorUnits)
    val major = absoluteValue / 100L
    val minorRemainder = absoluteValue % 100L

    val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ',' }
    val majorFormatted = DecimalFormat("#,##0", symbols).format(major)
    val minorFormatted = String.format(Locale.US, "%02d", minorRemainder)
    val displayDigits = (if (isNegative) "-" else "") + "$majorFormatted.$minorFormatted"

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hero_amount_display"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Subtle, sleek segmented pill toggle [Expense | Income | Transfer]
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, MonoBorder, RoundedCornerShape(24.dp)),
            color = MonoSurface
        ) {
            Row(
                modifier = Modifier.padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                TransactionType.values().forEach { type ->
                    val isSelected = type == transactionType
                    val bgColor by animateColorAsState(
                        targetValue = if (isSelected) MonoTextPrimary else Color.Transparent,
                        label = "type_bg"
                    )
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) MonoBackground else MonoTextSubtle,
                        label = "type_text"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(bgColor)
                            .clickable { onTypeSelected(type) }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("type_pill_${type.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = type.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                letterSpacing = 0.2.sp
                            ),
                            color = textColor
                        )
                    }
                }
            }
        }

        // Amount numbers and Currency Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text(
                text = displayDigits,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 42.sp,
                    fontWeight = FontWeight.Normal,
                    letterSpacing = (-0.5).sp
                ),
                color = MonoTextPrimary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(MonoSurface)
                    .border(1.dp, MonoBorder, RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = currencyCode,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MonoTextPrimary
                )
            }
        }
    }
}

// =========================================================================
// 2. CATEGORY PILL SELECTOR
// =========================================================================

/**
 * Horizontally scrollable row of rounded category chips.
 * Selected chip: Solid black fill with white text.
 * Unselected chip: #FFFFFF background with 1dp #E5E5EA border and black text.
 */
@Composable
fun CategoryPillSelector(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("category_pill_selector"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
        ) {
            items(categories, key = { it }) { category ->
                val isSelected = category == selectedCategory

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) MonoTextPrimary else MonoBackground)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) MonoTextPrimary else MonoBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable { onCategorySelected(category) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("category_chip_${category.replace(" ", "_").lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        ),
                        color = if (isSelected) MonoBackground else MonoTextPrimary
                    )
                }
            }
        }
    }
}

// =========================================================================
// 3. BUSINESS DETAILS CARD
// =========================================================================

/**
 * Card container with #F7F7F8 surface and 1dp border.
 * Only visible when activeWorkspace == WorkspaceType.BUSINESS.
 * Contains clean text inputs for "Spent By" and "Project / Department", plus status selector pills.
 */
@Composable
fun BusinessDetailsCard(
    activeWorkspace: WorkspaceType,
    spentBy: String,
    departmentOrProject: String,
    expenseStatus: ExpenseStatus,
    onSpentByChanged: (String) -> Unit,
    onDepartmentChanged: (String) -> Unit,
    onStatusSelected: (ExpenseStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = activeWorkspace == WorkspaceType.BUSINESS,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .testTag("business_details_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MonoSurface),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MonoBorder)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "BUSINESS ATTRIBUTION",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    ),
                    color = MonoTextSubtle
                )

                // Spent By Field
                OutlinedTextField(
                    value = spentBy,
                    onValueChange = onSpentByChanged,
                    label = { Text("Spent By (e.g. Employee Name)") },
                    placeholder = { Text("Sarah Lead", color = MonoTextSubtle) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MonoBackground,
                        unfocusedContainerColor = MonoBackground,
                        focusedBorderColor = MonoTextPrimary,
                        unfocusedBorderColor = MonoBorder,
                        focusedTextColor = MonoTextPrimary,
                        unfocusedTextColor = MonoTextPrimary,
                        focusedLabelColor = MonoTextPrimary,
                        unfocusedLabelColor = MonoTextSubtle
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("spent_by_input")
                )

                // Project / Department Field
                OutlinedTextField(
                    value = departmentOrProject,
                    onValueChange = onDepartmentChanged,
                    label = { Text("Project / Department") },
                    placeholder = { Text("Core Infrastructure", color = MonoTextSubtle) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MonoBackground,
                        unfocusedContainerColor = MonoBackground,
                        focusedBorderColor = MonoTextPrimary,
                        unfocusedBorderColor = MonoBorder,
                        focusedTextColor = MonoTextPrimary,
                        unfocusedTextColor = MonoTextPrimary,
                        focusedLabelColor = MonoTextPrimary,
                        unfocusedLabelColor = MonoTextSubtle
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("department_input")
                )

                // Status Selector Pills [PAID | PENDING | REIMBURSABLE]
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Settlement Status",
                        style = MaterialTheme.typography.labelSmall,
                        color = MonoTextSubtle
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExpenseStatus.values().forEach { status ->
                            val isSelected = status == expenseStatus
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) MonoTextPrimary else MonoBackground)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MonoTextPrimary else MonoBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onStatusSelected(status) }
                                    .padding(vertical = 10.dp)
                                    .testTag("status_pill_${status.name.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = status.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) MonoBackground else MonoTextPrimary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 4. MODERN TACTILE KEYPAD
// =========================================================================

/**
 * 12-key grid (1-9, C, 0, Backspace).
 * Designed for one-handed thumb speed: large touch targets (48dp-52dp height per key),
 * clean typography with subtle touch ripple, no heavy wireframe borders.
 */
@Composable
fun ModernTactileKeypad(
    onDigitPressed: (Char) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier
) {
    val keypadRows = listOf(
        listOf(KeypadAction.Digit('1'), KeypadAction.Digit('2'), KeypadAction.Digit('3')),
        listOf(KeypadAction.Digit('4'), KeypadAction.Digit('5'), KeypadAction.Digit('6')),
        listOf(KeypadAction.Digit('7'), KeypadAction.Digit('8'), KeypadAction.Digit('9')),
        listOf(KeypadAction.Clear, KeypadAction.Digit('0'), KeypadAction.Backspace)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("modern_tactile_keypad"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keypadRows.forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowKeys.forEach { action ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(color = MonoBorder, bounded = true)
                            ) {
                                when (action) {
                                    is KeypadAction.Digit -> onDigitPressed(action.char)
                                    is KeypadAction.Clear -> onClear()
                                    is KeypadAction.Backspace -> onBackspace()
                                }
                            }
                            .testTag(
                                when (action) {
                                    is KeypadAction.Digit -> "keypad_${action.char}"
                                    is KeypadAction.Clear -> "keypad_clear"
                                    is KeypadAction.Backspace -> "keypad_backspace"
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        when (action) {
                            is KeypadAction.Digit -> {
                                Text(
                                    text = action.char.toString(),
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Medium,
                                        fontFamily = FontFamily.SansSerif
                                    ),
                                    color = MonoTextPrimary
                                )
                            }
                            is KeypadAction.Clear -> {
                                Text(
                                    text = "C",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = MonoTextSubtle
                                )
                            }
                            is KeypadAction.Backspace -> {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                    contentDescription = "Backspace",
                                    tint = MonoTextPrimary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private sealed interface KeypadAction {
    data class Digit(val char: Char) : KeypadAction
    object Clear : KeypadAction
    object Backspace : KeypadAction
}
