package com.tora.organic.tokens

/**
 * The opacity scale. Named by the job each step does, because "45% of the on-surface colour" is a
 * design decision that recurs across screens and should not be retyped as a literal.
 */
data class OpacityTokens(
    /** Barely-there wash used behind chips on the dark locked surfaces. */
    val faint: OpacityToken,
    /** Filled but unobtrusive backgrounds. */
    val fill: OpacityToken,
    /** Hairline dividers. */
    val divider: OpacityToken,
    /** Outlines on dark surfaces. */
    val border: OpacityToken,
    /** Dial ticks and other fine drawn detail. */
    val tick: OpacityToken,
    /** Text pushed as far back as it can go while staying readable. */
    val ghost: OpacityToken,
    /** Secondary/disabled text and icons. */
    val muted: OpacityToken,
    /** Supporting text that still needs to be read comfortably. */
    val secondary: OpacityToken,
    /** Scrims over content. */
    val scrim: OpacityToken,
    /** Near-full-strength content on a tinted background. */
    val strong: OpacityToken,
    /** Fully opaque — the identity step, so call sites can branch without a literal. */
    val opaque: OpacityToken,
)
