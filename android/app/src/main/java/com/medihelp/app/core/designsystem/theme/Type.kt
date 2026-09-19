package com.medihelp.app.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.medihelp.app.R

// Manrope (display) and Public Sans (body) are the brand's substitute pairing
// (Resources/_ds readme.md, "Font substitution notice"). Both ship as variable
// fonts, so each weight below is one instance of the same file with the wght
// axis pinned — supported from API 26, which matches minSdk. Licenses are kept
// in app/licenses/.
@OptIn(ExperimentalTextApi::class)
private fun brandFont(resourceId: Int, weight: Int) = Font(
    resId = resourceId,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight)),
)

private val DisplayFontFamily = FontFamily(
    brandFont(R.font.manrope_variable, 500),
    brandFont(R.font.manrope_variable, 600),
    brandFont(R.font.manrope_variable, 700),
    brandFont(R.font.manrope_variable, 800),
)

private val BodyFontFamily = FontFamily(
    brandFont(R.font.public_sans_variable, 400),
    brandFont(R.font.public_sans_variable, 500),
    brandFont(R.font.public_sans_variable, 600),
    brandFont(R.font.public_sans_variable, 700),
)

// Type scale mirrors tokens/typography.css. Never go below 16sp anywhere.
val MediHelpTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp,
        lineHeight = 29.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 18.sp,
        lineHeight = 26.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp,
    ),
    // Buttons and bottom-navigation labels read as display type in the mockups.
    labelLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 22.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.9.sp,
    ),
    // Bottom-navigation labels only. The 16sp floor in tokens/typography.css
    // governs readable content; a nav label is a short word paired with an
    // icon, and at 15sp "Medicines" no longer fits a quarter of a 1080px
    // screen and truncates to "Medicin…", which is worse for legibility than
    // one step down.
    labelSmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
    ),
)
