package com.example.ui.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ExpenseStatus
import com.example.model.HomeTransaction
import com.example.model.TransactionType
import com.example.model.WorkspaceType
import com.example.ui.state.CreateUiState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Senior Architecture ViewModel managing the state machine and business logic
 * for the Create Transaction Screen in FINITYARC.
 */
class CreateTransactionViewModel(
    initialWorkspace: WorkspaceType = WorkspaceType.PERSONAL,
    initialCurrency: String = "SAR"
) : ViewModel(), CreateTransactionActions {

    private val _uiState = MutableStateFlow(
        CreateUiState(
            activeWorkspace = initialWorkspace,
            currencyCode = initialCurrency,
            availableCategories = if (initialWorkspace == WorkspaceType.BUSINESS) {
                CreateUiState.DEFAULT_BUSINESS_CATEGORIES
            } else {
                CreateUiState.DEFAULT_PERSONAL_CATEGORIES
            },
            selectedCategory = if (initialWorkspace == WorkspaceType.BUSINESS) {
                CreateUiState.DEFAULT_BUSINESS_CATEGORIES.first()
            } else {
                CreateUiState.DEFAULT_PERSONAL_CATEGORIES.first()
            }
        )
    )
    val uiState: StateFlow<CreateUiState> = _uiState.asStateFlow()

    private var externalSaveListener: ((HomeTransaction, Boolean) -> Unit)? = null

    fun setOnTransactionSavedListener(listener: (HomeTransaction, Boolean) -> Unit) {
        externalSaveListener = listener
    }

    fun onCurrencyChanged(currency: String) {
        _uiState.update { it.copy(currencyCode = currency) }
    }

    fun clearBuffer() {
        _uiState.update { current ->
            current.copy(
                rawMinorUnitsString = "",
                description = "",
                spentBy = "",
                departmentOrProject = "",
                errorMessage = null
            )
        }
    }

    private val _saveEvents = MutableSharedFlow<SaveResult>()
    val saveEvents: SharedFlow<SaveResult> = _saveEvents.asSharedFlow()

    sealed class SaveResult {
        data class Success(val transaction: HomeTransaction, val batchContinue: Boolean) : SaveResult()
        data class Error(val message: String) : SaveResult()
    }

    override fun onDigitPressed(digit: Char) {
        if (!digit.isDigit()) return
        _uiState.update { current ->
            val currentBuffer = current.rawMinorUnitsString
            // Prevent entering leading zeros on empty buffer
            if (currentBuffer.isEmpty() && digit == '0') {
                return@update current
            }
            // Max 9 digits prevents integer overflow (999,999,999 minor units = 9,999,999.99 SAR)
            if (currentBuffer.length >= 9) {
                return@update current
            }
            current.copy(
                rawMinorUnitsString = currentBuffer + digit,
                errorMessage = null
            )
        }
    }

    override fun onBackspace() {
        _uiState.update { current ->
            val currentBuffer = current.rawMinorUnitsString
            if (currentBuffer.isEmpty()) {
                current
            } else {
                current.copy(
                    rawMinorUnitsString = currentBuffer.dropLast(1),
                    errorMessage = null
                )
            }
        }
    }

    override fun onClear() {
        _uiState.update { current ->
            current.copy(
                rawMinorUnitsString = "",
                errorMessage = null
            )
        }
    }

    override fun onTypeSelected(type: TransactionType) {
        _uiState.update { current ->
            current.copy(transactionType = type)
        }
    }

    override fun onCategorySelected(category: String) {
        _uiState.update { current ->
            current.copy(selectedCategory = category)
        }
    }

    override fun onDescriptionChanged(description: String) {
        _uiState.update { current ->
            current.copy(description = description)
        }
    }

    override fun onWorkspaceChanged(workspace: WorkspaceType) {
        _uiState.update { current ->
            val categories = if (workspace == WorkspaceType.BUSINESS) {
                CreateUiState.DEFAULT_BUSINESS_CATEGORIES
            } else {
                CreateUiState.DEFAULT_PERSONAL_CATEGORIES
            }
            current.copy(
                activeWorkspace = workspace,
                availableCategories = categories,
                selectedCategory = categories.first()
            )
        }
    }

    override fun onBusinessFieldChanged(
        spentBy: String,
        departmentOrProject: String,
        status: ExpenseStatus
    ) {
        _uiState.update { current ->
            current.copy(
                spentBy = spentBy,
                departmentOrProject = departmentOrProject,
                expenseStatus = status
            )
        }
    }

    override fun onSave(resetForm: Boolean) {
        val currentState = _uiState.value

        if (currentState.amountMinorUnits <= 0L) {
            _uiState.update { it.copy(errorMessage = "Please enter an amount greater than 0.00 SAR") }
            return
        }

        if (currentState.activeWorkspace == WorkspaceType.BUSINESS) {
            if (currentState.spentBy.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Please enter who spent or authorized this business expense") }
                return
            }
            if (currentState.departmentOrProject.isBlank()) {
                _uiState.update { it.copy(errorMessage = "Please assign a department or project code") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            val timestamp = SimpleDateFormat("MMM dd, yyyy", Locale.US).format(Date())
            val title = if (currentState.description.isNotBlank()) {
                currentState.description.trim()
            } else {
                "${currentState.selectedCategory} ${currentState.transactionType.displayName}"
            }

            val savedTransaction = HomeTransaction(
                id = UUID.randomUUID().toString(),
                title = title,
                category = currentState.selectedCategory,
                amountMinorUnits = currentState.amountMinorUnits,
                isExpense = currentState.transactionType == TransactionType.EXPENSE,
                dateFormatted = timestamp,
                spentBy = if (currentState.activeWorkspace == WorkspaceType.BUSINESS) {
                    currentState.spentBy.trim()
                } else null
            )

            _saveEvents.emit(SaveResult.Success(savedTransaction, batchContinue = resetForm))
            externalSaveListener?.invoke(savedTransaction, resetForm)

            if (resetForm) {
                // Reset amount buffer and description for rapid batch entry ("+ Another")
                _uiState.update { current ->
                    current.copy(
                        rawMinorUnitsString = "",
                        description = "",
                        isSubmitting = false,
                        errorMessage = null
                    )
                }
            } else {
                // Clear the buffer after saving as requested
                _uiState.update { current ->
                    current.copy(
                        rawMinorUnitsString = "",
                        description = "",
                        spentBy = "",
                        departmentOrProject = "",
                        isSubmitting = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    override fun onDismissError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
