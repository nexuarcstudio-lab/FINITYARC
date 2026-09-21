package com.example

import com.example.data.HomeMockData
import com.example.model.BalanceSummary
import com.example.model.BudgetMission
import com.example.model.CurrencyFormatter
import com.example.model.HomeTransaction
import com.example.model.WorkspaceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun currencyFormatter_formatsMinorUnitsAccurately() {
    // 50.00 SAR = 5000L
    val formatted = CurrencyFormatter.format(5000L, "SAR")
    assertEquals("50.00 SAR", formatted)

    // Check thousand separator: 12,450.75 SAR = 1245075L
    val formattedLarge = CurrencyFormatter.format(1245075L, "SAR")
    assertEquals("12,450.75 SAR", formattedLarge)

    // Zero
    assertEquals("0.00 SAR", CurrencyFormatter.format(0L, "SAR"))
  }

  @Test
  fun balanceSummary_calculatesCorrectSavingsRate() {
    val summary = BalanceSummary(
      totalBalanceMinorUnits = 14_850_00L,
      totalIncomeMinorUnits = 20_000_00L,
      totalExpenseMinorUnits = 10_000_00L,
      currencyCode = "SAR"
    )
    // Saved: 10,000 out of 20,000 -> 50%
    assertEquals(50, summary.savingsRatePercentage)
    assertEquals(10_000_00L, summary.netSavingsMinorUnits)
  }

  @Test
  fun budgetMission_progressAndOverBudgetCalculations() {
    val mission = BudgetMission(
      id = "m1",
      title = "Food under 500 SAR this week",
      targetAmountMinorUnits = 500_00L,
      spentAmountMinorUnits = 250_00L,
      daysRemaining = 4,
      isBusiness = false
    )

    assertEquals(0.5f, mission.progressFraction, 0.001f)
    assertEquals(50, mission.progressPercentage)
    assertEquals(250_00L, mission.remainingAmountMinorUnits)
    assertFalse(mission.isOverBudget)

    val overBudgetMission = mission.copy(spentAmountMinorUnits = 550_00L)
    assertTrue(overBudgetMission.isOverBudget)
    assertEquals(1.0f, overBudgetMission.progressFraction, 0.001f)
  }

  @Test
  fun homeTransaction_formatsAmountsCorrectly() {
    val expense = HomeTransaction(
      id = "tx-1",
      title = "Danube",
      category = "Groceries",
      amountMinorUnits = 50_00L,
      isExpense = true,
      dateFormatted = "Today"
    )
    assertEquals("- 50.00 SAR", expense.formattedAmount)

    val income = HomeTransaction(
      id = "tx-2",
      title = "Salary",
      category = "Income",
      amountMinorUnits = 10_000_00L,
      isExpense = false,
      dateFormatted = "Yesterday"
    )
    assertEquals("+ 10,000.00 SAR", income.formattedAmount)
  }

  @Test
  fun mockDataProvider_providesDistinctPersonalAndBusinessData() {
    val personalState = HomeMockData.getPersonalState()
    val businessState = HomeMockData.getBusinessState()

    assertEquals(WorkspaceType.PERSONAL, personalState.activeWorkspace)
    assertEquals(WorkspaceType.BUSINESS, businessState.activeWorkspace)

    // Check personal missions are non-business
    assertTrue(personalState.activeMissions.all { !it.isBusiness })
    assertTrue(businessState.activeMissions.all { it.isBusiness })

    // Check business transactions have spentBy populated where appropriate
    val businessTxWithSpentBy = businessState.recentTransactions.filter { it.spentBy != null }
    assertTrue(businessTxWithSpentBy.isNotEmpty())
    assertNotNull(businessTxWithSpentBy.first().spentBy)

    // Check monetary units are Long
    assertTrue(personalState.balanceSummary.totalBalanceMinorUnits > 0L)
    assertTrue(businessState.balanceSummary.totalBalanceMinorUnits > 0L)
  }

  @Test
  fun mainAppViewModel_dynamicCalculationsAndCurrencyUpdates() {
    val vm = com.example.ui.MainAppViewModel()

    val initialState = vm.appState.value
    assertEquals("SAR", initialState.selectedCurrency)
    assertEquals(WorkspaceType.PERSONAL, initialState.activeWorkspace)

    val initialCount = initialState.personalTransactions.size
    val initialBalance = initialState.netBalanceMinorUnits

    // Save a new expense transaction
    val newExpense = HomeTransaction(
      id = "test-new-exp",
      title = "New Office Supplies",
      category = "Office Supplies",
      amountMinorUnits = 150_00L, // 150.00
      isExpense = true,
      dateFormatted = "Today"
    )

    vm.onSaveTransaction(newExpense, batchContinue = false)

    val updatedState = vm.appState.value
    assertEquals(initialCount + 1, updatedState.personalTransactions.size)
    assertEquals(initialBalance - 150_00L, updatedState.netBalanceMinorUnits)
    assertEquals(newExpense.id, updatedState.currentTransactions.first().id)

    // Switch currency to USD
    vm.setCurrency("USD")
    val usdState = vm.appState.value
    assertEquals("USD", usdState.selectedCurrency)
    assertEquals("USD", usdState.balanceSummary.currencyCode)
    assertTrue(usdState.balanceSummary.formattedTotalBalance.contains("USD"))
    assertTrue(usdState.currentTransactions.first().formattedAmount.contains("USD"))
    assertTrue(usdState.currentMissions.first().formattedTarget.contains("USD"))
  }
}

