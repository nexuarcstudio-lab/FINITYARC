package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val MinimalCleanColorScheme = lightColorScheme(
    primary = AppTextDark,
    onPrimary = AppPureWhite,
    primaryContainer = AppIndicatorHighlight,
    onPrimaryContainer = AppTextDark,
    secondary = AppAccentLime,
    onSecondary = AppTextDark,
    secondaryContainer = AppCardSurface,
    onSecondaryContainer = AppTextDark,
    tertiary = AppPositiveGreen,
    background = AppPureWhite,
    onBackground = AppTextDark,
    surface = AppCardSurface,
    onSurface = AppTextDark,
    surfaceVariant = AppCardSurface,
    onSurfaceVariant = AppTextSecondary,
    outline = AppBorderSubtle,
    outlineVariant = AppBorderSubtle
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Enforce clean minimalist light by default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = MinimalCleanColorScheme,
        typography = Typography,
        content = content
    )
}
