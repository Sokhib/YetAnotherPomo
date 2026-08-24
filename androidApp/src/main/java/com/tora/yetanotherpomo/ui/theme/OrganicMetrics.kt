package com.tora.yetanotherpomo.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import com.tora.organic.OrganicDesignSystem
import com.tora.organic.tokens.ElevationTokens
import com.tora.organic.tokens.RadiusTokens
import com.tora.organic.tokens.SizeTokens
import com.tora.organic.tokens.SpacingTokens
import com.tora.organic.tokens.StrokeTokens

/**
 * The Compose-side mirror of the design system's dimension tokens. Values come from
 * `:design-system`; this file only converts them once so screens can read plain [Dp]s.
 */

@Immutable
data class OrganicSpacing(
    val space1: Dp,
    val space2: Dp,
    val space3: Dp,
    val space4: Dp,
    val space6: Dp,
    val space8: Dp,
)

@Immutable
data class OrganicRadius(
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
    val pill: Dp,
)

/** Elevation depth (used with Modifier.shadow) tuned to the ground, per the stylesheet's --shadow-*. */
@Immutable
data class OrganicElevation(
    val sm: Dp,
    val md: Dp,
    val lg: Dp,
)

/** Line weights for borders, dividers and drawn strokes. */
@Immutable
data class OrganicStroke(
    val hairline: Dp,
    val thin: Dp,
    val ring: Dp,
)

/** Layout constants that are design decisions rather than per-component measurements. */
@Immutable
data class OrganicSize(
    val minTouchTarget: Dp,
    val screenGutter: Dp,
    val listGutter: Dp,
    val iconSm: Dp,
    val iconMd: Dp,
)

internal fun SpacingTokens.toOrganicSpacing() = OrganicSpacing(
    space1 = space1.toDp(),
    space2 = space2.toDp(),
    space3 = space3.toDp(),
    space4 = space4.toDp(),
    space6 = space6.toDp(),
    space8 = space8.toDp(),
)

internal fun RadiusTokens.toOrganicRadius() = OrganicRadius(
    sm = sm.toDp(),
    md = md.toDp(),
    lg = lg.toDp(),
    pill = pill.toDp(),
)

internal fun ElevationTokens.toOrganicElevation() = OrganicElevation(
    sm = sm.toDp(),
    md = md.toDp(),
    lg = lg.toDp(),
)

internal fun StrokeTokens.toOrganicStroke() = OrganicStroke(
    hairline = hairline.toDp(),
    thin = thin.toDp(),
    ring = ring.toDp(),
)

internal fun SizeTokens.toOrganicSize() = OrganicSize(
    minTouchTarget = minTouchTarget.toDp(),
    screenGutter = screenGutter.toDp(),
    listGutter = listGutter.toDp(),
    iconSm = iconSm.toDp(),
    iconMd = iconMd.toDp(),
)

val DefaultOrganicSpacing = OrganicDesignSystem.spacing.toOrganicSpacing()
val DefaultOrganicRadius = OrganicDesignSystem.radius.toOrganicRadius()
val DefaultOrganicElevation = OrganicDesignSystem.elevation.toOrganicElevation()
val DefaultOrganicStroke = OrganicDesignSystem.stroke.toOrganicStroke()
val DefaultOrganicSize = OrganicDesignSystem.size.toOrganicSize()

val LocalOrganicSpacing = staticCompositionLocalOf { DefaultOrganicSpacing }
val LocalOrganicRadius = staticCompositionLocalOf { DefaultOrganicRadius }
val LocalOrganicElevation = staticCompositionLocalOf { DefaultOrganicElevation }
val LocalOrganicStroke = staticCompositionLocalOf { DefaultOrganicStroke }
val LocalOrganicSize = staticCompositionLocalOf { DefaultOrganicSize }
