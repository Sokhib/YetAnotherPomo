package com.tora.yetanotherpomo.ui.theme

import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.tora.organic.OrganicDesignSystem
import com.tora.organic.tokens.MotionTokens

/**
 * The Compose-side mirror of the design system's motion tokens: durations in milliseconds ready
 * for `tween(...)`, and curves already built as [Easing].
 */
@Immutable
data class OrganicMotion(
    val quick: Int,
    val standard: Int,
    val deliberate: Int,
    val linear: Easing,
    val emphasized: Easing,
)

internal fun MotionTokens.toOrganicMotion() = OrganicMotion(
    quick = quick.toMillis(),
    standard = standard.toMillis(),
    deliberate = deliberate.toMillis(),
    linear = linear.toEasing(),
    emphasized = emphasized.toEasing(),
)

val DefaultOrganicMotion = OrganicDesignSystem.motion.toOrganicMotion()

val LocalOrganicMotion = staticCompositionLocalOf { DefaultOrganicMotion }
