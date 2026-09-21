package com.example.ui.state

import com.example.model.ExpenseStatus
import com.example.model.TransactionType
import com.example.model.WorkspaceType
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

/**
 * Immutable state model for FINITYARC's Create Transaction Screen.
 *
 * Adheres strictly to immutable Compose state requirements, modeling monetary amounts
 * through an integer digit buffer [rawMinorUnitsString] to avoid all floating-point rounding errors.
 */
data class CreateUiState(
    val activeWorkspace: WorkspaceType = WorkspaceType.PERSONAL,
    val transactionType: TransactionType = TransactionType.EXPENSE,
    val rawMinorUnitsString: String = "",
    val selectedCategory: String = "Food & Dining",
    val description: String = "",
    // Business Mode Specific Fields (only required/visible when activeWorkspace == BUSINESS)
    val spentBy: String = "",
    val departmentOrProject: String = "",
    val expenseStatus: ExpenseStatus = ExpenseStatus.PAID,
    val availableCategories: List<String> = DEFAULT_PERSONAL_CATEGORIES,
    val isSubmitting: Boolean = false,
    val currencyCode: String = "SAR",
    val errorMessage: String? = null
) {
    /**
     * Converts the raw digit buffer to numeric minor units (halalas/cents).
     * E.g., "1500" -> 1500L, "" -> 0L.
     */
    val amountMinorUnits: Long
        get() = rawMinorUnitsString.toLongOrNull() ?: 0L

    /**
     * Formatted presentation string for display in the numeric keypad header.
     * E.g., "1500" -> "15.00 SAR", "" -> "0.00 SAR".
     */
    val formattedDisplayAmount: String
        get() = formatMinorUnitsToCurrency(amountMinorUnits, currencyCode)

    /**
     * True if the current form inputs satisfy validation constraints to save.
     */
    val canSubmit: Boolean
        get() {
            if (amountMinorUnits <= 0L || isSubmitting) return false
            if (selectedCategory.isBlank()) return false
            if (activeWorkspace == WorkspaceType.BUSINESS) {
                if (spentBy.isBlank()) return false
                if (departmentOrProject.isBlank()) return false
            }
            return true
        }

    companion object {
        val DEFAULT_PERSONAL_CATEGORIES = listOf(
            "Food & Dining",
            "Housing & Rent",
            "Transportation",
            "Groceries",
            "Entertainment",
            "Utilities",
            "Healthcare",
            "Shopping",
            "Investments",
            "Other"
        )

        val DEFAULT_BUSINESS_CATEGORIES = listOf(
            "SaaS & Software",
            "Cloud Infrastructure",
            "Hardware & Office",
            "Contractors & Payroll",
            "Travel & Lodging",
            "Client Entertainment",
            "Marketing & Ads",
            "Legal & Compliance",
            "Office Supplies",
            "Other Operational"
        )

        /**
         * Pure utility to convert integer minor units into standard presentation format.
         * E.g., 1250L, "SAR" -> "12.50 SAR".
         */
        fun formatMinorUnitsToCurrency(minorUnits: Long, currency: String = "SAR"): String {
            val isNegative = minorUnits < 0L
            val absValue = kotlin.math.abs(minorUnits)
            val majorUnits = absValue / 100L
            val minorRemainder = absValue % 100L

            val symbols = DecimalFormatSymbols(Locale.US).apply {
                groupingSeparator = ','
            }
            val majorFormatter = DecimalFormat("#,##0", symbols)
            val formattedMajor = majorFormatter.format(majorUnits)
            val formattedMinor = String.format(Locale.US, "%02d", minorRemainder)
            val prefix = if (isNegative) "-" else ""

            return "$prefix$formattedMajor.$formattedMinor $currency"
        }

        /**
         * Utility converting raw digit buffer string directly into currency display.
         * E.g., "1250" -> "12.50 SAR", "" -> "0.00 SAR".
         */
        fun formatRawBufferToCurrency(rawBuffer: String, currency: String = "SAR"): String {
            val minorUnits = rawBuffer.toLongOrNull() ?: 0L
            return formatMinorUnitsToCurrency(minorUnits, currency)
        }
    }
}
