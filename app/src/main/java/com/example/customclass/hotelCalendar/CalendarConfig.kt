package com.example.customclass.hotelCalendar

import android.graphics.Color

enum class WeekdayFormat {
    ONE_LETTER,
    TWO_LETTER,
    THREE_LETTER,
    FULL
}

enum class MonthLabelAlignment {
    START,
    CENTER,
    END
}

data class CalendarConfig(
    val weekdayFormat: WeekdayFormat = WeekdayFormat.THREE_LETTER,
    val monthLabelAlignment: MonthLabelAlignment = MonthLabelAlignment.CENTER,

    val dayTextColor: Int,
    val dateLabelColor: Int,
    val headerTextColor: Int,
    val weekdayTextColor: Int,

    val selectedDayColor: Int,
    val rangeBackgroundColor: Int,
    val rangeEdgeTextColor: Int,
    val rangeMiddleTextColor: Int
)
