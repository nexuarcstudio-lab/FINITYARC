package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HomeMockData
import com.example.model.BalanceSummary
import com.example.model.BudgetMission
import com.example.model.HomeTransaction
import com.example.model.WorkspaceType
import com.example.ui.state.HomeUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Unified application-level state for FINITYARC.
 *
 * Serves as the single reactive source of truth for:
 * - Selected active currency across the entire application (default "SAR").
 * - Active workspace (PERSONAL vs. BUSINESS).
 * - Live transaction ledger (starts with rich offline mock data, dynamically prepends new entries).
 * - Active budget missions and spending targets.
 * - Dynamic financial calculations (totalIncome, totalExpense, netBalance, mission progress).
 */
data class MainAppState(
    val selectedCurrency: String = "SAR",
    val activeWorkspace: WorkspaceType = WorkspaceType.PERSONAL,
    val personalTransactions: List<HomeTransaction> = HomeMockData.personalTransactions,
    val businessTransactions: List<HomeTransaction> = HomeMockData.businessTransactions,
    val personalMissions: List<BudgetMission> = HomeMockData.personalMissions,
    val businessMissions: List<BudgetMission> = HomeMockData.businessMissions,
    val isLoading: Boolean = false
) {
    /**
     * Transactions filtered for the currently active workspace, formatted with the active currency.
     */
    val currentTransactions: List<HomeTransaction>
        get() {
            val txs = if (activeWorkspace == WorkspaceType.BUSINESS) businessTransactions else personalTransactions
            return txs.map { it.copy(currencyCode = selectedCurrency) }
        }

    /**
     * Missions filtered for the currently active workspace, formatted with the active currency
     * and with spent amounts dynamically recalculated based on matching recorded expense transactions.
     */
    val currentMissions: List<BudgetMission>
        get() {
            val baseMissions = if (activeWorkspace == WorkspaceType.BUSINESS) businessMissions else personalMissions
            return baseMissions.map { mission ->
                // Calculate dynamic spending from transactions matching this mission category
                val matchingExpenses = currentTransactions
                    .filter { it.isExpense && matchesMissionCategory(mission, it.category) }
                    .sumOf { it.amountMinorUnits }

                val dynamicSpent = (mission.spentAmountMinorUnits + matchingExpenses).coerceAtLeast(0L)
                mission.copy(
                    spentAmountMinorUnits = dynamicSpent,
                    currencyCode = selectedCurrency
                )
            }
        }

    /**
     * Total income (inflow) sum in minor units for the active workspace.
     */
    val totalIncomeMinorUnits: Long
        get() = currentTransactions
            .filter { !it.isExpense }
            .sumOf { it.amountMinorUnits }

    /**
     * Total expense (outflow) sum in minor units for the active workspace.
     */
    val totalExpenseMinorUnits: Long
        get() = currentTransactions
            .filter { it.isExpense }
            .sumOf { it.amountMinorUnits }

    /**
     * Net balance in minor units (totalIncome - totalExpense).
     */
    val netBalanceMinorUnits: Long
        get() = totalIncomeMinorUnits - totalExpenseMinorUnits

    /**
     * Aggregated [BalanceSummary] reactive to active workspace and currency changes.
     */
    val balanceSummary: BalanceSummary
        get() = BalanceSummary(
            totalBalanceMinorUnits = netBalanceMinorUnits,
            totalIncomeMinorUnits = totalIncomeMinorUnits,
            totalExpenseMinorUnits = totalExpenseMinorUnits,
            currencyCode = selectedCurrency
        )

    /**
     * Converts this state to [HomeUiState] consumed by [HomeScreen].
     */
    fun toHomeUiState(): HomeUiState = HomeUiState(
        activeWorkspace = activeWorkspace,
        balanceSummary = balanceSummary,
        activeMissions = currentMissions,
        recentTransactions = currentTransactions,
        isLoading = isLoading
    )

    companion object {
        /**
         * Determines if a transaction category attributes to a budget mission.
         */
        private fun matchesMissionCategory(mission: BudgetMission, txCategory: String): Boolean {
            val missionTitleLower = mission.title.lowercase()
            val categoryLower = txCategory.lowercase()
            return when {
                categoryLower.contains("grocer") || categoryLower.contains("food") ->
                    missionTitleLower.contains("food") || missionTitleLower.contains("grocer")
                categoryLower.contains("coffee") || categoryLower.contains("dining") ->
                    missionTitleLower.contains("coffee") || missionTitleLower.contains("dining")
                categoryLower.contains("commute") || categoryLower.contains("fuel") || categoryLower.contains("transport") ->
                    missionTitleLower.contains("fuel") || missionTitleLower.contains("commute")
                categoryLower.contains("cloud") || categoryLower.contains("server") ->
                    missionTitleLower.contains("cloud") || missionTitleLower.contains("server")
                categoryLower.contains("travel") || categoryLower.contains("flight") ->
                    missionTitleLower.contains("travel") || missionTitleLower.contains("client")
                categoryLower.contains("refreshment") || categoryLower.contains("pantry") ->
                    missionTitleLower.contains("refreshment") || missionTitleLower.contains("pantry")
                else -> false
            }
        }
    }
}

