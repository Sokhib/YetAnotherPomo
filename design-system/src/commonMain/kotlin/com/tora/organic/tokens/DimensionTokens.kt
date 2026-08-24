package com.tora.organic.tokens

/** The spacing scale. Step numbers are multiples of the 4.4dp base unit, not of each other. */
data class SpacingTokens(
    val space1: DpToken,
    val space2: DpToken,
    val space3: DpToken,
    val space4: DpToken,
    val space6: DpToken,
    val space8: DpToken,
)

data class RadiusTokens(
    val sm: DpToken,
    val md: DpToken,
    val lg: DpToken,
    /** Large enough that any height resolves to a fully rounded capsule. */
    val pill: DpToken,
)

/** Shadow depth tuned to the ground, per the stylesheet's `--shadow-*`. */
data class ElevationTokens(
    val sm: DpToken,
    val md: DpToken,
    val lg: DpToken,
)

/** Line weights for borders, dividers and drawn strokes. */
data class StrokeTokens(
    val hairline: DpToken,
    val thin: DpToken,
    val ring: DpToken,
)

/** Layout constants that are design decisions rather than per-component measurements. */
data class SizeTokens(
    val minTouchTarget: DpToken,
    val screenGutter: DpToken,
    val listGutter: DpToken,
    val iconSm: DpToken,
    val iconMd: DpToken,
)
