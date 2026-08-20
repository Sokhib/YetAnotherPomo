package com.tora.organic

import com.tora.organic.tokens.ColorRampTokens
import com.tora.organic.tokens.ColorToken
import com.tora.organic.tokens.ColorTokens
import com.tora.organic.tokens.EasingToken
import com.tora.organic.tokens.ElevationTokens
import com.tora.organic.tokens.FontFamilyToken
import com.tora.organic.tokens.FontWeightToken
import com.tora.organic.tokens.MotionTokens
import com.tora.organic.tokens.OpacityToken
import com.tora.organic.tokens.OpacityTokens
import com.tora.organic.tokens.RadiusTokens
import com.tora.organic.tokens.SizeTokens
import com.tora.organic.tokens.SpacingTokens
import com.tora.organic.tokens.StrokeTokens
import com.tora.organic.tokens.TextStyleToken
import com.tora.organic.tokens.TypographyTokens
import com.tora.organic.tokens.dp
import com.tora.organic.tokens.ms
import com.tora.organic.tokens.sp

/**
 * The Organic design language: a warm paper ground, a single fixed light palette, and a baked-in
 * dark treatment reserved for the Locked/Blocked surfaces.
 *
 * Values are ported verbatim from the Organic stylesheet. This file is the one place to edit when
 * the design changes, and the one file to replace when swapping the design language wholesale.
 */

private val NeutralRamp = ColorRampTokens(
    c100 = ColorToken(0xFFF9F4ED),
    c200 = ColorToken(0xFFEEE7DB),
    c300 = ColorToken(0xFFDCD3C4),
    c400 = ColorToken(0xFFC0B6A5),
    c500 = ColorToken(0xFFA19786),
    c600 = ColorToken(0xFF82796A),
    c700 = ColorToken(0xFF645C50),
    c800 = ColorToken(0xFF474238),
    c900 = ColorToken(0xFF2E2B25),
)

private val AccentRamp = ColorRampTokens(
    c100 = ColorToken(0xFFFFF2EB),
    c200 = ColorToken(0xFFFFE1D0),
    c300 = ColorToken(0xFFFFC6A5),
    c400 = ColorToken(0xFFF6A06B),
    c500 = ColorToken(0xFFD67F48),
    c600 = ColorToken(0xFFB2622D),
    c700 = ColorToken(0xFF8C491A),
    c800 = ColorToken(0xFF643312),
    c900 = ColorToken(0xFF402310),
)

private val Accent2Ramp = ColorRampTokens(
    c100 = ColorToken(0xFFF0FAE1),
    c200 = ColorToken(0xFFE1EECC),
    c300 = ColorToken(0xFFCCDBB2),
    c400 = ColorToken(0xFFAEBF92),
    c500 = ColorToken(0xFF8FA073),
    c600 = ColorToken(0xFF728157),
    c700 = ColorToken(0xFF56633F),
    c800 = ColorToken(0xFF3D472B),
    c900 = ColorToken(0xFF272E1B),
)

private val Ink = ColorToken(0xFF201E1D)

private val OrganicOpacity = OpacityTokens(
    faint = OpacityToken(0.09f),
    fill = OpacityToken(0.12f),
    divider = OpacityToken(0.16f),
    border = OpacityToken(0.22f),
    tick = OpacityToken(0.28f),
    ghost = OpacityToken(0.35f),
    muted = OpacityToken(0.45f),
    secondary = OpacityToken(0.5f),
    scrim = OpacityToken(0.72f),
    strong = OpacityToken(0.8f),
    opaque = OpacityToken(1f),
)

private val OrganicColors = ColorTokens(
    bg = ColorToken(0xFFF5EAD8),
    surface = ColorToken(0xFFEBDDC5),
    text = Ink,
    accent = ColorToken(0xFFC67139),
    accent2 = ColorToken(0xFF7A8A5E),
    divider = Ink.withAlpha(OrganicOpacity.divider),
    neutral = NeutralRamp,
    accentRamp = AccentRamp,
    accent2Ramp = Accent2Ramp,
    lockedSurface = ColorToken(0xFF131110),
    lockedOnSurface = ColorToken(0xFFF5EAD8),
)

private val OrganicTypography = TypographyTokens(
    heading1 = TextStyleToken(FontFamilyToken.Display, fontSize = 42.sp, lineHeight = 45.sp),
    heading2 = TextStyleToken(FontFamilyToken.Display, fontSize = 32.sp, lineHeight = 34.sp),
    heading3 = TextStyleToken(FontFamilyToken.Display, fontSize = 24.sp, lineHeight = 27.sp),
    body = TextStyleToken(
        family = FontFamilyToken.Body,
        fontSize = 15.sp,
        weight = FontWeightToken.Normal,
        lineHeight = 23.sp,
    ),
    bodySmall = TextStyleToken(
        family = FontFamilyToken.Body,
        fontSize = 13.sp,
        weight = FontWeightToken.Normal,
        lineHeight = 19.sp,
    ),
    label = TextStyleToken(
        family = FontFamilyToken.Body,
        fontSize = 15.5.sp,
        weight = FontWeightToken.Medium,
    ),
    labelSmall = TextStyleToken(
        family = FontFamilyToken.Body,
        fontSize = 11.5.sp,
        weight = FontWeightToken.Normal,
        letterSpacing = 1.6.sp,
    ),
    dialDigits = TextStyleToken(FontFamilyToken.Display, fontSize = 78.sp, lineHeight = 70.sp),
    clockDigits = TextStyleToken(FontFamilyToken.Display, fontSize = 88.sp, lineHeight = 81.sp),
)

val OrganicDesignSystem = DesignSystem(
    name = "Organic",
    colors = OrganicColors,
    typography = OrganicTypography,
    spacing = SpacingTokens(
        space1 = 4.4.dp,
        space2 = 8.8.dp,
        space3 = 13.2.dp,
        space4 = 17.6.dp,
        space6 = 26.4.dp,
        space8 = 35.2.dp,
    ),
    radius = RadiusTokens(
        sm = 8.dp,
        md = 16.dp,
        lg = 28.dp,
        pill = 999.dp,
    ),
    elevation = ElevationTokens(
        sm = 2.dp,
        md = 6.dp,
        lg = 18.dp,
    ),
    stroke = StrokeTokens(
        hairline = 1.dp,
        thin = 2.dp,
        ring = 22.dp,
    ),
    size = SizeTokens(
        minTouchTarget = 44.dp,
        screenGutter = 26.dp,
        listGutter = 20.dp,
        iconSm = 18.dp,
        iconMd = 26.dp,
    ),
    opacity = OrganicOpacity,
    motion = MotionTokens(
        quick = 150.ms,
        standard = 180.ms,
        deliberate = 320.ms,
        linear = EasingToken.Linear,
        emphasized = EasingToken.Standard,
    ),
)
