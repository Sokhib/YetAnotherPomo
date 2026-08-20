package com.tora.yetanotherpomo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tora.yetanotherpomo.ui.theme.CaprasimoFamily
import com.tora.yetanotherpomo.ui.theme.Organic

enum class PillButtonVariant { Primary, Secondary, Ghost }

/**
 * The design's `.btn` pill (border-radius: 999px), matching Home's "Begin" button and the
 * confirm-sheet actions. [containerColor]/[contentColor]/[borderColor] let callers reproduce
 * the doc's per-state combinations (e.g. selected vs unselected duration presets) without a
 * combinatorial explosion of variant enums.
 */
@Composable
fun PillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: PillButtonVariant = PillButtonVariant.Primary,
    fullWidth: Boolean = false,
    containerColor: Color? = null,
    contentColor: Color? = null,
    borderColor: Color? = null,
    enabled: Boolean = true,
) {
    val colors = Organic.colors
    val type = Organic.type
    val radius = Organic.radius
    val opacity = Organic.opacity
    val borderWidth = Organic.stroke.hairline

    val resolvedContainer = containerColor ?: when (variant) {
        PillButtonVariant.Primary -> colors.accent
        PillButtonVariant.Secondary -> Color.Transparent
        PillButtonVariant.Ghost -> Color.Transparent
    }
    val resolvedContent = contentColor ?: when (variant) {
        PillButtonVariant.Primary -> colors.bg
        PillButtonVariant.Secondary -> colors.neutral.c700
        PillButtonVariant.Ghost -> colors.neutral.c700
    }
    val resolvedBorder = borderColor ?: when (variant) {
        PillButtonVariant.Secondary -> colors.divider
        else -> null
    }

    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val pressedContainer = when {
        !pressed -> resolvedContainer
        variant == PillButtonVariant.Primary -> colors.accentRamp.c700
        else -> colors.neutral.c200
    }
    val shape = RoundedCornerShape(radius.pill)

    Box(
        modifier = (if (fullWidth) modifier.fillMaxWidth() else modifier)
            .clip(shape)
            .background(pressedContainer, shape)
            .then(
                if (resolvedBorder != null) {
                    Modifier.border(BorderStroke(borderWidth, resolvedBorder), shape)
                } else {
                    Modifier
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(vertical = 19.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = type.label.copy(fontFamily = CaprasimoFamily, fontSize = 17.sp),
            color = resolvedContent.copy(alpha = if (enabled) opacity.opaque else opacity.muted),
        )
    }
}
