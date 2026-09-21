package com.example.ui.state

import com.example.model.BalanceSummary
import com.example.model.BudgetMission
import com.example.model.HomeTransaction
import com.example.model.WorkspaceType

/**
 * Single source of truth immutable UI State for the FINITYARC Home Screen.
 *
 * Designed with strict unidirectional data flow (UDF) principles.
 * The UI layer observes this immutable contract and never mutates state directly.
 *
 * @property activeWorkspace The currently selected workspace ([WorkspaceType.PERSONAL] or [WorkspaceType.BUSINESS]).
 * @property balanceSummary The aggregated balance metrics for the active workspace.
 * @property activeMissions Active budget missions and spending targets for the active workspace.
 * @property recentTransactions Chronological stream of recent inflows and outflows.
 * @property isLoading Indicates whether local database queries or async sync operations are in progress.
 */
data class HomeUiState(
    val activeWorkspace: WorkspaceType = WorkspaceType.PERSONAL,
    val balanceSummary: BalanceSummary = BalanceSummary(
        totalBalanceMinorUnits = 0L,
        totalIncomeMinorUnits = 0L,
        totalExpenseMinorUnits = 0L,
        currencyCode = "SAR"
    ),
    val activeMissions: List<BudgetMission> = emptyList(),
    val recentTransactions: List<HomeTransaction> = emptyList(),
    val isLoading: Boolean = false
) {
    /**
     * Determines whether the workspace dashboard has zero recorded history and missions.
     */
    val isEmpty: Boolean
        get() = !isLoading &&
                activeMissions.isEmpty() &&
                recentTransactions.isEmpty() &&
                balanceSummary.totalBalanceMinorUnits == 0L

    /**
     * Number of missions currently at risk or exceeding the target budget threshold.
     */
    val alertMissionsCount: Int
        get() = activeMissions.count { it.isOverBudget || it.progressFraction >= 0.9f }
}
