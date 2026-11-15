package com.tora.yetanotherpomo.ui.screens.night

import android.content.Context
import android.text.format.DateFormat
import android.text.format.DateUtils
import java.util.Date
import java.util.TimeZone

/**
 * A minute of the day in the phone's 12/24-hour style. Formatted in UTC from the epoch, so a
 * time that doesn't exist today (the hour skipped when clocks go forward) still reads as typed.
 */
fun formatMinuteOfDay(context: Context, minuteOfDay: Int): String =
    DateFormat.getTimeFormat(context)
        .apply { timeZone = TimeZone.getTimeZone("UTC") }
        .format(Date(minuteOfDay * 60_000L))

fun formatWallTime(context: Context, wallMs: Long): String =
    DateFormat.getTimeFormat(context).format(Date(wallMs))

/** "Tue 07:00": used where the day matters, such as when a waiting change takes over. */
fun formatWallDayAndTime(context: Context, wallMs: Long): String =
    DateUtils.formatDateTime(
        context,
        wallMs,
        DateUtils.FORMAT_SHOW_WEEKDAY or DateUtils.FORMAT_ABBREV_WEEKDAY or DateUtils.FORMAT_SHOW_TIME,
    )
