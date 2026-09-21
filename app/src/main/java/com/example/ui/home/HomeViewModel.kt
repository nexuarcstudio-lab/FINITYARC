package com.example.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.HomeMockData
import com.example.model.WorkspaceType
import com.example.ui.state.HomeUiState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Senior Architect ViewModel orchestrating the offline-first Home Screen state.
 * Maintains an immutable [HomeUiState] StateFlow adhering to unidirectional data flow (UDF).
 */
class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeMockData.getPersonalState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    /**
     * Switches the active workspace between Personal and Business ledgers.
     */
    fun selectWorkspace(workspace: WorkspaceType) {
        if (_uiState.value.activeWorkspace == workspace) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // Brief simulation of local database retrieval
            delay(150)
            val newState = HomeMockData.getStateForWorkspace(workspace)
            _uiState.value = newState
        }
    }

    /**
     * Refreshes the active workspace dataset.
     */
    fun refreshCurrentWorkspace() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            delay(200)
            val currentWorkspace = _uiState.value.activeWorkspace
            _uiState.value = HomeMockData.getStateForWorkspace(currentWorkspace)
        }
    }
}
