package com.example.model

/**
 * Fundamental transaction direction / classification for FINITYARC.
 */
enum class TransactionType(val displayName: String) {
    EXPENSE("Expense"),
    INCOME("Income"),
    TRANSFER("Transfer")
}
