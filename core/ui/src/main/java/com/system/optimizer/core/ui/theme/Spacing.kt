package com.system.optimizer.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Centralized spacing scale used across screens. Composables can read via
 * `AppTheme.spacing.md` instead of sprinkling magic dp values throughout the layout.
 *
 * The scale follows an 8dp baseline with explicit half-step (4dp) and double-step
 * (32dp / 48dp) values so common Material density patterns are covered without
 * arbitrary numbers.
 */
@Immutable
data class AppSpacing(
    val xxs: Dp = 2.dp,
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 16.dp,
    val lg: Dp = 24.dp,
    val xl: Dp = 32.dp,
    val xxl: Dp = 48.dp
)

val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }

/**
 * App-level theme accessor. Used as a side-channel to [androidx.compose.material3.MaterialTheme]
 * for tokens that aren't part of the official M3 token system (currently spacing).
 */
object AppTheme {
    val spacing: AppSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalAppSpacing.current
}
