package com.example.ui.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ExpenseStatus
import com.example.model.TransactionType
import com.example.model.WorkspaceType
import com.example.ui.create.components.BusinessDetailsCard
import com.example.ui.create.components.CategoryPillSelector
import com.example.ui.create.components.HeroAmountDisplay
import com.example.ui.create.components.ModernTactileKeypad
import com.example.ui.create.components.MonoBackground
import com.example.ui.create.components.MonoBorder
import com.example.ui.create.components.MonoSurface
import com.example.ui.create.components.MonoTextPrimary
import com.example.ui.create.components.MonoTextSubtle
import com.example.ui.state.CreateUiState
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CreateTransactionRoute(
    viewModel: CreateTransactionViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.saveEvents.collectLatest { event ->
            when (event) {
                is CreateTransactionViewModel.SaveResult.Success -> {
                    if (!event.batchContinue) {
                        onNavigateBack()
                    } else {
                        snackbarHostState.showSnackbar("Transaction saved! Ready for next.")
                    }
                }
                is CreateTransactionViewModel.SaveResult.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onDismissError()
        }
    }

    CreateScreen(
        uiState = uiState,
        actions = viewModel,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

/**
 * Complete `CreateScreen` Composable combining all components from previous steps.
 *
 * Structure:
 * 1. Top Area (Scrollable):
 *    - Screen title ("NEW ENTRY") and current mode indicator chip.
 *    - HeroAmountDisplay.
 *    - CategoryPillSelector.
 *    - Description / Notes text input (soft gray rounded card, no harsh border).
 *    - BusinessDetailsCard (expands smoothly when in Business mode).
 * 2. Bottom Docked Area:
 *    - ModernTactileKeypad.
 *    - Action Button Bar:
 *      * "+ Another" button: Outlined 1dp black border, white fill, black bold text.
 *      * "Save Entry" button: Solid high-contrast black pill button, bold white text, height 52dp.
 */
@Composable
fun CreateScreen(
    uiState: CreateUiState,
    actions: CreateTransactionActions,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding(),
        containerColor = MonoBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            // Bottom Docked Area: ModernTactileKeypad + Action Button Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("create_screen_docked_bottom"),
                color = MonoBackground,
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // ModernTactileKeypad
                    ModernTactileKeypad(
                        onDigitPressed = { actions.onDigitPressed(it) },
                        onBackspace = { actions.onBackspace() },
                        onClear = { actions.onClear() }
                    )

                    // Action Button Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // "+ Another" button: Outlined 1dp black border, white fill, black bold text
                        OutlinedButton(
                            onClick = { actions.onSave(resetForm = true) },
                            enabled = uiState.canSubmit && !uiState.isSubmitting,
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .testTag("save_and_add_another_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MonoBackground,
                                contentColor = MonoTextPrimary,
                                disabledContainerColor = MonoBackground,
                                disabledContentColor = MonoTextSubtle
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MonoTextPrimary)
                        ) {
                            Text(
                                text = "+ Another",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.2.sp
                                )
                            )
                        }

                        // "Save Entry" button: Solid high-contrast black pill button, bold white text, height 52dp
                        Button(
                            onClick = { actions.onSave(resetForm = false) },
                            enabled = uiState.canSubmit && !uiState.isSubmitting,
                            modifier = Modifier
                                .weight(1.4f)
                                .height(52.dp)
                                .testTag("save_transaction_button"),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MonoTextPrimary,
                                contentColor = MonoBackground,
                                disabledContainerColor = MonoSurface,
                                disabledContentColor = MonoTextSubtle
                            )
                        ) {
                            if (uiState.isSubmitting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MonoBackground,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Save Entry",
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.3.sp
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // Top Area (Scrollable)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .testTag("create_screen_scrollable_top"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Screen Header: Title ("NEW ENTRY") and current mode indicator chip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Navigate Back",
                        tint = MonoTextPrimary
                    )
                }

                Text(
                    text = "NEW ENTRY",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = MonoTextPrimary
                )

                // Current mode indicator chip / Workspace switcher
                Surface(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .border(1.dp, MonoBorder, RoundedCornerShape(20.dp)),
                    color = MonoSurface
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        WorkspacePill(
                            title = "Personal",
                            icon = Icons.Default.Person,
                            isSelected = uiState.activeWorkspace == WorkspaceType.PERSONAL,
                            onClick = { actions.onWorkspaceChanged(WorkspaceType.PERSONAL) },
                            modifier = Modifier.testTag("create_workspace_personal")
                        )
                        WorkspacePill(
                            title = "Business",
                            icon = Icons.Default.BusinessCenter,
                            isSelected = uiState.activeWorkspace == WorkspaceType.BUSINESS,
                            onClick = { actions.onWorkspaceChanged(WorkspaceType.BUSINESS) },
                            modifier = Modifier.testTag("create_workspace_business")
                        )
                    }
                }
            }

            // 1. HeroAmountDisplay
            HeroAmountDisplay(
                rawMinorUnitsString = uiState.rawMinorUnitsString,
                currencyCode = uiState.currencyCode,
                transactionType = uiState.transactionType,
                onTypeSelected = { actions.onTypeSelected(it) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // 2. CategoryPillSelector
            CategoryPillSelector(
                categories = uiState.availableCategories,
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { actions.onCategorySelected(it) }
            )

            // 3. Description / Notes text input (soft gray rounded card, no harsh border)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { actions.onDescriptionChanged(it) },
                    placeholder = { Text("Add memo or note (optional)", color = MonoTextSubtle) },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MonoSurface,
                        unfocusedContainerColor = MonoSurface,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = MonoTextPrimary,
                        unfocusedTextColor = MonoTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("description_input")
                )
            }

            // 4. BusinessDetailsCard (expands smoothly when in Business mode)
            BusinessDetailsCard(
                activeWorkspace = uiState.activeWorkspace,
                spentBy = uiState.spentBy,
                departmentOrProject = uiState.departmentOrProject,
                expenseStatus = uiState.expenseStatus,
                onSpentByChanged = {
                    actions.onBusinessFieldChanged(it, uiState.departmentOrProject, uiState.expenseStatus)
                },
                onDepartmentChanged = {
                    actions.onBusinessFieldChanged(uiState.spentBy, it, uiState.expenseStatus)
                },
                onStatusSelected = {
                    actions.onBusinessFieldChanged(uiState.spentBy, uiState.departmentOrProject, it)
                }
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Backward compatibility alias for [CreateScreen].
 */
@Composable
fun CreateTransactionScreen(
    uiState: CreateUiState,
    actions: CreateTransactionActions,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    modifier: Modifier = Modifier
) {
    CreateScreen(
        uiState = uiState,
        actions = actions,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

@Composable
private fun WorkspacePill(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MonoTextPrimary else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MonoBackground else MonoTextSubtle,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                ),
                color = if (isSelected) MonoBackground else MonoTextSubtle
            )
        }
    }
}

