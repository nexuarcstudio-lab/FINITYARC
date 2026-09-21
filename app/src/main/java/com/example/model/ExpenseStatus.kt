package com.example.model

/**
 * Settlement / corporate status for business expenses in FINITYARC.
 */
enum class ExpenseStatus(val displayName: String) {
    PAID("Paid"),
    PENDING("Pending Approval"),
    REIMBURSABLE("Reimbursable")
}
