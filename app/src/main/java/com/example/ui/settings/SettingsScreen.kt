package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material.icons.filled.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.WorkspaceType
import com.example.ui.settings.components.SettingsActionRow
import com.example.ui.settings.components.SettingsMonoBackground
import com.example.ui.settings.components.SettingsMonoBorder
import com.example.ui.settings.components.SettingsMonoSurface
import com.example.ui.settings.components.SettingsMonoTextPrimary
import com.example.ui.settings.components.SettingsMonoTextSecondary
import com.example.ui.settings.components.SettingsSectionContainer
import com.example.ui.settings.components.SettingsSelectorRow
import com.example.ui.settings.components.SettingsToggleRow
import com.example.ui.state.SettingsUiState
import com.example.ui.theme.MyApplicationTheme

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.onDismissStatusMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.onDismissErrorMessage()
        }
    }

    SettingsScreen(
        uiState = uiState,
        actions = viewModel,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

/**
 * Complete `SettingsScreen` Composable combining all settings modular components.
 *
 * Requirements:
 * 1. Screen Layout:
 *    - Header with screen title ("SETTINGS") and minimalist offline badge ("100% OFFLINE").
 *    - Scrollable list containing:
 *      * Section 1: Mode & Preferences
 *        - Active workspace switch (Personal vs Business).
 *        - Currency selector row (displays current: SAR).
 *      * Section 2: Data & Portability (Core Offline Features)
 *        - "Export to CSV" (shows stored transaction count subtitle).
 *        - "Import from CSV" (shows last backup date).
 *      * Section 3: Privacy & Security
 *        - "App Lock / Biometric" toggle.
 *      * Section 4: Danger Zone
 *        - "Erase All Data" row (triggers confirmation alert).
 *      * Footer: Minimal app version text ("v1.0.0 • No Cloud • Zero Tracking").
 * 2. Danger Zone Alert:
 *    - AlertDialog prompting the user with a warning before wiping offline SQLite records.
 */
@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    actions: SettingsActions,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    var showEraseConfirmDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = SettingsMonoBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .testTag("settings_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Navigation Back, Title ("SETTINGS"), and "100% OFFLINE" Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SettingsMonoTextPrimary
                        )
                    }

                    Text(
                        text = "SETTINGS",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp
                        ),
                        color = SettingsMonoTextPrimary
                    )
                }

                // Minimalist Offline Badge
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, SettingsMonoBorder, RoundedCornerShape(20.dp)),
                    color = SettingsMonoSurface
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = SettingsMonoTextPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "100% OFFLINE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 0.5.sp
                            ),
                            color = SettingsMonoTextPrimary
                        )
                    }
                }
            }

            // SECTION 1: MODE & PREFERENCES
            SettingsSectionContainer(title = "Mode & Preferences") {
                // Active Workspace Switcher
                SettingsSelectorRow(
                    title = "Active Workspace",
                    subtitle = if (uiState.activeWorkspace == WorkspaceType.BUSINESS) {
                        "Corporate mode with team attribution and receipts"
                    } else {
                        "Private personal expenditure ledger"
                    },
                    selectedValue = if (uiState.activeWorkspace == WorkspaceType.BUSINESS) "Business" else "Personal",
                    options = listOf("Personal", "Business"),
                    onOptionSelected = { selected ->
                        val workspace = if (selected == "Business") WorkspaceType.BUSINESS else WorkspaceType.PERSONAL
                        actions.onWorkspaceToggle(workspace)
                    },
                    icon = Icons.Default.WorkOutline
                )

                // Currency Selector Row
                SettingsSelectorRow(
                    title = "Ledger Currency",
                    subtitle = "Current base currency: ${uiState.selectedCurrency}",
                    selectedValue = uiState.selectedCurrency,
                    options = uiState.availableCurrencies,
                    onOptionSelected = { actions.onCurrencyChanged(it) },
                    icon = Icons.Default.Payments,
                    showDivider = false
                )
            }

            // SECTION 2: DATA & PORTABILITY (CORE OFFLINE FEATURES)
            SettingsSectionContainer(title = "Data & Portability") {
                // Export to CSV
                SettingsActionRow(
                    title = "Export to CSV",
                    subtitle = if (uiState.isExporting) {
                        "Exporting ledger..."
                    } else {
                        "${uiState.totalStoredTransactionsCount} transactions available for offline export"
                    },
                    onClick = {
                        if (!uiState.isExporting) actions.onExportCsvClicked()
                    },
                    icon = Icons.Default.Download
                )

                // Import from CSV
                SettingsActionRow(
                    title = "Import from CSV",
                    subtitle = if (uiState.isImporting) {
                        "Reading file..."
                    } else {
                        "Last backup: ${uiState.formattedLastBackup}"
                    },
                    onClick = {
                        if (!uiState.isImporting) actions.onImportCsvClicked()
                    },
                    icon = Icons.Default.Upload,
                    showDivider = false
                )
            }

            // SECTION 3: PRIVACY & SECURITY
            SettingsSectionContainer(title = "Privacy & Security") {
                // App Lock / Biometric Toggle
                SettingsToggleRow(
                    title = "App Lock / Biometric",
                    subtitle = if (uiState.isBiometricLockEnabled) {
                        "Fingerprint / Face authentication required on launch"
                    } else {
                        "Unlock without biometric credentials"
                    },
                    checked = uiState.isBiometricLockEnabled,
                    onCheckedChange = { actions.onToggleBiometricLock(it) },
                    icon = Icons.Default.Fingerprint,
                    showDivider = false
                )
            }

            // SECTION 4: DANGER ZONE
            SettingsSectionContainer(title = "Danger Zone") {
                // Erase All Data Row
                SettingsActionRow(
                    title = "Erase All Data",
                    subtitle = "Permanently wipe all local transactions from this device",
                    onClick = { showEraseConfirmDialog = true },
                    icon = Icons.Default.DeleteForever,
                    isDestructive = true,
                    showChevron = false,
                    showDivider = false
                )
            }

            // FOOTER: APP VERSION & OFFLINE MANIFESTO
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = SettingsMonoTextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "FINITYARC OFFLINE LEDGER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        ),
                        color = SettingsMonoTextSecondary
                    )
                }

                Text(
                    text = "v${uiState.appVersion} • No Cloud • Zero Tracking",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    ),
                    color = SettingsMonoTextSecondary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "All financial records remain strictly on your device storage.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = SettingsMonoTextSecondary
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // DANGER ZONE CONFIRMATION DIALOG
    if (showEraseConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showEraseConfirmDialog = false },
            containerColor = SettingsMonoBackground,
            shape = RoundedCornerShape(16.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SettingsMonoTextPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WarningAmber,
                        contentDescription = "Warning",
                        tint = SettingsMonoBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
            },
            title = {
                Text(
                    text = "Erase All Offline Data?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.2.sp
                    ),
                    color = SettingsMonoTextPrimary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Text(
                    text = "This will permanently wipe all stored transactions from this device. Because FINITYARC operates 100% offline with zero cloud servers, this action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SettingsMonoTextSecondary,
                    textAlign = TextAlign.Center
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEraseConfirmDialog = false
                        actions.onClearAllDataClicked()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SettingsMonoTextPrimary,
                        contentColor = SettingsMonoBackground
                    ),
                    modifier = Modifier.testTag("confirm_erase_button")
                ) {
                    Text(
                        text = "Erase Everything",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showEraseConfirmDialog = false },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = SettingsMonoTextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SettingsMonoBorder),
                    modifier = Modifier.testTag("cancel_erase_button")
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        )
    }
}

// =========================================================================
// PREVIEWS
// =========================================================================

@Preview(showBackground = true, name = "Settings Screen Preview")
@Composable
fun SettingsScreenPreview() {
    MyApplicationTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                activeWorkspace = WorkspaceType.PERSONAL,
                selectedCurrency = "SAR",
                availableCurrencies = listOf("SAR", "USD", "EUR", "AED", "GBP"),
                isBiometricLockEnabled = true,
                totalStoredTransactionsCount = 42,
                lastBackupTimestamp = System.currentTimeMillis() - 3600000L, // 1 hr ago
                appVersion = "1.0.0"
            ),
            actions = object : SettingsActions {
                override fun onWorkspaceToggle(workspace: WorkspaceType) {}
                override fun onCurrencyChanged(currency: String) {}
                override fun onToggleBiometricLock(enabled: Boolean) {}
                override fun onExportCsvClicked() {}
                override fun onImportCsvClicked() {}
                override fun onClearAllDataClicked() {}
                override fun onDismissStatusMessage() {}
                override fun onDismissErrorMessage() {}
            },
            onNavigateBack = {}
        )
    }
}
