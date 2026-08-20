package com.tora.yetanotherpomo.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import com.tora.organic.OrganicDesignSystem
import com.tora.organic.tokens.TypographyTokens

/**
 * The Compose-side mirror of the design system's type scale. Values come from `:design-system`;
 * this file pairs each style with the font files resolved by [OrganicFonts].
 */
@Immutable
data class OrganicType(
    val heading1: TextStyle,
    val heading2: TextStyle,
    val heading3: TextStyle,
    val body: TextStyle,
    val bodySmall: TextStyle,
    val label: TextStyle,
    val labelSmall: TextStyle,
    val dialDigits: TextStyle,
    val clockDigits: TextStyle,
)

internal fun TypographyTokens.toOrganicType(fonts: OrganicFonts) = OrganicType(
    heading1 = heading1.toTextStyle(fonts),
    heading2 = heading2.toTextStyle(fonts),
    heading3 = heading3.toTextStyle(fonts),
    body = body.toTextStyle(fonts),
    bodySmall = bodySmall.toTextStyle(fonts),
    label = label.toTextStyle(fonts),
    labelSmall = labelSmall.toTextStyle(fonts),
    dialDigits = dialDigits.toTextStyle(fonts),
    clockDigits = clockDigits.toTextStyle(fonts),
)

val DefaultOrganicType = OrganicDesignSystem.typography.toOrganicType(DefaultOrganicFonts)

val LocalOrganicType = staticCompositionLocalOf { DefaultOrganicType }
