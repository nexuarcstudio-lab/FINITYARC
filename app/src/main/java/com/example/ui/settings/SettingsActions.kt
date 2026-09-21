package com.example.ui.settings

import com.example.model.WorkspaceType

/**
 * Interface contract defining user event handlers and callbacks for the Settings Screen.
 */
interface SettingsActions {
    /** Toggles the active workspace between PERSONAL and BUSINESS. */
    fun onWorkspaceToggle(workspace: WorkspaceType)

    /** Selects the primary ledger currency (e.g., "SAR", "USD"). */
    fun onCurrencyChanged(currency: String)

    /** Toggles local biometric lock protection. */
    fun onToggleBiometricLock(enabled: Boolean)

    /** Initiates export of transactions to CSV format. */
    fun onExportCsvClicked()

    /** Initiates import of transactions from CSV format. */
    fun onImportCsvClicked()

    /** Clears all stored ledger transactions and resets state. */
    fun onClearAllDataClicked()

    /** Dismisses active status / toast message. */
    fun onDismissStatusMessage()

    /** Dismisses active error message. */
    fun onDismissErrorMessage()
}
