package com.tora.organic.tokens

/**
 * The primitive value types every other token is built from.
 *
 * All of them are `value class` wrappers over a JVM primitive, so a token set costs nothing at
 * runtime and carries no dependency on Compose, Android, or any other UI toolkit. Turning a token
 * into a renderable value (`Color`, `Dp`, `TextStyle`, ...) is the consumer's job.
 */

/**
 * A colour packed as 32-bit sRGB `AARRGGBB`, written the way it reads in a stylesheet:
 * `ColorToken(0xFFF5EAD8)`.
 */
@JvmInline
value class ColorToken(val argb: Long) {
    val alpha: Int get() = ((argb shr 24) and 0xFF).toInt()
    val red: Int get() = ((argb shr 16) and 0xFF).toInt()
    val green: Int get() = ((argb shr 8) and 0xFF).toInt()
    val blue: Int get() = (argb and 0xFF).toInt()

    /**
     * The same hue at [fraction] opacity. Rounds to 8 bits the same way Compose's
     * `Color.copy(alpha = ...)` does, so bindings stay pixel-identical.
     */
    fun withAlpha(fraction: Float): ColorToken {
        val a = (fraction.coerceIn(0f, 1f) * 255f + 0.5f).toInt().toLong()
        return ColorToken((a shl 24) or (argb and 0x00FFFFFF))
    }

    fun withAlpha(opacity: OpacityToken): ColorToken = withAlpha(opacity.fraction)
}

/**
 * A density-independent length. Carries the number only — resolving it against a screen density
 * is a platform concern and deliberately lives outside this module.
 */
@JvmInline
value class DpToken(val value: Float) : Comparable<DpToken> {
    override fun compareTo(other: DpToken): Int = value.compareTo(other.value)

    operator fun plus(other: DpToken): DpToken = DpToken(value + other.value)
    operator fun minus(other: DpToken): DpToken = DpToken(value - other.value)
    operator fun times(factor: Float): DpToken = DpToken(value * factor)
    operator fun div(divisor: Float): DpToken = DpToken(value / divisor)
}

/** A scale-independent text length: font size, line height or letter spacing. */
@JvmInline
value class SpToken(val value: Float) : Comparable<SpToken> {
    override fun compareTo(other: SpToken): Int = value.compareTo(other.value)

    operator fun times(factor: Float): SpToken = SpToken(value * factor)
}

/** An opacity in the 0f..1f range. */
@JvmInline
value class OpacityToken(val fraction: Float)

/** An animation duration in milliseconds. */
@JvmInline
value class DurationToken(val millis: Int)

/**
 * A cubic-bezier easing curve given by its two control points, matching the CSS
 * `cubic-bezier(a, b, c, d)` the Organic stylesheet is authored in.
 */
data class EasingToken(
    val a: Float,
    val b: Float,
    val c: Float,
    val d: Float,
) {
    companion object {
        val Linear = EasingToken(0f, 0f, 1f, 1f)
        val Standard = EasingToken(0.4f, 0f, 0.2f, 1f)
        val Decelerate = EasingToken(0f, 0f, 0.2f, 1f)
    }
}

val Number.dp: DpToken get() = DpToken(toFloat())
val Number.sp: SpToken get() = SpToken(toFloat())
val Number.ms: DurationToken get() = DurationToken(toInt())
