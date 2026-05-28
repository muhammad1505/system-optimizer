package com.system.optimizer.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 shape tokens. Compose's [Shapes] class only exposes five named slots
 * (extra-small → extra-large); we use the spec's recommended radii so M3 components
 * automatically pick the right corner per role:
 *
 * - extraSmall (4dp) — text fields, chips
 * - small (8dp) — small buttons, snackbars
 * - medium (12dp) — cards, dialogs (small)
 * - large (16dp) — cards, sheets, navigation drawers
 * - extraLarge (28dp) — modal dialogs, large surfaces
 */
val AppShapes: Shapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(28.dp)
)
