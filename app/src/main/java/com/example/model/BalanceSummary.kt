package com.example.model

/**
 * Immutable financial summary representing active workspace balances.
 * All monetary amounts are held strictly as [Long] minor units (e.g. 5000L = 50.00 SAR).
 *
 * @property totalBalanceMinorUnits Net current balance available in the active workspace.
 * @property totalIncomeMinorUnits Total accumulated income recorded for the current period.
 * @property totalExpenseMinorUnits Total accumulated expenditures recorded for the current period.
 * @property currencyCode ISO standard currency code representation, defaulting to "SAR".
 */
data class BalanceSummary(
    val totalBalanceMinorUnits: Long,
    val totalIncomeMinorUnits: Long,
    val totalExpenseMinorUnits: Long,
    val currencyCode: String = "SAR"
) {
    val formattedTotalBalance: String
        get() = CurrencyFormatter.format(totalBalanceMinorUnits, currencyCode)

    val formattedTotalIncome: String
        get() = CurrencyFormatter.format(totalIncomeMinorUnits, currencyCode)

    val formattedTotalExpense: String
        get() = CurrencyFormatter.format(totalExpenseMinorUnits, currencyCode)

    /**
     * Ratio of saved capital relative to total income expressed as an integer percentage (0-100%).
     */
    val savingsRatePercentage: Int
        get() {
            if (totalIncomeMinorUnits <= 0L) return 0
            val netSaved = (totalIncomeMinorUnits - totalExpenseMinorUnits).coerceAtLeast(0L)
            return ((netSaved * 100L) / totalIncomeMinorUnits).toInt()
        }

    /**
     * Net savings amount calculated without floating-point errors.
     */
    val netSavingsMinorUnits: Long
        get() = totalIncomeMinorUnits - totalExpenseMinorUnits
}
