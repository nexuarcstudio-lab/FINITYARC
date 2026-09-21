package com.example.model

/**
 * Immutable budget goal / spending mission for an offline-first budget tracker.
 *
 * @property id Unique identifier for the mission.
 * @property title Human-readable description/objective (e.g., "Food under 500 SAR this week").
 * @property targetAmountMinorUnits Spending limit cap or target in integer minor units (Long).
 * @property spentAmountMinorUnits Current amount spent toward this limit in integer minor units (Long).
 * @property daysRemaining Days remaining until the mission window resets or closes.
 * @property isBusiness Denotes whether this mission belongs to the Business workspace or Personal workspace.
 */
data class BudgetMission(
    val id: String,
    val title: String,
    val targetAmountMinorUnits: Long,
    val spentAmountMinorUnits: Long,
    val daysRemaining: Int,
    val isBusiness: Boolean,
    val currencyCode: String = "SAR"
) {
    /**
     * Normalized progress ratio between 0.0f and 1.0f suitable for Jetpack Compose LinearProgressIndicator.
     */
    val progressFraction: Float
        get() = if (targetAmountMinorUnits > 0L) {
            (spentAmountMinorUnits.toFloat() / targetAmountMinorUnits.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    /**
     * Integer percentage of budget utilized (can exceed 100% if over budget).
     */
    val progressPercentage: Int
        get() = if (targetAmountMinorUnits > 0L) {
            ((spentAmountMinorUnits * 100L) / targetAmountMinorUnits).toInt()
        } else {
            0
        }

    /**
     * Remaining permissible spending capacity before reaching the cap.
     */
    val remainingAmountMinorUnits: Long
        get() = (targetAmountMinorUnits - spentAmountMinorUnits).coerceAtLeast(0L)

    /**
     * True if the user has breached the target spending limit.
     */
    val isOverBudget: Boolean
        get() = spentAmountMinorUnits > targetAmountMinorUnits

    val formattedTarget: String
        get() = CurrencyFormatter.format(targetAmountMinorUnits, currencyCode)

    val formattedSpent: String
        get() = CurrencyFormatter.format(spentAmountMinorUnits, currencyCode)

    val formattedRemaining: String
        get() = CurrencyFormatter.format(remainingAmountMinorUnits, currencyCode)
}
