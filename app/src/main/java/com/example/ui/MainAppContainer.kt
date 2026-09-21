package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.WorkspaceType
import com.example.ui.create.CreateScreen
import com.example.ui.create.CreateTransactionViewModel
import com.example.ui.home.HomeScreen
import com.example.ui.navigation.AppBottomNavigationBar
import com.example.ui.navigation.Screen
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WarmOffWhiteBg
import kotlinx.coroutines.flow.collectLatest

/**
 * Root application container wrapping FINITYARC in an editorial Scaffold with a persistent Bottom Navigation Bar.
 *
 * Architecture & State Integration:
 * 1. [MainAppViewModel] acts as the single unified reactive state holder (`MainAppState`) at the root container level.
 *    - `selectedCurrency` updates across all screens dynamically.
 *    - `activeWorkspace` seamlessly keeps personal and business ledgers in sync.
 *    - `currentTransactions` contains the live list of transactions, dynamically prepending new entries.
 *    - `totalIncome`, `totalExpense`, `netBalance`, and `activeMissions` are dynamically recomputed.
 * 2. When an entry is saved on [CreateScreen]:
 *    - It calls `mainAppViewModel.onSaveTransaction(newTransaction)`.
 *    - The transaction list updates immediately on [HomeScreen].
 *    - The numeric keypad buffer on [CreateScreen] is completely cleared.
 *    - The app smoothly navigates back to [HomeScreen] (or displays a snackbar if batch "+ Another" was tapped).
 * 3. When currency is switched on [SettingsScreen]:
 *    - It calls `mainAppViewModel.setCurrency(newCurrency)`.
 *    - Every card, balance display, mission target, and transaction row across the entire app updates instantly.
 * 4. The Bottom Navigation Bar is placed strictly in `Scaffold(bottomBar = { AppBottomNavigationBar(...) })`.
 *    `innerPadding` is passed directly to the active screen, ensuring zero UI clipping or overlapping.
 */
@Composable
fun MainAppContainer(
    mainAppViewModel: MainAppViewModel = viewModel(),
    createViewModel: CreateTransactionViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel(),
    initialScreen: Screen = Screen.HOME,
    initialWorkspace: WorkspaceType = WorkspaceType.PERSONAL,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf(initialScreen) }

    val appState by mainAppViewModel.appState.collectAsStateWithLifecycle()
    val createUiState by createViewModel.uiState.collectAsStateWithLifecycle()
    val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Connect CreateViewModel's save callback to MainAppViewModel
    LaunchedEffect(mainAppViewModel, createViewModel) {
        createViewModel.setOnTransactionSavedListener { transaction, batchContinue ->
            mainAppViewModel.onSaveTransaction(transaction, batchContinue)
        }
    }

    // Connect SettingsViewModel's currency change callback to MainAppViewModel
    LaunchedEffect(mainAppViewModel, settingsViewModel) {
        settingsViewModel.setOnCurrencyChangedListener { currency ->
            mainAppViewModel.setCurrency(currency)
        }
    }

    // Sync appState currency and workspace into child ViewModels
    LaunchedEffect(appState.selectedCurrency) {
        createViewModel.onCurrencyChanged(appState.selectedCurrency)
        settingsViewModel.updateSelectedCurrency(appState.selectedCurrency)
    }

    LaunchedEffect(appState.activeWorkspace) {
        createViewModel.onWorkspaceChanged(appState.activeWorkspace)
        settingsViewModel.onWorkspaceToggle(appState.activeWorkspace)
    }

    // Keep transaction count in settings in sync with live list
    LaunchedEffect(appState.currentTransactions.size) {
        settingsViewModel.updateStoredTransactionCount(appState.currentTransactions.size)
    }

    // Collect navigation events from MainAppViewModel (e.g. return to Home after saving)
    LaunchedEffect(mainAppViewModel) {
        mainAppViewModel.navigationEvents.collectLatest { event ->
            when (event) {
                is MainAppViewModel.NavigationEvent.NavigateToHome -> {
                    currentScreen = Screen.HOME
                }
                is MainAppViewModel.NavigationEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    fun updateSharedWorkspace(newWorkspace: WorkspaceType) {
        mainAppViewModel.selectWorkspace(newWorkspace)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_app_container"),
        containerColor = WarmOffWhiteBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppBottomNavigationBar(
                currentScreen = currentScreen,
                onScreenSelected = { currentScreen = it }
            )
        }
    ) { innerPadding ->
        when (currentScreen) {
            Screen.HOME -> {
                HomeScreen(
                    uiState = appState.toHomeUiState(),
                    onWorkspaceChanged = { updateSharedWorkspace(it) },
                    onTransactionClick = { /* Navigate to detail */ },
                    onNewMissionClick = { currentScreen = Screen.CREATE },
                    onViewAllClick = { /* View all */ },
                    onRefresh = { mainAppViewModel.refreshCurrentWorkspace() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
            Screen.CREATE -> {
                CreateScreen(
                    uiState = createUiState.copy(
                        activeWorkspace = appState.activeWorkspace,
                        currencyCode = appState.selectedCurrency
                    ),
                    actions = createViewModel,
                    onNavigateBack = { currentScreen = Screen.HOME },
                    snackbarHostState = snackbarHostState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
            Screen.SETTINGS -> {
                SettingsScreen(
                    uiState = settingsUiState.copy(
                        activeWorkspace = appState.activeWorkspace,
                        selectedCurrency = appState.selectedCurrency,
                        totalStoredTransactionsCount = appState.currentTransactions.size
                    ),
                    actions = settingsViewModel,
                    onNavigateBack = { currentScreen = Screen.HOME },
                    snackbarHostState = snackbarHostState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }
    }
}

// =========================================================================
// PREVIEWS
// =========================================================================

@Preview(showBackground = true, name = "Main App - Home Screen")
@Composable
fun MainAppContainerHomePreview() {
    MyApplicationTheme {
        MainAppContainer(
            initialScreen = Screen.HOME,
            initialWorkspace = WorkspaceType.PERSONAL
        )
    }
}

@Preview(showBackground = true, name = "Main App - Add Entry Screen")
@Composable
fun MainAppContainerCreatePreview() {
    MyApplicationTheme {
        MainAppContainer(
            initialScreen = Screen.CREATE,
            initialWorkspace = WorkspaceType.PERSONAL
        )
    }
}

@Preview(showBackground = true, name = "Main App - Settings Screen")
@Composable
fun MainAppContainerSettingsPreview() {
    MyApplicationTheme {
        MainAppContainer(
            initialScreen = Screen.SETTINGS,
            initialWorkspace = WorkspaceType.PERSONAL
        )
    }
}
