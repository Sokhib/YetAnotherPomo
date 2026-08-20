package com.tora.yetanotherpomo.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.tora.organic.DesignSystem
import com.tora.organic.OrganicDesignSystem

/**
 * Materialises a [DesignSystem] into Compose types and publishes it to the tree.
 *
 * The design language itself lives in the platform-free `:design-system` module, so theming this
 * app differently is a matter of passing a different [designSystem] - no call site changes.
 *
 * Deliberately does not branch on [isSystemInDarkTheme] or dynamic color: the Locked/Blocked
 * screens' dark treatment is a baked-in design choice (see [OrganicColors.lockedSurface]), not a
 * system dark mode.
 */
@Composable
fun OrganicTheme(
    designSystem: DesignSystem = OrganicDesignSystem,
    fonts: OrganicFonts = DefaultOrganicFonts,
    content: @Composable () -> Unit,
) {
    val colors = remember(designSystem) { designSystem.colors.toOrganicColors() }
    val type = remember(designSystem, fonts) { designSystem.typography.toOrganicType(fonts) }
    val spacing = remember(designSystem) { designSystem.spacing.toOrganicSpacing() }
    val radius = remember(designSystem) { designSystem.radius.toOrganicRadius() }
    val elevation = remember(designSystem) { designSystem.elevation.toOrganicElevation() }
    val stroke = remember(designSystem) { designSystem.stroke.toOrganicStroke() }
    val size = remember(designSystem) { designSystem.size.toOrganicSize() }
    val opacity = remember(designSystem) { designSystem.opacity.toOrganicOpacity() }
    val motion = remember(designSystem) { designSystem.motion.toOrganicMotion() }

    CompositionLocalProvider(
        LocalOrganicColors provides colors,
        LocalOrganicType provides type,
        LocalOrganicFonts provides fonts,
        LocalOrganicSpacing provides spacing,
        LocalOrganicRadius provides radius,
        LocalOrganicElevation provides elevation,
        LocalOrganicStroke provides stroke,
        LocalOrganicSize provides size,
        LocalOrganicOpacity provides opacity,
        LocalOrganicMotion provides motion,
    ) {
        // MaterialTheme kept only for ripple/selection-handle defaults consumed by underlying
        // Compose components; no M3 color role is read directly by Organic components.
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = colors.accent,
                background = colors.bg,
                surface = colors.surface,
                onBackground = colors.text,
                onSurface = colors.text,
            ),
            content = content,
        )
    }
}

/** Convenience accessors so screens/components read `Organic.colors` etc. */
object Organic {
    val colors: OrganicColors
        @Composable get() = LocalOrganicColors.current
    val type: OrganicType
        @Composable get() = LocalOrganicType.current
    val fonts: OrganicFonts
        @Composable get() = LocalOrganicFonts.current
    val spacing: OrganicSpacing
        @Composable get() = LocalOrganicSpacing.current
    val radius: OrganicRadius
        @Composable get() = LocalOrganicRadius.current
    val elevation: OrganicElevation
        @Composable get() = LocalOrganicElevation.current
    val stroke: OrganicStroke
        @Composable get() = LocalOrganicStroke.current
    val size: OrganicSize
        @Composable get() = LocalOrganicSize.current
    val opacity: OrganicOpacity
        @Composable get() = LocalOrganicOpacity.current
    val motion: OrganicMotion
        @Composable get() = LocalOrganicMotion.current
}
