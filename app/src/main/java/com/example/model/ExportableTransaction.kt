package com.example.model

/**
 * Full domain entity representing an exportable/importable transaction in FINITYARC offline ledger.
 *
 * Covers all required CSV persistence attributes:
 * ID, Date, Amount (Major/Minor), Type, Category, Description, SpentBy, Department, Status.
 */
data class ExportableTransaction(
    val id: String,
    val dateIso: String,
    val amountMinorUnits: Long,
    val type: TransactionType = TransactionType.EXPENSE,
    val category: String,
    val description: String = "",
    val spentBy: String? = null,
    val department: String? = null,
    val status: ExpenseStatus = ExpenseStatus.PAID
) {
    /**
     * Major unit decimal representation (e.g. 1500L -> "15.00").
     */
    val amountMajorString: String
        get() {
            val major = amountMinorUnits / 100L
            val minor = kotlin.math.abs(amountMinorUnits % 100L)
            return String.format(java.util.Locale.US, "%d.%02d", major, minor)
        }
}