/**
 * Root ViewModel holding the unified [MainAppState].
 * Ensures seamless reactive synchronization between CreateScreen, HomeScreen, and SettingsScreen.
 */
class MainAppViewModel : ViewModel() {

    private val _appState = MutableStateFlow(MainAppState())
    val appState: StateFlow<MainAppState> = _appState.asStateFlow()

    // Event bus to communicate one-off navigation/toast events
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>()
    val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    sealed interface NavigationEvent {
        object NavigateToHome : NavigationEvent
        data class ShowSnackbar(val message: String) : NavigationEvent
    }

    /**
     * Switches the active workspace (PERSONAL vs BUSINESS).
     */
    fun selectWorkspace(workspace: WorkspaceType) {
        if (_appState.value.activeWorkspace == workspace) return
        _appState.update { it.copy(activeWorkspace = workspace) }
    }

    /**
     * Updates the base currency across the entire application (e.g. "SAR", "USD", "EUR", "AED", "GBP").
     */
    fun setCurrency(currency: String) {
        _appState.update { it.copy(selectedCurrency = currency) }
    }

    /**
     * Adds a newly created transaction to the active workspace's transaction list,
     * triggering automatic reactive recalculation of net balance, total income, total expense,
     * and budget mission progress.
     *
     * @param transaction The new [HomeTransaction] entry.
     * @param batchContinue If true, keep user on CreateScreen for rapid batch entry ("+ Another").
     *                      If false, navigates back to HomeScreen.
     */
    fun onSaveTransaction(transaction: HomeTransaction, batchContinue: Boolean = false) {
        val current = _appState.value
        if (current.activeWorkspace == WorkspaceType.BUSINESS) {
            _appState.update { state ->
                state.copy(businessTransactions = listOf(transaction) + state.businessTransactions)
            }
        } else {
            _appState.update { state ->
                state.copy(personalTransactions = listOf(transaction) + state.personalTransactions)
            }
        }

        viewModelScope.launch {
            if (batchContinue) {
                _navigationEvents.emit(NavigationEvent.ShowSnackbar("Entry saved! Ready for next."))
            } else {
                _navigationEvents.emit(NavigationEvent.NavigateToHome)
            }
        }
    }

    /**
     * Simulates an offline refresh of the active workspace.
     */
    fun refreshCurrentWorkspace() {
        viewModelScope.launch {
            _appState.update { it.copy(isLoading = true) }
            kotlinx.coroutines.delay(150)
            _appState.update { it.copy(isLoading = false) }
        }
    }

    /**
     * Clears all transactions from the current workspace (used by Settings Danger Zone).
     */
    fun clearWorkspaceTransactions() {
        val current = _appState.value
        if (current.activeWorkspace == WorkspaceType.BUSINESS) {
            _appState.update { it.copy(businessTransactions = emptyList()) }
        } else {
            _appState.update { it.copy(personalTransactions = emptyList()) }
        }
    }
}
