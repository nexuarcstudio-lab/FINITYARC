package com.example.ui.create

import com.example.model.ExpenseStatus
import com.example.model.TransactionType
import com.example.model.WorkspaceType

/**
 * Interface contract defining all keypad, selection, and form submission actions
 * for the FINITYARC Create Transaction flow.
 */
interface CreateTransactionActions {
    /** Appends digit to the raw minor units buffer (capped at 9 digits to prevent Long overflow). */
    fun onDigitPressed(digit: Char)

    /** Drops the last digit from the input buffer. */
    fun onBackspace()

    /** Resets the entire numeric input buffer. */
    fun onClear()

    /** Selects transaction type (EXPENSE, INCOME, TRANSFER). */
    fun onTypeSelected(type: TransactionType)

    /** Selects or updates the category. */
    fun onCategorySelected(category: String)

    /** Updates the description / memo field. */
    fun onDescriptionChanged(description: String)

    /** Updates workspace (switches category lists and business field requirements). */
    fun onWorkspaceChanged(workspace: WorkspaceType)

    /** Updates business-specific metadata fields. */
    fun onBusinessFieldChanged(
        spentBy: String,
        departmentOrProject: String,
        status: ExpenseStatus
    )

    /**
     * Saves the transaction.
     * @param resetForm If true, saves the transaction and resets the input buffer for rapid batch entry ("+ Another").
     *                  If false, finishes transaction creation and signals completion.
     */
    fun onSave(resetForm: Boolean = false)

    /** Clears any active error state. */
    fun onDismissError()
}
