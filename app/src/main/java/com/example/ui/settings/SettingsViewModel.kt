package com.example.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HomeMockData
import com.example.model.ExportableTransaction
import com.example.model.WorkspaceType
import com.example.ui.state.SettingsUiState
import com.example.util.CsvLedgerExporter
import com.example.util.OfflineDataExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel managing settings state and local data export/import workflows.
 */
class SettingsViewModel(
    private val exporter: OfflineDataExporter = CsvLedgerExporter
) : ViewModel(), SettingsActions {

    private val _uiState = MutableStateFlow(
        SettingsUiState(
            activeWorkspace = WorkspaceType.PERSONAL,
            selectedCurrency = "SAR",
            totalStoredTransactionsCount = HomeMockData.personalTransactions.size,
            lastBackupTimestamp = System.currentTimeMillis() - 86400000L // 1 day ago
        )
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private var onCurrencyChangedListener: ((String) -> Unit)? = null

    fun setOnCurrencyChangedListener(listener: (String) -> Unit) {
        onCurrencyChangedListener = listener
    }

    fun updateSelectedCurrency(currency: String) {
        _uiState.update { it.copy(selectedCurrency = currency) }
    }

    fun updateStoredTransactionCount(count: Int) {
        _uiState.update { it.copy(totalStoredTransactionsCount = count) }
    }

    override fun onWorkspaceToggle(workspace: WorkspaceType) {
        _uiState.update { current ->
            val count = if (workspace == WorkspaceType.BUSINESS) {
                HomeMockData.businessTransactions.size
            } else {
                HomeMockData.personalTransactions.size
            }
            current.copy(
                activeWorkspace = workspace,
                totalStoredTransactionsCount = count
            )
        }
    }

    override fun onCurrencyChanged(currency: String) {
        _uiState.update { it.copy(selectedCurrency = currency) }
        onCurrencyChangedListener?.invoke(currency)
    }

    override fun onToggleBiometricLock(enabled: Boolean) {
        _uiState.update { it.copy(isBiometricLockEnabled = enabled) }
    }

    override fun onExportCsvClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isExporting = true) }

            val transactions = if (_uiState.value.activeWorkspace == WorkspaceType.BUSINESS) {
                HomeMockData.sampleBusinessState.recentTransactions
            } else {
                HomeMockData.samplePersonalState.recentTransactions
            }

            val csv = exporter.generateCsvString(transactions)
            val now = System.currentTimeMillis()

            _uiState.update { current ->
                current.copy(
                    isExporting = false,
                    lastExportedCsv = csv,
                    lastBackupTimestamp = now,
                    statusMessage = "Successfully exported ${transactions.size} records to CSV"
                )
            }
        }
    }

    override fun onImportCsvClicked() {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }

            // If a previous export exists, re-parse it; otherwise generate a valid mock CSV
            val csvToImport = _uiState.value.lastExportedCsv ?: exporter.generateCsvString(
                HomeMockData.samplePersonalState.recentTransactions
            )

            val parsed = exporter.parseCsvStringToTransactions(csvToImport)

            _uiState.update { current ->
                current.copy(
                    isImporting = false,
                    totalStoredTransactionsCount = parsed.size,
                    statusMessage = "Successfully parsed ${parsed.size} transactions from CSV"
                )
            }
        }
    }

    override fun onClearAllDataClicked() {
        _uiState.update { current ->
            current.copy(
                totalStoredTransactionsCount = 0,
                lastExportedCsv = null,
                statusMessage = "All ledger entries cleared from device"
            )
        }
    }

    override fun onDismissStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    override fun onDismissErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
