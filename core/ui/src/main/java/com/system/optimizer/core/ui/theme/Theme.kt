package com.system.optimizer.core.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary40,
    onPrimary = OnPrimary100,
    primaryContainer = PrimaryContainer90,
    onPrimaryContainer = OnPrimaryContainer10,
    secondary = Secondary40,
    onSecondary = OnSecondary100,
    secondaryContainer = SecondaryContainer90,
    onSecondaryContainer = OnSecondaryContainer10,
    tertiary = Tertiary40,
    onTertiary = OnTertiary100,
    tertiaryContainer = TertiaryContainer90,
    onTertiaryContainer = OnTertiaryContainer10,
    error = Error40,
    onError = OnError100,
    errorContainer = ErrorContainer90,
    onErrorContainer = OnErrorContainer10,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceTint = LightSurfaceTint,
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    inversePrimary = LightInversePrimary,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    scrim = LightScrim,
    surfaceBright = LightSurfaceBright,
    surfaceDim = LightSurfaceDim,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary80,
    onPrimary = OnPrimary20,
    primaryContainer = PrimaryContainer30,
    onPrimaryContainer = OnPrimaryContainer90,
    secondary = Secondary80,
    onSecondary = OnSecondary20,
    secondaryContainer = SecondaryContainer30,
    onSecondaryContainer = OnSecondaryContainer90,
    tertiary = Tertiary80,
    onTertiary = OnTertiary20,
    tertiaryContainer = TertiaryContainer30,
    onTertiaryContainer = OnTertiaryContainer90,
    error = Error80,
    onError = OnError20,
    errorContainer = ErrorContainer30,
    onErrorContainer = OnErrorContainer90,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = DarkSurfaceTint,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    inversePrimary = DarkInversePrimary,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = DarkScrim,
    surfaceBright = DarkSurfaceBright,
    surfaceDim = DarkSurfaceDim,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest
)

/**
 * Material3 theme entry-point. Honours an explicit [darkTheme] override (sourced from a
 * persisted preference) but falls back to system setting. Provides shapes, typography
 * and a [LocalAppSpacing] custom token alongside the colour scheme.
 *
 * Edge-to-edge: status / navigation bars stay transparent; light/dark icon appearance
 * follows the active theme via [WindowCompat].
 */
@Composable
fun SystemOptimizerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAppSpacing provides AppSpacing()) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = AppShapes,
            content = content
        )
    }
}
