package com.example.ui.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HomeMockData
import com.example.model.BalanceSummary
import com.example.model.BudgetMission
import com.example.model.HomeTransaction
import com.example.model.WorkspaceType
import com.example.ui.state.HomeUiState
import com.example.ui.theme.CrispWhiteSurface
import com.example.ui.theme.DeepInkJade
import com.example.ui.theme.ModernLimeAccent
import com.example.ui.theme.MutedNeutral
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.WarmCardBorder
import com.example.ui.theme.WarmDividerLine
import com.example.ui.theme.WarmHighlightPill
import com.example.ui.theme.WarmOffWhiteBg

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onTransactionClick: (String) -> Unit = {},
    onNewMissionClick: () -> Unit = {},
    onViewAllClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onWorkspaceChanged = { viewModel.selectWorkspace(it) },
        onTransactionClick = onTransactionClick,
        onNewMissionClick = onNewMissionClick,
        onViewAllClick = onViewAllClick,
        onRefresh = { viewModel.refreshCurrentWorkspace() },
        modifier = modifier
    )
}

/**
 * Editorial, minimal off-white HomeScreen.
 *
 * NOTE: Bottom Navigation Bar is hosted exclusively in [MainAppContainer]'s Scaffold.
 * HomeScreen receives `innerPadding` from the root Scaffold so content is never cut off
 * or overlapped by the bottom bar.
 *
 * Color System:
 * - Canvas/Scaffold Background: #F8F9FA
 * - Card Surface: #FFFFFF with 1dp solid border #E2E4E0
 * - Primary Text: #18251D
 * - Secondary / Helper Text: #6E7771
 * - Accent Pill & Progress: #B7FF72
 * - Secondary Breakdown Containers: #F1F3EE with 8dp rounded corners
 */
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onWorkspaceChanged: (WorkspaceType) -> Unit,
    onTransactionClick: (String) -> Unit = {},
    onNewMissionClick: () -> Unit = {},
    onViewAllClick: () -> Unit = {},
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmOffWhiteBg)
            .statusBarsPadding()
            .testTag("home_screen_scrollable"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 1. Header with branding & compact segmented switch
        item {
            EditorialHomeHeader(
                activeWorkspace = uiState.activeWorkspace,
                onWorkspaceChanged = onWorkspaceChanged,
                onRefresh = onRefresh
            )
        }

        // Optional loading indicator
        if (uiState.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = DeepInkJade,
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        // 2. Hero Balance Card (Dedicated White Card with 1dp border, 16dp rounded corners, and tactile secondary pills)
        item {
            HeroBalanceCard(
                balanceSummary = uiState.balanceSummary,
                workspace = uiState.activeWorkspace
            )
        }

        // 3. Budget Missions Section
        item {
            BudgetMissionsSection(
                missions = uiState.activeMissions,
                onNewMissionClick = onNewMissionClick
            )
        }

        // 4. Recent Activity Section
        item {
            RecentActivitySection(
                transactions = uiState.recentTransactions,
                onTransactionClick = onTransactionClick,
                onViewAllClick = onViewAllClick
            )
        }

        item {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// =========================================================================
// 1. EDITORIAL HEADER & SEGMENTED WORKSPACE SWITCH
// =========================================================================

@Composable
fun EditorialHomeHeader(
    activeWorkspace: WorkspaceType,
    onWorkspaceChanged: (WorkspaceType) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_header"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // App Title in clean tracked monospace (14sp, bold)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(ModernLimeAccent)
            )
            Text(
                text = "FINITYARC",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                ),
                color = DeepInkJade
            )
        }

        // Compact Segmented Switch: [Personal | Business] with smooth pill indicator
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, WarmCardBorder, RoundedCornerShape(20.dp)),
            color = CrispWhiteSurface
        ) {
            Row(
                modifier = Modifier.padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SegmentedPillItem(
                    title = "Personal",
                    icon = Icons.Default.Person,
                    isSelected = activeWorkspace == WorkspaceType.PERSONAL,
                    onClick = { onWorkspaceChanged(WorkspaceType.PERSONAL) },
                    testTag = "pill_personal"
                )
                SegmentedPillItem(
                    title = "Business",
                    icon = Icons.Default.BusinessCenter,
                    isSelected = activeWorkspace == WorkspaceType.BUSINESS,
                    onClick = { onWorkspaceChanged(WorkspaceType.BUSINESS) },
                    testTag = "pill_business"
                )
            }
        }
    }
}

