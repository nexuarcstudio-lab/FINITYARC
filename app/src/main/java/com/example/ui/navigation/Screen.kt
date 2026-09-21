package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation Destinations for FINITYARC root navigation.
 */
enum class Screen(
    val label: String,
    val unselectedIcon: ImageVector,
    val selectedIcon: ImageVector
) {
    HOME(
        label = "Home",
        unselectedIcon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home
    ),
    CREATE(
        label = "Add",
        unselectedIcon = Icons.Outlined.AddCircleOutline,
        selectedIcon = Icons.Filled.AddCircle
    ),
    SETTINGS(
        label = "Settings",
        unselectedIcon = Icons.Outlined.Settings,
        selectedIcon = Icons.Filled.Settings
    )
}
