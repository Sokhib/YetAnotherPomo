package com.tora.organic.tokens

/**
 * A 100-900 tonal ramp on one shared perceptual lightness scale, mirroring the Organic
 * stylesheet's `--color-*-100 ... --color-*-900` custom properties.
 */
data class ColorRampTokens(
    val c100: ColorToken,
    val c200: ColorToken,
    val c300: ColorToken,
    val c400: ColorToken,
    val c500: ColorToken,
    val c600: ColorToken,
    val c700: ColorToken,
    val c800: ColorToken,
    val c900: ColorToken,
)

/** The semantic colour roles a screen actually reads, plus the ramps they are drawn from. */
data class ColorTokens(
    val bg: ColorToken,
    val surface: ColorToken,
    val text: ColorToken,
    val accent: ColorToken,
    val accent2: ColorToken,
    val divider: ColorToken,
    val neutral: ColorRampTokens,
    val accentRamp: ColorRampTokens,
    val accent2Ramp: ColorRampTokens,
    /**
     * Fixed dark treatment used only by the Locked/Blocked surfaces. Not a system dark theme —
     * the design bakes this in regardless of the device's theme setting.
     */
    val lockedSurface: ColorToken,
    val lockedOnSurface: ColorToken,
)