@Composable
private fun SegmentedPillItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) DeepInkJade else Color.Transparent,
        label = "pill_bg"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) CrispWhiteSurface else MutedNeutral,
        label = "pill_content"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                ),
                color = contentColor
            )
        }
    }
}

// =========================================================================
// 2. HERO BALANCE CARD (DEDICATED WHITE CONTAINER WITH TACTILE PILLS)
// =========================================================================

/**
 * Dedicated Hero Balance Card:
 * - Surface: Pure White (#FFFFFF) with a 1dp solid border (#E2E4E0) on soft background (#F8F9FA)
 * - Shape: RoundedCornerShape(16.dp)
 * - Internal padding: 20.dp
 * - Income and Expense secondary pill boxes (Background: #F1F3EE, 8dp corner radius, 10dp padding)
 */
@Composable
fun HeroBalanceCard(
    balanceSummary: BalanceSummary,
    workspace: WorkspaceType,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, WarmCardBorder, RoundedCornerShape(16.dp))
            .testTag("hero_balance_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CrispWhiteSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Label & Workspace Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "NET BALANCE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.2.sp
                    ),
                    color = MutedNeutral
                )

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = WarmHighlightPill,
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmCardBorder)
                ) {
                    Text(
                        text = if (workspace == WorkspaceType.BUSINESS) "BUSINESS LEDGER" else "PERSONAL VAULT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 0.6.sp
                        ),
                        color = DeepInkJade,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Prominent Net Balance Amount in clean bold typography (32sp)
            Text(
                text = balanceSummary.formattedTotalBalance,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = DeepInkJade,
                modifier = Modifier.testTag("total_balance_text")
            )

            HorizontalDivider(
                thickness = 1.dp,
                color = WarmDividerLine
            )

            // Tactile Secondary Breakdown Pill Containers (#F1F3EE, 8dp radius)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Income Pill Container (#F1F3EE, 8dp radius, 10dp padding)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = WarmHighlightPill,
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(CrispWhiteSurface)
                                .border(1.dp, WarmCardBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Income",
                                tint = DeepInkJade,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "INCOME",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MutedNeutral
                            )
                            Text(
                                text = "+${balanceSummary.formattedTotalIncome}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = DeepInkJade,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Expense Pill Container (#F1F3EE, 8dp radius, 10dp padding)
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = WarmHighlightPill,
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmCardBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(CrispWhiteSurface)
                                .border(1.dp, WarmCardBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Expense",
                                tint = DeepInkJade,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "EXPENSE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MutedNeutral
                            )
                            Text(
                                text = "-${balanceSummary.formattedTotalExpense}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = DeepInkJade,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// 3. BUDGET MISSIONS CAROUSEL / SECTION
// =========================================================================

@Composable
fun BudgetMissionsSection(
    missions: List<BudgetMission>,
    onNewMissionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Header: "ACTIVE MISSIONS" with minimal "+ New" button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "ACTIVE MISSIONS",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.2.sp
                    ),
                    color = MutedNeutral
                )
                if (missions.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(WarmHighlightPill)
                            .border(1.dp, WarmCardBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${missions.size}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = DeepInkJade
                        )
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, WarmCardBorder, RoundedCornerShape(12.dp))
                    .clickable(onClick = onNewMissionClick)
                    .testTag("btn_new_mission"),
                color = CrispWhiteSurface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Mission",
                        tint = DeepInkJade,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "New",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = DeepInkJade
                    )
                }
            }
        }

        // Active Missions Horizontal Carousel or Empty State
        if (missions.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WarmCardBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CrispWhiteSurface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "No active spending missions for this cycle.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedNeutral
                    )
                    TextButton(onClick = onNewMissionClick) {
                        Text(
                            text = "+ Create",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = DeepInkJade
                        )
                    }
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(missions, key = { it.id }) { mission ->
                    BudgetMissionCard(mission = mission)
                }
            }
        }
    }
}