// =========================================================================
// PREVIEWS
// =========================================================================

@Preview(showBackground = true, name = "Create Screen - Personal Mode")
@Composable
fun CreateScreenPersonalPreview() {
    MyApplicationTheme {
        CreateScreen(
            uiState = CreateUiState(
                activeWorkspace = WorkspaceType.PERSONAL,
                rawMinorUnitsString = "1250",
                selectedCategory = "Food & Dining",
                description = "Lunch meeting at bistro"
            ),
            actions = object : CreateTransactionActions {
                override fun onDigitPressed(digit: Char) {}
                override fun onBackspace() {}
                override fun onClear() {}
                override fun onTypeSelected(type: TransactionType) {}
                override fun onCategorySelected(category: String) {}
                override fun onDescriptionChanged(description: String) {}
                override fun onWorkspaceChanged(workspace: WorkspaceType) {}
                override fun onBusinessFieldChanged(
                    spentBy: String,
                    departmentOrProject: String,
                    status: ExpenseStatus
                ) {}
                override fun onSave(resetForm: Boolean) {}
                override fun onDismissError() {}
            },
            onNavigateBack = {}
        )
    }
}

@Preview(showBackground = true, name = "Create Screen - Business Mode")
@Composable
fun CreateScreenBusinessPreview() {
    MyApplicationTheme {
        CreateScreen(
            uiState = CreateUiState(
                activeWorkspace = WorkspaceType.BUSINESS,
                rawMinorUnitsString = "840000",
                availableCategories = CreateUiState.DEFAULT_BUSINESS_CATEGORIES,
                selectedCategory = "SaaS & Software",
                description = "Annual cloud server renewal",
                spentBy = "Sarah Lead",
                departmentOrProject = "Core Infrastructure",
                expenseStatus = ExpenseStatus.PENDING
            ),
            actions = object : CreateTransactionActions {
                override fun onDigitPressed(digit: Char) {}
                override fun onBackspace() {}
                override fun onClear() {}
                override fun onTypeSelected(type: TransactionType) {}
                override fun onCategorySelected(category: String) {}
                override fun onDescriptionChanged(description: String) {}
                override fun onWorkspaceChanged(workspace: WorkspaceType) {}
                override fun onBusinessFieldChanged(
                    spentBy: String,
                    departmentOrProject: String,
                    status: ExpenseStatus
                ) {}
                override fun onSave(resetForm: Boolean) {}
                override fun onDismissError() {}
            },
            onNavigateBack = {}
        )
    }
}
