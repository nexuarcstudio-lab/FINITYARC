package com.example.model

/**
 * Immutable transaction item displayed on the Home Screen.
 *
 * @property id Unique transaction identifier.
 * @property title Descriptive title or merchant name (e.g. "Al-Danube Supermarket").
 * @property category Category taxonomy (e.g. "Groceries", "Utilities", "Cloud Services").
 * @property amountMinorUnits Transaction amount stored as positive Long minor units (e.g. 5000L = 50.00 SAR).
 * @property isExpense True if this represents an outflow/debit, false if it is an inflow/credit.
 * @property dateFormatted User-facing formatted date string (e.g. "Today, 04:15 PM").
 * @property spentBy Optional member or department attribution for business workspace records (e.g. "Sarah (Operations)").
 */
data class HomeTransaction(
    val id: String,
    val title: String,
    val category: String,
    val amountMinorUnits: Long,
    val isExpense: Boolean,
    val dateFormatted: String,
    val spentBy: String? = null,
    val currencyCode: String = "SAR"
) {
    /**
     * Formatted string with appropriate positive or negative prefix indicator.
     */
    val formattedAmount: String
        get() = CurrencyFormatter.formatSigned(amountMinorUnits, isExpense, currencyCode)

    /**
     * Clean absolute formatted string without directional symbol.
     */
    val formattedAbsoluteAmount: String
        get() = CurrencyFormatter.format(amountMinorUnits, currencyCode)
}
