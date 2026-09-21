package com.example.ui.state

import com.example.model.WorkspaceType

/**
 * Immutable UI state model for the FINITYARC Settings Screen.
 */
data class SettingsUiState(
    val activeWorkspace: WorkspaceType = WorkspaceType.PERSONAL,
    val selectedCurrency: String = "SAR",
    val availableCurrencies: List<String> = DEFAULT_CURRENCIES,
    val isBiometricLockEnabled: Boolean = false,
    val totalStoredTransactionsCount: Int = 0,
    val lastBackupTimestamp: Long? = null,
    val appVersion: String = "1.0.0-offline",
    val isExporting: Boolean = false,
    val isImporting: Boolean = false,
    val lastExportedCsv: String? = null,
    val statusMessage: String? = null,
    val errorMessage: String? = null
) {
    /**
     * Formatted string representing the last backup timestamp.
     */
    val formattedLastBackup: String
        get() = if (lastBackupTimestamp != null && lastBackupTimestamp > 0L) {
            val sdf = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.US)
            sdf.format(java.util.Date(lastBackupTimestamp))
        } else {
            "Never"
        }

    companion object {
        val DEFAULT_CURRENCIES = listOf("SAR", "USD", "EUR", "AED", "GBP")
    }
}
