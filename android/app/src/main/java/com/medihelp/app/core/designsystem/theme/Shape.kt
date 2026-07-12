package com.medihelp.app.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Corner radii mirror tokens/spacing.css — Material 3 "expressive" rounding.
object MediHelpRadius {
    val sm = 12.dp
    val md = 20.dp
    val lg = 28.dp
    val xl = 36.dp
    val full = 999.dp
}

val MediHelpShapes = Shapes(
    extraSmall = RoundedCornerShape(MediHelpRadius.sm),
    small = RoundedCornerShape(MediHelpRadius.sm),
    medium = RoundedCornerShape(MediHelpRadius.md),
    large = RoundedCornerShape(MediHelpRadius.lg),
    extraLarge = RoundedCornerShape(MediHelpRadius.xl),
)

object MediHelpSpacing {
    val space1 = 4.dp
    val space2 = 8.dp
    val space3 = 12.dp
    val space4 = 16.dp
    val space5 = 20.dp
    val space6 = 24.dp
    val space8 = 32.dp
    val space10 = 40.dp
    val space12 = 48.dp
    val space16 = 64.dp

    // Non-negotiable minimum tap target for elderly users.
    val tapTargetMin = 56.dp
}
