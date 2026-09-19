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

// tokens/typography.css specifies a deliberately oversized scale (40sp display,
// 18-20sp body) for elderly and low-vision readers. On a real handset that
// scale overflowed cards and wrapped headings, so the whole ramp is stepped
// down roughly one size here. Reading text still sits at 16-17sp — above
// Material's 14sp default — so the accessibility intent survives at a size
// that fits the screen.
val MediHelpTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 38.sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 21.sp,
        lineHeight = 26.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 17.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 23.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = BodyFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    // Buttons read as display type in the mockups.
    labelLarge = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        lineHeight = 20.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.8.sp,
    ),
    // Bottom-navigation labels only, where each label shares a quarter of the
    // screen width with its icon.
    labelSmall = TextStyle(
        fontFamily = DisplayFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
)
