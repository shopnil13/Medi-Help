package com.medihelp.app.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import com.medihelp.app.core.designsystem.theme.Red200
import com.medihelp.app.core.designsystem.theme.Red300
import com.medihelp.app.core.designsystem.theme.Red500
import com.medihelp.app.core.designsystem.theme.Red600
import com.medihelp.app.core.designsystem.theme.Red700
import kotlin.math.PI
import kotlin.math.sin

/**
 * One band of the brand wave motif.
 *
 * @param crest vertical start of the band's crest, as a fraction of height.
 * @param amplitude wave height, as a fraction of the band's height.
 * @param tilt how far the band rises from left to right, as a fraction of height.
 * @param phase horizontal offset in radians, so stacked bands do not align.
 */
private data class WaveBand(
    val color: Color,
    val crest: Float,
    val amplitude: Float,
    val tilt: Float,
    val phase: Float,
)

// Ordered back (palest, highest) to front (deepest, lowest), matching the
// onboarding mockup in Resources/App_opening_screen.png. Each band is a flat
// fill rather than a gradient — the design system calls for flat color, and the
// depth reads from the overlap instead.
private val OnboardingWaveBands = listOf(
    WaveBand(color = Red200, crest = 0.34f, amplitude = 0.10f, tilt = 0.26f, phase = 0.0f),
    WaveBand(color = Red300, crest = 0.48f, amplitude = 0.09f, tilt = 0.22f, phase = 0.7f),
    WaveBand(color = Red500, crest = 0.62f, amplitude = 0.08f, tilt = 0.18f, phase = 1.5f),
    WaveBand(color = Red600, crest = 0.76f, amplitude = 0.07f, tilt = 0.14f, phase = 2.3f),
    WaveBand(color = Red700, crest = 0.90f, amplitude = 0.06f, tilt = 0.10f, phase = 3.1f),
)

/**
 * The layered wave motif anchoring the bottom of the onboarding screen.
 *
 * Drawn rather than shipped as a bitmap so it scales to any screen size and
 * recolors with the brand ramp.
 */
@Composable
fun BrandWaveFooter(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        // Enough samples that the curve reads as smooth at any display density.
        val steps = 72

        OnboardingWaveBands.forEach { band ->
            val path = Path()
            val baseline = height * band.crest
            val amplitude = height * band.amplitude
            val rise = height * band.tilt

            path.moveTo(0f, baseline)
            for (step in 0..steps) {
                val progress = step.toFloat() / steps
                val y = baseline -
                    rise * progress +
                    amplitude * sin(progress * 2f * PI.toFloat() + band.phase)
                path.lineTo(width * progress, y)
            }
            path.lineTo(width, height)
            path.lineTo(0f, height)
            path.close()

            drawPath(path = path, color = band.color)
        }
    }
}
