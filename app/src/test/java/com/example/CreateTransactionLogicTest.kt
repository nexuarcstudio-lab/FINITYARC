package com.example

import com.example.model.ExpenseStatus
import com.example.model.TransactionType
import com.example.model.WorkspaceType
import com.example.ui.create.CreateTransactionViewModel
import com.example.util.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying the state machine, keypad logic, and monetary precision
 * of FINITYARC's Create Transaction flow.
 */
class CreateTransactionLogicTest {

    @Test
    fun `currency formatter correctly converts minor units to SAR representation`() {
        assertEquals("50.00 SAR", CurrencyFormatter.formatMinorUnits(5000L))
        assertEquals("12.50 SAR", CurrencyFormatter.formatMinorUnits(1250L))
        assertEquals("0.05 SAR", CurrencyFormatter.formatMinorUnits(5L))
        assertEquals("0.00 SAR", CurrencyFormatter.formatMinorUnits(0L))
        assertEquals("-15.75 SAR", CurrencyFormatter.formatMinorUnits(-1575L))
    }

    @Test
    fun `keypad digit entry updates raw string and calculates minor units`() {
        val viewModel = CreateTransactionViewModel(initialWorkspace = WorkspaceType.PERSONAL)

        // Initial state
        assertEquals("", viewModel.uiState.value.rawMinorUnitsString)
        assertEquals(0L, viewModel.uiState.value.amountMinorUnits)

        // Type '1', '5', '0', '0' -> 15.00 SAR
        viewModel.onDigitPressed('1')
        viewModel.onDigitPressed('5')
        viewModel.onDigitPressed('0')
        viewModel.onDigitPressed('0')

        val state = viewModel.uiState.value
        assertEquals("1500", state.rawMinorUnitsString)
        assertEquals(1500L, state.amountMinorUnits)
        assertEquals("15.00 SAR", state.formattedDisplayAmount)
    }

    @Test
    fun `keypad backspace and clear handle boundaries gracefully`() {
        val viewModel = CreateTransactionViewModel()

        viewModel.onDigitPressed('2')
        viewModel.onDigitPressed('5')
        viewModel.onDigitPressed('0')
        assertEquals("250", viewModel.uiState.value.rawMinorUnitsString)

        viewModel.onBackspace()
        assertEquals("25", viewModel.uiState.value.rawMinorUnitsString)
        assertEquals(25L, viewModel.uiState.value.amountMinorUnits)

        viewModel.onClear()
        assertEquals("", viewModel.uiState.value.rawMinorUnitsString)
        assertEquals(0L, viewModel.uiState.value.amountMinorUnits)
        assertEquals("0.00 SAR", viewModel.uiState.value.formattedDisplayAmount)
    }

    @Test
    fun `keypad prevents leading zeros and caps at 9 digits`() {
        val viewModel = CreateTransactionViewModel()

        // Leading zeros ignored when empty
        viewModel.onDigitPressed('0')
        assertEquals("", viewModel.uiState.value.rawMinorUnitsString)

        // Enter 10 digits; 10th should be rejected
        for (digit in "1234567890") {
            viewModel.onDigitPressed(digit)
        }
        // Should only have 9 digits
        assertEquals(9, viewModel.uiState.value.rawMinorUnitsString.length)
        assertEquals("123456789", viewModel.uiState.value.rawMinorUnitsString)
    }

    @Test
    fun `business mode enforces spentBy and departmentOrProject validation`() {
        val viewModel = CreateTransactionViewModel(initialWorkspace = WorkspaceType.BUSINESS)

        viewModel.onDigitPressed('5')
        viewModel.onDigitPressed('0')
        viewModel.onDigitPressed('0') // 5.00 SAR

        // Initially empty business fields -> cannot submit
        assertFalse(viewModel.uiState.value.canSubmit)

        viewModel.onSave(resetForm = false)
        assertTrue(viewModel.uiState.value.errorMessage!!.contains("spent"))

        viewModel.onBusinessFieldChanged(
            spentBy = "Sarah Lead",
            departmentOrProject = "Core Infrastructure",
            status = ExpenseStatus.PAID
        )

        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `workspace change updates available categories`() {
        val viewModel = CreateTransactionViewModel(initialWorkspace = WorkspaceType.PERSONAL)
        assertTrue(viewModel.uiState.value.availableCategories.contains("Food & Dining"))

        viewModel.onWorkspaceChanged(WorkspaceType.BUSINESS)
        assertEquals(WorkspaceType.BUSINESS, viewModel.uiState.value.activeWorkspace)
        assertTrue(viewModel.uiState.value.availableCategories.contains("SaaS & Software"))
    }
}
