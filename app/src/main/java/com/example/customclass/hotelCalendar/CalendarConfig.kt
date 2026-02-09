package com.example.customclass.hotelCalendar

import android.graphics.Color
import android.graphics.Typeface
import java.util.Calendar
import java.util.Date

data class TextAppearance(
    val textSizeSP: Float = 14f,
    val textColor: Int = Color.BLACK,
    val typeface: Typeface? = Typeface.DEFAULT,
    val isBold: Boolean = false,
    val isItalic: Boolean = false
)

data class CalendarConfig(
    // Weekday configuration
    val weekdayFormat: WeekdayFormat = WeekdayFormat.THREE_LETTER,
    val weekdayAppearance: TextAppearance = TextAppearance(
        textSizeSP = 12f,
        textColor = Color.GRAY,
        isBold = false
    ),

    // Month header configuration
    val monthLabelAlignment: MonthLabelAlignment = MonthLabelAlignment.CENTER,
    val monthHeaderAppearance: TextAppearance = TextAppearance(
        textSizeSP = 16f,
        textColor = Color.DKGRAY,
        isBold = true
    ),

    // Date text configuration
    val dateTextAppearance: TextAppearance = TextAppearance(
        textSizeSP = 14f,
        textColor = Color.BLACK,
        isBold = false
    ),

    // Date label (custom text below date) configuration
    val dateLabelAppearance: TextAppearance = TextAppearance(
        textSizeSP = 12f,
        textColor = Color.GRAY,
        isItalic = false
    ),

    // Selection colors
    val selectedDayColor: Int = Color.parseColor("#6200EE"),
    val selectedDayTextColor: Int = Color.WHITE,  // IMPORTANT: This was missing!
    val rangeBackgroundColor: Int = Color.parseColor("#E3F2FD"),
    val rangeEdgeTextColor: Int = Color.WHITE,
    val rangeMiddleTextColor: Int = Color.WHITE,
    val todayColor: Int = Color.parseColor("#03DAC6"),
    val disabledDateColor: Int = Color.parseColor("#BDBDBD"),

    // Layout configuration
    val showWeekdaysInsideMonth: Boolean = true,
    val cellHeightDP: Float = 48f,
    val dateLabelTopMarginDP: Float = 8f,
    val headerBottomMarginDP: Float = 4f,

    // Shape configuration
    val selectedCircleScale: Float = 0.55f,
    val rangeBandHeightScale: Float = 0.85f,

    // Restrictions
    val minDate: Date? = null,
    val maxDate: Date? = null,
    val disablePastDates: Boolean = true
)

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

// Interface for communicating date selections
interface OnCanvasDateRangeSelectedListener {
    fun onDateRangeSelected(startDate: Date, endDate: Date)
    fun onDateSelectionChanged(startDate: Date?, endDate: Date?)

    fun onInvalidDateSelected(date: Date) {}
    fun onMonthDisplayed(month: Calendar) {}
    fun onDateSelectionCancelled() {}
}