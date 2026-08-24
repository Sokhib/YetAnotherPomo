package com.tora.yetanotherpomo.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.tora.yetanotherpomo.ui.theme.Organic

/** The design's 50x30 pill switch (`.knob` translateX(20px) when on), matching Allowlist/Settings rows. */
@Composable
fun OrganicSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = Organic.colors
    val trackColor by animateColorAsState(
        targetValue = if (checked) colors.accent else colors.neutral.c300,
        label = "switchTrack",
    )
    val knobOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = tween(Organic.motion.standard),
        label = "switchKnob",
    )
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(width = 50.dp, height = 30.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                role = Role.Switch,
                onClick = { onCheckedChange(!checked) },
            )
            .background(trackColor, CircleShape),
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .offset(x = 3.dp + knobOffset, y = 0.dp)
                .size(24.dp)
                .shadow(2.dp, CircleShape)
                .background(colors.bg, CircleShape),
        )
    }
}
