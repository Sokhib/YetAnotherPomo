package com.tora.yetanotherpomo.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.tora.organic.OrganicDesignSystem
import com.tora.organic.tokens.ColorRampTokens
import com.tora.organic.tokens.ColorTokens
import com.tora.organic.tokens.OpacityTokens

/**
 * The Compose-side mirror of the design system's colour tokens. Values come from
 * `:design-system`; this file only converts them once so screens can read plain [Color]s.
 */

/** A 100-900 tonal ramp on one shared perceptual lightness scale. */
@Immutable
data class ColorRamp(
    val c100: Color,
    val c200: Color,
    val c300: Color,
    val c400: Color,
    val c500: Color,
    val c600: Color,
    val c700: Color,
    val c800: Color,
    val c900: Color,
)

@Immutable
data class OrganicColors(
    val bg: Color,
    val surface: Color,
    val text: Color,
    val accent: Color,
    val accent2: Color,
    val divider: Color,
    val neutral: ColorRamp,
    val accentRamp: ColorRamp,
    val accent2Ramp: ColorRamp,
    // Fixed dark treatment used only by the Locked/Blocked screens - not a system dark theme,
    // the design bakes this in regardless of the device's theme setting.
    val lockedSurface: Color,
    val lockedOnSurface: Color,
)

/** Opacity steps as raw alpha fractions, ready for `Color.copy(alpha = ...)`. */
@Immutable
data class OrganicOpacity(
    val faint: Float,
    val fill: Float,
    val divider: Float,
    val border: Float,
    val tick: Float,
    val ghost: Float,
    val muted: Float,
    val secondary: Float,
    val scrim: Float,
    val strong: Float,
    val opaque: Float,
)

internal fun ColorRampTokens.toColorRamp() = ColorRamp(
    c100 = c100.toColor(),
    c200 = c200.toColor(),
    c300 = c300.toColor(),
    c400 = c400.toColor(),
    c500 = c500.toColor(),
    c600 = c600.toColor(),
    c700 = c700.toColor(),
    c800 = c800.toColor(),
    c900 = c900.toColor(),
)

internal fun ColorTokens.toOrganicColors() = OrganicColors(
    bg = bg.toColor(),
    surface = surface.toColor(),
    text = text.toColor(),
    accent = accent.toColor(),
    accent2 = accent2.toColor(),
    divider = divider.toColor(),
    neutral = neutral.toColorRamp(),
    accentRamp = accentRamp.toColorRamp(),
    accent2Ramp = accent2Ramp.toColorRamp(),
    lockedSurface = lockedSurface.toColor(),
    lockedOnSurface = lockedOnSurface.toColor(),
)

internal fun OpacityTokens.toOrganicOpacity() = OrganicOpacity(
    faint = faint.toAlpha(),
    fill = fill.toAlpha(),
    divider = divider.toAlpha(),
    border = border.toAlpha(),
    tick = tick.toAlpha(),
    ghost = ghost.toAlpha(),
    muted = muted.toAlpha(),
    secondary = secondary.toAlpha(),
    scrim = scrim.toAlpha(),
    strong = strong.toAlpha(),
    opaque = opaque.toAlpha(),
)

val DefaultOrganicColors = OrganicDesignSystem.colors.toOrganicColors()
val DefaultOrganicOpacity = OrganicDesignSystem.opacity.toOrganicOpacity()

val LocalOrganicColors = staticCompositionLocalOf { DefaultOrganicColors }
val LocalOrganicOpacity = staticCompositionLocalOf { DefaultOrganicOpacity }
