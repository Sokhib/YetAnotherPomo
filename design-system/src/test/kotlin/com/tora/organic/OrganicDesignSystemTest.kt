package com.tora.organic

import com.tora.organic.tokens.ColorToken
import com.tora.organic.tokens.FontFamilyToken
import com.tora.organic.tokens.FontWeightToken
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ColorTokenTest {

    @Test
    fun `channels unpack from packed argb`() {
        val color = ColorToken(0xFFC67139)
        assertEquals(0xFF, color.alpha)
        assertEquals(0xC6, color.red)
        assertEquals(0x71, color.green)
        assertEquals(0x39, color.blue)
    }

    @Test
    fun `withAlpha keeps the hue and rounds the way Compose does`() {
        // Compose's Color.copy(alpha = 0.16f) lands on 41/255 once packed to 8 bits.
        assertEquals(ColorToken(0x29201E1D), ColorToken(0xFF201E1D).withAlpha(0.16f))
    }

    @Test
    fun `withAlpha clamps out-of-range input`() {
        assertEquals(0xFF, ColorToken(0x00201E1D).withAlpha(2f).alpha)
        assertEquals(0x00, ColorToken(0xFF201E1D).withAlpha(-1f).alpha)
    }
}

class OrganicDesignSystemTest {

    @Test
    fun `divider is the ink colour at the divider opacity`() {
        val colors = OrganicDesignSystem.colors
        assertEquals(colors.text.red, colors.divider.red)
        assertEquals(colors.text.green, colors.divider.green)
        assertEquals(colors.text.blue, colors.divider.blue)
        assertEquals(41, colors.divider.alpha)
    }

    @Test
    fun `type scale only needs the font weights the app actually bundles`() {
        val required = OrganicDesignSystem.typography.requirements.associate { it.family to it.weights }

        assertEquals(setOf(FontWeightToken.Normal), required[FontFamilyToken.Display])
        assertEquals(
            setOf(FontWeightToken.Normal, FontWeightToken.Medium),
            required[FontFamilyToken.Body],
        )
    }

    @Test
    fun `opacity scale is monotonically increasing`() {
        val scale = OrganicDesignSystem.opacity.let {
            listOf(it.faint, it.fill, it.divider, it.border, it.tick, it.ghost, it.muted, it.secondary, it.scrim, it.strong, it.opaque)
        }
        assertTrue(scale.zipWithNext().all { (lower, higher) -> lower.fraction < higher.fraction })
    }

    @Test
    fun `spacing scale is monotonically increasing`() {
        val scale = OrganicDesignSystem.spacing.let {
            listOf(it.space1, it.space2, it.space3, it.space4, it.space6, it.space8)
        }
        assertTrue(scale.zipWithNext().all { (lower, higher) -> lower < higher })
    }
}