/**
 * Clean card featuring:
 * - Crisp pure white card with 1dp border #E2E4E0
 * - Mission Title ("Food & Groceries under 2,000 SAR")
 * - Days left badge
 * - Smooth horizontal progress bar filled with #B7FF72
 * - Clear "SAR 580 remaining" label
 */
@Composable
fun BudgetMissionCard(
    mission: BudgetMission,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(260.dp)
            .border(1.dp, WarmCardBorder, RoundedCornerShape(14.dp))
            .testTag("mission_card_${mission.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CrispWhiteSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Title and Days left badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = mission.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    ),
                    color = DeepInkJade,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = WarmHighlightPill,
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmCardBorder)
                ) {
                    Text(
                        text = "${mission.daysRemaining}d left",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = MutedNeutral,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            // Smooth horizontal progress bar filled with #B7FF72
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(WarmHighlightPill)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(mission.progressFraction)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(ModernLimeAccent)
                )
            }

            // Remaining spending label & percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${mission.formattedRemaining} remaining",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = DeepInkJade
                )

                Text(
                    text = "${mission.progressPercentage}% used",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    ),
                    color = MutedNeutral
                )
            }
        }
    }
}

// =========================================================================
// 4. RECENT ACTIVITY SECTION & MINIMAL WHITE ROWS
// =========================================================================

@Composable
fun RecentActivitySection(
    transactions: List<HomeTransaction>,
    onTransactionClick: (String) -> Unit,
    onViewAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section title "RECENT ACTIVITY" with "See All"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "RECENT ACTIVITY",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                ),
                color = MutedNeutral
            )

            Text(
                text = "See All",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = DeepInkJade,
                modifier = Modifier
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onViewAllClick
                    )
                    .testTag("btn_see_all_transactions")
            )
        }

        if (transactions.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WarmCardBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CrispWhiteSurface)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No recent transactions recorded.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MutedNeutral
                    )
                }
            }
        } else {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, WarmCardBorder, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CrispWhiteSurface),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    transactions.forEachIndexed { index, tx ->
                        RecentTransactionRow(
                            transaction = tx,
                            onClick = { onTransactionClick(tx.id) },
                            showDivider = index < transactions.lastIndex
                        )
                    }
                }
            }
        }
    }
}

/**
 * Minimal white row:
 * - Circular subtle avatar/glyph
 * - Bold merchant/category name
 * - Clean timestamp
 * - High-contrast amount (-412.50 SAR)
 */
@Composable
fun RecentTransactionRow(
    transaction: HomeTransaction,
    onClick: () -> Unit,
    showDivider: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = WarmCardBorder, bounded = true),
                    onClick = onClick
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
                .testTag("tx_item_${transaction.id}"),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Circular subtle avatar/glyph
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(WarmHighlightPill)
                        .border(1.dp, WarmCardBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = transaction.category.firstOrNull()?.uppercase() ?: "T",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 14.sp
                        ),
                        color = DeepInkJade
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(verticalArrangement = Arrangement.Center) {
                    Text(
                        text = transaction.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        ),
                        color = DeepInkJade,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = transaction.category,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MutedNeutral
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color = MutedNeutral
                        )
                        Text(
                            text = transaction.dateFormatted,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MutedNeutral
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // High-contrast amount
            Text(
                text = transaction.formattedAmount,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace
                ),
                color = DeepInkJade
            )
        }

        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = WarmDividerLine
            )
        }
    }
}

// =========================================================================
// PREVIEWS
// =========================================================================

@Preview(showBackground = true, name = "Editorial Home - Personal Mode")
@Composable
fun EditorialHomePersonalPreview() {
    MyApplicationTheme {
        HomeScreen(
            uiState = HomeMockData.samplePersonalState,
            onWorkspaceChanged = {},
            onTransactionClick = {},
            onNewMissionClick = {},
            onViewAllClick = {}
        )
    }
}

@Preview(showBackground = true, name = "Editorial Home - Business Mode")
@Composable
fun EditorialHomeBusinessPreview() {
    MyApplicationTheme {
        HomeScreen(
            uiState = HomeMockData.sampleBusinessState,
            onWorkspaceChanged = {},
            onTransactionClick = {},
            onNewMissionClick = {},
            onViewAllClick = {}
        )
    }
}
