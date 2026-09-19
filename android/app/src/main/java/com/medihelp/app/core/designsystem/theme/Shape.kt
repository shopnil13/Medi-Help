package com.medihelp.app.core.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// tokens/spacing.css calls for Material 3 "expressive" rounding (12-36dp).
// Stepped down alongside the type scale: at the original radii the cards read
// as oversized pills once their contents shrank.
object MediHelpRadius {
    val sm = 10.dp
    val md = 14.dp
    val lg = 20.dp
    val xl = 28.dp
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

    // Minimum tap target. tokens/spacing.css asks for 56dp; 48dp is Material's
    // accessible minimum and keeps buttons from looking oversized next to the
    // reduced type scale.
    val tapTargetMin = 48.dp
}
