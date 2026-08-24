package com.tora.yetanotherpomo.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tora.organic.tokens.ColorToken
import com.tora.organic.tokens.DpToken
import com.tora.organic.tokens.DurationToken
import com.tora.organic.tokens.EasingToken
import com.tora.organic.tokens.FontWeightToken
import com.tora.organic.tokens.OpacityToken
import com.tora.organic.tokens.SpToken
import com.tora.organic.tokens.TextStyleToken

/**
 * The bridge between the toolkit-free `:design-system` module and Compose.
 *
 * Everything Android-specific about the Organic design system lives on this side of the boundary:
 * these conversions, plus the font binding in [OrganicFonts]. The token module itself knows
 * nothing about Compose, so porting the design language to another renderer means writing another
 * file like this one and nothing else.
 *
 * Conversions run once, when [OrganicTheme] materialises the token set — call sites read the
 * already-converted Compose types through [Organic].
 */

internal fun ColorToken.toColor(): Color = Color(argb)

internal fun DpToken.toDp(): Dp = value.dp

internal fun SpToken.toSp(): TextUnit = value.sp

internal fun OpacityToken.toAlpha(): Float = fraction

internal fun DurationToken.toMillis(): Int = millis

internal fun EasingToken.toEasing(): Easing = CubicBezierEasing(a, b, c, d)

internal fun FontWeightToken.toFontWeight(): FontWeight = FontWeight(weight)

internal fun TextStyleToken.toTextStyle(fonts: OrganicFonts): TextStyle = TextStyle(
    fontFamily = fonts.familyFor(family),
    fontWeight = weight.toFontWeight(),
    fontSize = fontSize.toSp(),
    lineHeight = lineHeight?.toSp() ?: TextUnit.Unspecified,
    letterSpacing = letterSpacing?.toSp() ?: TextUnit.Unspecified,
)
