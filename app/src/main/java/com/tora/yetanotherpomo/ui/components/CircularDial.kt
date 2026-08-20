package com.tora.yetanotherpomo.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.tora.yetanotherpomo.ui.theme.Organic
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * The Home screen's drag-to-set-minutes dial: a conic-gradient ring (accent progress over a
 * neutral track), 12 tick marks, a knob riding the outer edge, and the minutes readout in the
 * center. Angle math ports the reference prototype's `minutesFromEvent`/`knobT` 1:1 - degrees
 * measured clockwise from 12 o'clock, `m = round(deg / 360 * max)` clamped to `[5, max]`.
 */
@Composable
fun CircularDial(
    minutes: Int,
    onMinutesChange: (Int) -> Unit,
    maxMinutes: Int,
    modifier: Modifier = Modifier,
    size: Dp = 272.dp,
) {
    val colors = Organic.colors
    val type = Organic.type
    val opacity = Organic.opacity
    val sizes = Organic.size
    val density = LocalDensity.current
    val sizePx = with(density) { size.toPx() }
    val strokeWidth = Organic.stroke.ring
    val tickStroke = Organic.stroke.thin

    fun minutesAt(offset: Offset): Int {
        val center = Offset(sizePx / 2f, sizePx / 2f)
        val dx = offset.x - center.x
        val dy = offset.y - center.y
        var degTop = Math.toDegrees(atan2(dx, -dy).toDouble())
        if (degTop < 0) degTop += 360.0
        val m = (degTop / 360.0 * maxMinutes).roundToInt()
        return m.coerceIn(5, maxMinutes)
    }

    Box(
        modifier = modifier
            .size(size)
            // Two independent detectors on the same pointer stream: taps set the value
            // immediately (matching the reference prototype's onPointerDown), drags track
            // continued movement past touch slop.
            .pointerInput(maxMinutes, sizePx) {
                detectTapGestures(onPress = { offset -> onMinutesChange(minutesAt(offset)) })
            }
            .pointerInput(maxMinutes, sizePx) {
                detectDragGestures(
                    onDragStart = { offset -> onMinutesChange(minutesAt(offset)) },
                    onDrag = { change, _ ->
                        onMinutesChange(minutesAt(change.position))
                        change.consume()
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        // A single animated value drives the ring sweep, the knob angle, and the digit readout
        // together, so they always stay in sync. animateFloatAsState re-targets smoothly on
        // every change - a snappy spring keeps it tracking closely during a drag (target moves
        // every pointer event) while still sweeping visibly for a discrete jump like a preset tap.
        val animatedMinutes by animateFloatAsState(
            targetValue = minutes.toFloat(),
            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium),
            label = "dialMinutes",
        )
        val fraction = (animatedMinutes / maxMinutes.toFloat()).coerceIn(0f, 1f)
        val displayMinutes = animatedMinutes.roundToInt().coerceIn(5, maxMinutes)

        Canvas(modifier = Modifier.size(size)) {
            val strokeWidthPx = strokeWidth.toPx()
            val arcSize = Size(this.size.width - strokeWidthPx, this.size.height - strokeWidthPx)
            val topLeft = Offset(strokeWidthPx / 2f, strokeWidthPx / 2f)
            // Full track, then the accent progress drawn on top - equivalent to the doc's
            // conic-gradient(accent Xdeg, neutral 0).
            drawArc(
                color = colors.neutral.c300,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt),
            )
            if (fraction > 0f) {
                drawArc(
                    color = colors.accent,
                    startAngle = -90f,
                    sweepAngle = 360f * fraction,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidthPx, cap = StrokeCap.Butt),
                )
            }
            // 12 tick marks, evenly spaced, matching the doc's static rotate(i*30deg) ring.
            val tickColor = colors.text.copy(alpha = opacity.tick)
            repeat(12) { i ->
                rotate(degrees = (i * 30).toFloat()) {
                    drawLine(
                        color = tickColor,
                        start = Offset(this.size.width / 2f, 4.5.dp.toPx()),
                        end = Offset(this.size.width / 2f, 13.5.dp.toPx()),
                        strokeWidth = tickStroke.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            }
        }

        // Inner disc covering the ring's inside, leaving only the outer band visible.
        Box(
            modifier = Modifier
                .size(size - strokeWidth * 2)
                .clip(CircleShape)
                .background(colors.bg),
        )

        // Knob riding the outer edge at the current angle.
        val angleRad = Math.toRadians((fraction * 360.0) - 90.0)
        val knobRadiusPx = sizePx / 2f
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset {
                    IntOffset(
                        x = (knobRadiusPx * cos(angleRad)).roundToInt(),
                        y = (knobRadiusPx * sin(angleRad)).roundToInt(),
                    )
                }
                .size(sizes.iconMd)
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(colors.bg),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(sizes.iconSm)
                    .clip(CircleShape)
                    .background(colors.accent),
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = displayMinutes.toString(), style = type.dialDigits, color = colors.text)
            Text(text = "MINUTES", style = type.labelSmall, color = colors.neutral.c600)
        }
    }
}
