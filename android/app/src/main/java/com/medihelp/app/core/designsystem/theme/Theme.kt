package com.medihelp.app.core.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// The brand design system defines a single warm, "calm not clinical" light
// surface — no dark theme was specified (see Resources/_ds readme.md, Visual
// Foundations). We use the same light scheme regardless of system setting
// rather than inventing an unreviewed dark palette.
private val MediHelpColorScheme = lightColorScheme(
    primary = Red600,
    onPrimary = Warm0,
    primaryContainer = Red200,
    onPrimaryContainer = Red900,
    secondary = Warm600,
    onSecondary = Warm0,
    background = Warm50,
    onBackground = Warm900,
    surface = Warm0,
    onSurface = Warm900,
    surfaceVariant = Warm100,
    onSurfaceVariant = Warm600,
    outline = Warm200,
    outlineVariant = Warm200,
    error = StatusCritical,
    onError = Warm0,
    errorContainer = StatusCriticalBg,
    onErrorContainer = Red900,
)

@Composable
fun MediHelpTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = MediHelpColorScheme,
        typography = MediHelpTypography,
        shapes = MediHelpShapes,
        content = content,
    )
}
