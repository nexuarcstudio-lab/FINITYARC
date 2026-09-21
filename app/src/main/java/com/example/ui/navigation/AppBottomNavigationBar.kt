package com.example.ui.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CrispWhiteSurface
import com.example.ui.theme.DeepInkJade
import com.example.ui.theme.ModernLimeAccent
import com.example.ui.theme.MutedNeutral
import com.example.ui.theme.WarmCardBorder

/**
 * Clean, modern minimalist Bottom Navigation Bar for FINITYARC.
 *
 * Requirements:
 * - Hosted exclusively in Scaffold(bottomBar = { AppBottomNavigationBar(...) }).
 * - Native windowInsets = WindowInsets.navigationBars ensures labels and icons
 *   ("Home", "Add", "Settings") sit cleanly above the Android gesture/pill bar.
 * - Background: Pure White (#FFFFFF) with a 1dp top border (#E2E4E0).
 * - Selected icon/label: #18251D (high contrast).
 * - Unselected icon/label: #6E7771 (muted neutral).
 * - Indicator color: Modern Lime Accent (#B7FF72) subtle pill highlight.
 */
@Composable
fun AppBottomNavigationBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = WarmCardBorder)
            .testTag("app_bottom_navigation_bar"),
        containerColor = CrispWhiteSurface,
        contentColor = DeepInkJade,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets.navigationBars
    ) {
        Screen.values().forEach { screen ->
            val isSelected = screen == currentScreen
            NavigationBarItem(
                selected = isSelected,
                onClick = { onScreenSelected(screen) },
                icon = {
                    Icon(
                        imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.label,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        text = screen.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 11.sp,
                            letterSpacing = 0.2.sp
                        )
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = DeepInkJade,
                    selectedTextColor = DeepInkJade,
                    unselectedIconColor = MutedNeutral,
                    unselectedTextColor = MutedNeutral,
                    indicatorColor = ModernLimeAccent
                ),
                modifier = Modifier.testTag("nav_item_${screen.name.lowercase()}")
            )
        }
    }
}
