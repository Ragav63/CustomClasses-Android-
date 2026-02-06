package com.example.customclass.hotelCalendar

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun Int.toWeekdayFormat(): WeekdayFormat {
    return when (this) {
        1 -> WeekdayFormat.ONE_LETTER
        2 -> WeekdayFormat.TWO_LETTER
        3 -> WeekdayFormat.THREE_LETTER
        else -> WeekdayFormat.FULL
    }
}

fun getWeekdayLabel(index: Int, format: WeekdayFormat): String {
    val cal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY + index)
    }

    val pattern = when (format) {
        WeekdayFormat.ONE_LETTER -> "EEEEE"
        WeekdayFormat.TWO_LETTER -> "EE"
        WeekdayFormat.THREE_LETTER -> "EEE"
        WeekdayFormat.FULL -> "EEEE"
    }

    return SimpleDateFormat(pattern, Locale.getDefault()).format(cal.time)
}
