package io.github.zidbrain.fchat.util

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime

private val hourMinuteFormat = LocalTime.Format {
    hour()
    char(':')
    minute()
}

private val dayOfTheWeekFormat = LocalDate.Format {
    dayOfWeek(DayOfWeekNames.ENGLISH_ABBREVIATED)
}

private val dayMonthFormat = LocalDate.Format {
    dayOfMonth()
    char(' ')
    monthName(MonthNames.ENGLISH_FULL)
}

private val dayMonthYearFormat = LocalDate.Format {
    dayOfMonth()
    char(' ')
    monthName(MonthNames.ENGLISH_FULL)
    char(' ')
    year()
}

fun LocalDate.formatForDisplay(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return when {
        (now - this).days < 365 -> format(dayMonthFormat)
        else -> format(dayMonthYearFormat)
    }
}

fun Instant.formatForDisplay(): String {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val dateTime = toLocalDateTime(TimeZone.currentSystemDefault())

    return when {
        now == dateTime.date -> dateTime.time.format(hourMinuteFormat)
        (now - dateTime.date).days <= 7 -> dateTime.date.format(dayOfTheWeekFormat)
        else -> dateTime.date.format(LocalDate.Formats.ISO)
    }
}

fun Instant.formatLocalTime(): String {
    val time = toLocalDateTime(TimeZone.currentSystemDefault()).time
    return time.format(hourMinuteFormat)
}