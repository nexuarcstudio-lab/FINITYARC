package com.example.data

import com.example.model.BalanceSummary
import com.example.model.BudgetMission
import com.example.model.HomeTransaction
import com.example.model.WorkspaceType
import com.example.ui.state.HomeUiState

/**
 * Offline mock data provider for FINITYARC.
 *
 * Implements full production-grade sample datasets for both Personal and Business workspaces.
 * All amounts are strictly modeled as Long integer minor units (e.g. 5000L = 50.00 SAR).
 */
class HomeMockData {

    companion object {

        // =========================================================================
        // PERSONAL WORKSPACE DATA
        // =========================================================================

        /**
         * Personal household balance summary.
         * Total Balance: 14,850.00 SAR (1,485,000 minor units)
         * Total Income:  24,000.00 SAR (2,400,000 minor units)
         * Total Expense:  9,150.00 SAR (  915,000 minor units)
         */
        val personalBalance = BalanceSummary(
            totalBalanceMinorUnits = 1_485_000L,
            totalIncomeMinorUnits = 2_400_000L,
            totalExpenseMinorUnits = 915_000L,
            currencyCode = "SAR"
        )

        /**
         * Active personal budget missions with varying completion stages and deadlines.
         */
        val personalMissions: List<BudgetMission> = listOf(
            BudgetMission(
                id = "mission-p-101",
                title = "Food & Groceries under 2,000 SAR this month",
                targetAmountMinorUnits = 200_000L,   // 2,000.00 SAR
                spentAmountMinorUnits = 142_050L,    // 1,420.50 SAR (71%)
                daysRemaining = 8,
                isBusiness = false
            ),
            BudgetMission(
                id = "mission-p-102",
                title = "Coffee & Dining under 500 SAR this week",
                targetAmountMinorUnits = 50_000L,    // 500.00 SAR
                spentAmountMinorUnits = 315_00L,     // 315.00 SAR (63%)
                daysRemaining = 3,
                isBusiness = false
            ),
            BudgetMission(
                id = "mission-p-103",
                title = "Fuel & Highway Commute under 650 SAR",
                targetAmountMinorUnits = 65_000L,    // 650.00 SAR
                spentAmountMinorUnits = 585_00L,     // 585.00 SAR (90% - Near Cap)
                daysRemaining = 11,
                isBusiness = false
            ),
            BudgetMission(
                id = "mission-p-104",
                title = "Home Entertainment & Gaming under 400 SAR",
                targetAmountMinorUnits = 40_000L,    // 400.00 SAR
                spentAmountMinorUnits = 129_00L,     // 129.00 SAR (32%)
                daysRemaining = 16,
                isBusiness = false
            )
        )

        /**
         * Recent personal ledger transactions.
         */
        val personalTransactions: List<HomeTransaction> = listOf(
            HomeTransaction(
                id = "tx-p-201",
                title = "Danube Hypermarket",
                category = "Groceries",
                amountMinorUnits = 412_50L, // 412.50 SAR
                isExpense = true,
                dateFormatted = "Today, 04:15 PM",
                spentBy = null
            ),
            HomeTransaction(
                id = "tx-p-202",
                title = "Barn's Coffee Roasters",
                category = "Dining",
                amountMinorUnits = 28_00L, // 28.00 SAR
                isExpense = true,
                dateFormatted = "Today, 08:30 AM",
                spentBy = null
            ),
            HomeTransaction(
                id = "tx-p-203",
                title = "Monthly Salary Deposit",
                category = "Salary",
                amountMinorUnits = 2_400_000L, // 24,000.00 SAR
                isExpense = false,
                dateFormatted = "Yesterday, 09:00 AM",
                spentBy = null
            ),
            HomeTransaction(
                id = "tx-p-204",
                title = "Jarir Bookstore",
                category = "Electronics & Books",
                amountMinorUnits = 189_00L, // 189.00 SAR
                isExpense = true,
                dateFormatted = "Sep 22, 07:45 PM",
                spentBy = null
            ),
            HomeTransaction(
                id = "tx-p-205",
                title = "STC Fiber Gigabit Internet",
                category = "Utilities",
                amountMinorUnits = 345_00L, // 345.00 SAR
                isExpense = true,
                dateFormatted = "Sep 20, 11:20 AM",
                spentBy = null
            ),
            HomeTransaction(
                id = "tx-p-206",
                title = "Freelance UI Consultation",
                category = "Side Income",
                amountMinorUnits = 350_000L, // 3,500.00 SAR
                isExpense = false,
                dateFormatted = "Sep 18, 03:30 PM",
                spentBy = null
            )
        )

        // =========================================================================
        // BUSINESS WORKSPACE DATA
        // =========================================================================

        /**
         * Business enterprise balance summary.
         * Total Balance: 86,420.00 SAR (8,642,000 minor units)
         * Total Income: 115,000.00 SAR (11,500,000 minor units)
         * Total Expense: 28,580.00 SAR (2,858,000 minor units)
         */
        val businessBalance = BalanceSummary(
            totalBalanceMinorUnits = 8_642_000L,
            totalIncomeMinorUnits = 11_500_000L,
            totalExpenseMinorUnits = 2_858_000L,
            currencyCode = "SAR"
        )

        /**
         * Active business budget missions with team spending limits and timelines.
         */
        val businessMissions: List<BudgetMission> = listOf(
            BudgetMission(
                id = "mission-b-301",
                title = "Cloud Infrastructure & Servers under 12,000 SAR Q3",
                targetAmountMinorUnits = 1_200_000L, // 12,000.00 SAR
                spentAmountMinorUnits = 8_450_00L,   // 8,450.00 SAR (70%)
                daysRemaining = 14,
                isBusiness = true
            ),
            BudgetMission(
                id = "mission-b-302",
                title = "Team Refreshments & Pantry under 3,000 SAR",
                targetAmountMinorUnits = 300_000L,   // 3,000.00 SAR
                spentAmountMinorUnits = 1_840_00L,   // 1,840.00 SAR (61%)
                daysRemaining = 7,
                isBusiness = true
            ),
            BudgetMission(
                id = "mission-b-303",
                title = "Client Travel & Partner Meetings under 6,000 SAR",
                targetAmountMinorUnits = 600_000L,   // 6,000.00 SAR
                spentAmountMinorUnits = 5_420_00L,   // 5,420.00 SAR (90% - Warning)
                daysRemaining = 5,
                isBusiness = true
            ),
            BudgetMission(
                id = "mission-b-304",
                title = "Software Subscriptions & SaaS under 4,500 SAR",
                targetAmountMinorUnits = 450_000L,   // 4,500.00 SAR
                spentAmountMinorUnits = 2_150_00L,   // 2,150.00 SAR (47%)
                daysRemaining = 20,
                isBusiness = true
            )
        )

        /**
         * Recent business ledger entries including corporate team member attributions ([spentBy]).
         */
        val businessTransactions: List<HomeTransaction> = listOf(
            HomeTransaction(
                id = "tx-b-401",
                title = "Amazon Web Services (AWS) Riyadh Region",
                category = "Infrastructure",
                amountMinorUnits = 4_250_00L, // 4,250.00 SAR
                isExpense = true,
                dateFormatted = "Today, 11:30 AM",
                spentBy = "Fahad (DevOps Lead)"
            ),
            HomeTransaction(
                id = "tx-b-402",
                title = "Enterprise Retainer Invoice #SA-2041",
                category = "Client Retainer",
                amountMinorUnits = 45_000_00L, // 45,000.00 SAR
                isExpense = false,
                dateFormatted = "Yesterday, 02:15 PM",
                spentBy = null
            ),
            HomeTransaction(
                id = "tx-b-403",
                title = "Apple Store Olaya (Hardware Upgrades)",
                category = "Equipment",
                amountMinorUnits = 7_899_00L, // 7,899.00 SAR
                isExpense = true,
                dateFormatted = "Sep 22, 05:40 PM",
                spentBy = "Sarah (Operations)"
            ),
            HomeTransaction(
                id = "tx-b-404",
                title = "WeWork Executive Boardroom Booking",
                category = "Facilities",
                amountMinorUnits = 1_250_00L, // 1,250.00 SAR
                isExpense = true,
                dateFormatted = "Sep 21, 09:15 AM",
                spentBy = "Ahmed (Procurement)"
            ),
            HomeTransaction(
                id = "tx-b-405",
                title = "Google Workspace & Slack Enterprise",
                category = "Software & IT",
                amountMinorUnits = 680_00L, // 680.00 SAR
                isExpense = true,
                dateFormatted = "Sep 20, 01:00 PM",
                spentBy = "Sarah (Operations)"
            ),
            HomeTransaction(
                id = "tx-b-406",
                title = "Government Licensing & CR Renewal",
                category = "Compliance",
                amountMinorUnits = 2_100_00L, // 2,100.00 SAR
                isExpense = true,
                dateFormatted = "Sep 19, 10:45 AM",
                spentBy = "Tariq (Finance Officer)"
            )
        )

        // =========================================================================
        // STATE PROVIDERS
        // =========================================================================

        /**
         * Returns an immutable [HomeUiState] configured for the Personal workspace.
         */
        val samplePersonalState: HomeUiState
            get() = getPersonalState()

        val sampleBusinessState: HomeUiState
            get() = getBusinessState()

        fun getPersonalState(): HomeUiState {
            return HomeUiState(
                activeWorkspace = WorkspaceType.PERSONAL,
                balanceSummary = personalBalance,
                activeMissions = personalMissions,
                recentTransactions = personalTransactions,
                isLoading = false
            )
        }

        /**
         * Returns an immutable [HomeUiState] configured for the Business workspace.
         */
        fun getBusinessState(): HomeUiState {
            return HomeUiState(
                activeWorkspace = WorkspaceType.BUSINESS,
                balanceSummary = businessBalance,
                activeMissions = businessMissions,
                recentTransactions = businessTransactions,
                isLoading = false
            )
        }

        /**
         * Resolves the appropriate immutable [HomeUiState] for the requested [workspace].
         */
        fun getStateForWorkspace(workspace: WorkspaceType): HomeUiState {
            return when (workspace) {
                WorkspaceType.PERSONAL -> getPersonalState()
                WorkspaceType.BUSINESS -> getBusinessState()
            }
        }
    }
}
