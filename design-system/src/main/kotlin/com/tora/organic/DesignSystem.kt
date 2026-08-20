package com.tora.organic

import com.tora.organic.tokens.ColorTokens
import com.tora.organic.tokens.ElevationTokens
import com.tora.organic.tokens.MotionTokens
import com.tora.organic.tokens.OpacityTokens
import com.tora.organic.tokens.RadiusTokens
import com.tora.organic.tokens.SizeTokens
import com.tora.organic.tokens.SpacingTokens
import com.tora.organic.tokens.StrokeTokens
import com.tora.organic.tokens.TypographyTokens

/**
 * One complete design language, as data.
 *
 * This is the module's whole public contract: a consumer binds [DesignSystem] once — mapping each
 * token to its platform type — and every screen then reads through that binding. Swapping the
 * design language means handing the binding a different [DesignSystem] instance; nothing
 * downstream changes.
 *
 * @see OrganicDesignSystem for the instance this app ships.
 */
data class DesignSystem(
    val name: String,
    val colors: ColorTokens,
    val typography: TypographyTokens,
    val spacing: SpacingTokens,
    val radius: RadiusTokens,
    val elevation: ElevationTokens,
    val stroke: StrokeTokens,
    val size: SizeTokens,
    val opacity: OpacityTokens,
    val motion: MotionTokens,
)
