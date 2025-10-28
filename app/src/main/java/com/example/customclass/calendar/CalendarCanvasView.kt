package com.example.customclass.calendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.customclass.R
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.apply
import kotlin.collections.forEach
import kotlin.collections.map
import kotlin.collections.set
import kotlin.let
import kotlin.math.min // Add this line
import kotlin.ranges.coerceAtMost
import kotlin.ranges.until

// Interface for communicating date selections
interface OnCanvasDateRangeSelectedListener {
    fun onDateRangeSelected(startDate: Date, endDate: Date)
    fun onDateSelectionCancelled() // Not directly used in this basic example, but good to keep
    fun onInvalidDateSelected(date: Date) // For dates outside min/max
    fun onDateSelectionChanged(startDate: Date?, endDate: Date?) // Updates as selection progresses
    fun onMonthDisplayed(month: Calendar) // <--- New in interface
}

// Data class to hold custom text and its color for a specific date
data class DateLabel(val date: Date, val text: String)

class CalendarCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val calendarDayTextSize: Float
        get() = if (resources.configuration.smallestScreenWidthDp >= 600) {
            18.spToPx() // Tablet
        } else {
            16.spToPx() // Mobile
        }

    private val calendarDateLabelTextSize: Float
        get() = if (resources.configuration.smallestScreenWidthDp >= 600) {
            16.spToPx() // Tablet
        } else {
            14.spToPx() // Mobile
        }

    private val calendarHeaderTextSize = 20.spToPx()
    private val calendarWeekdayTextSize = 14.spToPx()


    // Color values
    private val defaultDayTextColor = Color.BLACK
    private val defaultDateLabelColor = Color.BLACK
    private val defaultSelectedDayColor = Color.parseColor("#6200EE") // Material primary color
    private val defaultRangeBackgroundColor = Color.parseColor("#E3F2FD") // Light blue
    private val defaultHeaderColor = Color.DKGRAY
    private val defaultWeekDayColor = Color.GRAY
    private val todayColor = Color.parseColor("#03DAC6") // Teal
    private val disabledDateColor = Color.parseColor("#BDBDBD") // Grey

    // Extension function to convert sp to px
    private fun Int.spToPx(): Float {
        return this * resources.displayMetrics.scaledDensity
    }
    private val dayTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = calendarDayTextSize
        textAlign = Paint.Align.CENTER
    }

    // NEW: Paint for the custom date labels
    private val dateLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = calendarDateLabelTextSize
        textAlign = Paint.Align.CENTER
    }

    private val selectedDayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = defaultSelectedDayColor
        style = Paint.Style.FILL
    }

    private val rangeBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = defaultRangeBackgroundColor
        style = Paint.Style.FILL
    }

    private val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = calendarHeaderTextSize
        textAlign = Paint.Align.CENTER
        setTypeface(Typeface.DEFAULT_BOLD)
    }

    private val weekDayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textSize = calendarWeekdayTextSize
        textAlign = Paint.Align.CENTER
    }

    // This will now be set externally for each month.
    // The `set` block below handles the logic that was in the redundant `setDisplayedMonth` function.
    var displayedMonth: Calendar = Calendar.getInstance()
        set(value) {
            field = value.clone() as Calendar // Clone to ensure the original Calendar object isn't modified
            field.set(Calendar.DAY_OF_MONTH, 1) // Ensure it's the first of the month
            onDateRangeSelectedListener?.onMonthDisplayed(field) // Notify about the displayed month
            invalidate() // Redraw when the month changes
            requestLayout()
        }

    // New: Allow setting the selected range from outside
    var selectedStartDate: Date? = null
        set(value) {
            field = value?.normalizeDate() // Normalize when setting
            invalidate()
        }

    var selectedEndDate: Date? = null
        set(value) {
            field = value?.normalizeDate() // Normalize when setting
            invalidate()
        }

    // Removed the `private val currentMonth` as it's redundant now.

    private var minSelectableDate: Date? = null
    private var maxSelectableDate: Date? = null

    var onDateRangeSelectedListener: OnCanvasDateRangeSelectedListener? = null

    // NEW: Map to store custom labels for dates (Date -> DateLabel)
    private val customDateLabels = mutableMapOf<Date, DateLabel>()

    // Corrected format for year "y" and then "MMMM" for full month name
    private val dateFormatMonthYear = SimpleDateFormat("MMMM y", Locale.getDefault())
    private val dayOfWeekFormat = SimpleDateFormat("EEEEE", Locale.getDefault()) // "E" for abbreviated day, "EEEEE" for narrow
    private val daysOfWeek = (0..6).map { i ->
        val cal = Calendar.getInstance().apply { set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY + i) }
        dayOfWeekFormat.format(cal.time)
    }

    private var cellWidth = 0f
    private var cellHeight = 0f
    private val headerHeight get() = calendarHeaderTextSize * 2.5f
    private val weekdaysHeaderHeight get() = calendarWeekdayTextSize * 2f

    // Added margin for date labels
    private val dateLabelTopMargin = 15f // Adjust this value for more or less margin
    private val selectedShapePadding = 2f // Adjust this value to make the selected circle/roundRect smaller or larger

    // Today's date, normalized to start of day, for comparison with past dates
    private val todayNormalized = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.time

    // GestureDetector to handle clicks
    private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            handleTap(e.x, e.y)
            return true
        }

        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            if (selectedStartDate != null && selectedEndDate == null) {
                handleTap(e2.x, e2.y, isDrag = true)
            }
            return true
        }

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }
    }).apply {
        setIsLongpressEnabled(false) // Disable long press to make scrolling smoother
    }

    init {
        // Initialize with default min/max dates if needed, or set them externally
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val fourteenMonthsLater = Calendar.getInstance().apply {
            time = today.time
            add(Calendar.MONTH, 14)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }
        minSelectableDate = today.time
        maxSelectableDate = fourteenMonthsLater.time
    }

    fun setDayTextColor(colorResId: Int) {
        dayTextPaint.color = ContextCompat.getColor(context, colorResId)
        dayTextPaint.textSize = calendarDayTextSize
        invalidate()
    }

    fun setDateLabelColor(colorResId: Int) {
        dateLabelPaint.color = ContextCompat.getColor(context, colorResId)
        dateLabelPaint.textSize = calendarDateLabelTextSize
        invalidate()
    }

    fun setSelectedDayColor(colorResId: Int) {
        selectedDayPaint.color = ContextCompat.getColor(context, colorResId)
        invalidate()
    }

    fun setRangeBackgroundColor(colorResId: Int) {
        rangeBackgroundPaint.color = ContextCompat.getColor(context, colorResId)
        invalidate()
    }

    fun setHeaderColor(colorResId: Int) {
        headerPaint.color = ContextCompat.getColor(context, colorResId)
        invalidate()
    }

    fun setWeekDayColor(colorResId: Int) {
        weekDayPaint.color = ContextCompat.getColor(context, colorResId)
        invalidate()
    }

    fun setMinMaxDates(minDate: Date, maxDate: Date) {
        this.minSelectableDate = minDate
        this.maxSelectableDate = maxDate
        invalidate() // Redraw the calendar
    }



    /**
     * Clears all existing custom date labels.
     */
    fun clearDateLabels() {
        customDateLabels.clear()
        invalidate()
    }

    /**
     * Sets custom labels for multiple dates at once.
     * Pass a list of DateLabel objects.
     */
    fun setDateLabels(labels: List<DateLabel>) {
        customDateLabels.clear() // Clear existing ones
        labels.forEach { label ->
            customDateLabels[label.date.normalizeDate()] = DateLabel(label.date.normalizeDate(), label.text)
        }
        invalidate()
    }

    private fun getNumberOfWeeksInMonth(month: Calendar): Int {
        val tempCal = month.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1) // Go to the first day of the month

        // Get the day of the week for the 1st of the month (1=Sunday, 7=Saturday)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)

        // Get the total days in the month
        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Calculate offset for the first week (days before the 1st)
        val offset = (firstDayOfWeek - Calendar.SUNDAY + 7) % 7 // Adjust to make Sunday=0, Monday=1, etc. if needed, or simply use firstDayOfWeek -1 if Sunday is 1st day.
        // Using Calendar.SUNDAY as first day of week gives us a correct 0-6 index for days before first day of month.
        // Total slots needed: offset (for days before 1st) + daysInMonth
        val totalSlots = offset + daysInMonth

        // Calculate rows needed (ceil division)
        return (totalSlots + 6) / 7 // Integer division trick for ceiling
    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        cellWidth = w / 7f

        // REDUCED: The total height available for days is now divided by the actual number of weeks
        // This will make the calendar fit more snugly, reducing space below shorter months.
        // You might need to slightly adjust header/weekday heights to fit.
        val dynamicRows = getNumberOfWeeksInMonth(displayedMonth)
        if (dynamicRows > 0) { // Avoid division by zero
            cellHeight = (h - headerHeight - weekdaysHeaderHeight) / dynamicRows.toFloat()
        } else {
            cellHeight = (h - headerHeight - weekdaysHeaderHeight) / 6f // Fallback
        }
        // Ensure cellHeight isn't ridiculously large if view is very tall and month is short
        val maxCellHeight = if (resources.configuration.smallestScreenWidthDp >= 600) {
            200f // Larger for tablets
        } else {
            170f // Default for phones
        }
        cellHeight = cellHeight.coerceAtMost(maxCellHeight)

        // If you want a fixed min cell height to prevent tiny cells:
        // val minCellHeight = 100f // Adjust as needed
        // cellHeight = cellHeight.coerceAtLeast(minCellHeight)
    }


    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredWidth = MeasureSpec.getSize(widthMeasureSpec)
        val dynamicRows = getNumberOfWeeksInMonth(displayedMonth)

        // Calculate a temporary cellHeight based on the measured height,
        // and then apply the maxCellHeight constraint.
        val maxCellHeight = if (resources.configuration.smallestScreenWidthDp >= 600) {
            200f
        } else {
            170f
        }
        val measureHeight = MeasureSpec.getSize(heightMeasureSpec)
        var calculatedCellHeight = (measureHeight - headerHeight - weekdaysHeaderHeight) / dynamicRows.toFloat()

        calculatedCellHeight = calculatedCellHeight.coerceAtMost(maxCellHeight)

        val desiredHeight = (headerHeight + weekdaysHeaderHeight + dynamicRows * calculatedCellHeight).toInt()

        setMeasuredDimension(
            resolveSize(desiredWidth, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )
    }



    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (width == 0 || height == 0) return

        // 1. Draw Month and Year Header
        val headerText = dateFormatMonthYear.format(displayedMonth.time)
        canvas.drawText(headerText, width / 2f, headerHeight / 2f + headerPaint.textSize / 3, headerPaint)

        // 2. Draw Weekday Headers
        for (i in 0 until 7) {
            val x = cellWidth / 2f + i * cellWidth
            val y = headerHeight + weekdaysHeaderHeight / 2f + weekDayPaint.textSize / 3
            canvas.drawText(daysOfWeek[i], x, y, weekDayPaint)
        }

        // 3. Draw Calendar Days
        val calendar = displayedMonth.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)
        calendar.add(Calendar.DAY_OF_MONTH, -(firstDayOfMonth - 1))

        val originalDayTextColor = dayTextPaint.color

        val originalDateLabelColor = dateLabelPaint.color

        // MODIFIED: Use dynamic number of rows
        val numRowsToDraw = getNumberOfWeeksInMonth(displayedMonth)

        for (row in 0 until numRowsToDraw) { // Loop only for the actual number of weeks
            for (col in 0 until 7) {
                val x = col * cellWidth
                val y = headerHeight + weekdaysHeaderHeight + row * cellHeight

                val dayDate = calendar.time.normalizeDate()
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                val isCurrentMonth = calendar.get(Calendar.MONTH) == displayedMonth.get(Calendar.MONTH)
                val isToday = isSameDay(dayDate, todayNormalized)
                val isSelectable = isDateSelectable(dayDate)
                val isPastDate = dayDate.before(todayNormalized)

                val currentDayIsSelected = (selectedStartDate != null && isSameDay(dayDate, selectedStartDate)) ||
                        (selectedEndDate != null && isSameDay(dayDate, selectedEndDate)) ||
                        (selectedStartDate != null && selectedEndDate != null &&
                                dayDate.after(selectedStartDate) && dayDate.before(selectedEndDate))

                // This RectF will now be used for ALL selected backgrounds,
                // ensuring consistent padding/height.
                val paddedBackgroundRectF = RectF(
                    x + selectedShapePadding,
                    y + selectedShapePadding,
                    x + cellWidth - selectedShapePadding,
                    y + cellHeight - selectedShapePadding
                )
                val cornerRadius = min(
                    paddedBackgroundRectF.width(),
                    paddedBackgroundRectF.height()
                ) / 4f

                // Determine selection states for the current day
                val isStartDate = selectedStartDate != null && isSameDay(dayDate, selectedStartDate)
                val isEndDate = selectedEndDate != null && isSameDay(dayDate, selectedEndDate)
                val isDateBetweenRange = selectedStartDate != null && selectedEndDate != null &&
                        dayDate.after(selectedStartDate) && dayDate.before(selectedEndDate)


                if (isCurrentMonth) {
                    when {
                        // Case 1: Only a single date is selected (start of a new selection)
                        selectedStartDate != null && selectedEndDate == null && isSameDay(dayDate, selectedStartDate) -> {
                            canvas.drawRoundRect(paddedBackgroundRectF, cornerRadius, cornerRadius, selectedDayPaint)
                        }
                        // Case 2: A range is selected, and this is both the start and end (a single day was selected as a completed range)
                        isStartDate && isEndDate -> {
                            canvas.drawCircle(paddedBackgroundRectF.centerX(), paddedBackgroundRectF.centerY(), cornerRadius, selectedDayPaint)
                        }
                        // Case 3: This is the start date of a selected range
                        isStartDate -> {
                            canvas.drawRoundRect(paddedBackgroundRectF, cornerRadius, cornerRadius, selectedDayPaint)
                            // Extend the color to the right for the range.
                            // The right half of the paddedBackgroundRectF is drawn.
                            canvas.drawRect(paddedBackgroundRectF.centerX(), paddedBackgroundRectF.top, paddedBackgroundRectF.right, paddedBackgroundRectF.bottom, selectedDayPaint)
                        }
                        // Case 4: This is the end date of a selected range
                        isEndDate -> {
                            canvas.drawRoundRect(paddedBackgroundRectF, cornerRadius, cornerRadius, selectedDayPaint)
                            // Extend the color to the left for the range.
                            // The left half of the paddedBackgroundRectF is drawn.
                            canvas.drawRect(paddedBackgroundRectF.left, paddedBackgroundRectF.top, paddedBackgroundRectF.centerX(), paddedBackgroundRectF.bottom, selectedDayPaint)
                        }
                        // Case 5: This date is within the selected range (but not start or end)
                        // MODIFIED: Use paddedBackgroundRectF to maintain consistent height
                        isDateBetweenRange -> {
                            canvas.drawRect(paddedBackgroundRectF, rangeBackgroundPaint)
                        }
                    }

                    // Determine text color based on selection state
                    val dayTextColor = when { // Renamed to avoid clash with dateLabelPaint.color setting
                        !isSelectable -> disabledDateColor// Non-selectable dates are grey
                        isStartDate || isEndDate || isDateBetweenRange -> Color.WHITE // Selected dates are white
                        isToday -> todayColor // Today, if not selected, is light blue
                        else -> originalDayTextColor // Use the color set by setDayTextColor() for default days
                    }

                    dayTextPaint.color = dayTextColor
                    val centerX = x + cellWidth / 2
                    // --- MODIFIED: More robust vertical centering for day text ---
                    val dayTextCenterY = y + cellHeight / 2 - ((dayTextPaint.descent() + dayTextPaint.ascent()) / 2)
                    canvas.drawText(dayOfMonth.toString(), centerX, dayTextCenterY, dayTextPaint)

                    val normalizedDayDate = dayDate.normalizeDate()
                    if (!isPastDate) {
                        customDateLabels[normalizedDayDate]?.let { label ->
                            dateLabelPaint.color = if (currentDayIsSelected) {
                                Color.WHITE
                            } else {
                                originalDateLabelColor
                            }
                            // --- MODIFIED: Position label relative to the centered day text ---
                            // We use dayTextPaint.descent() to get the distance from the baseline to the bottom of the text.
                            // Adding dateLabelTopMargin and half of dateLabelPaint.textSize will place the label correctly below.
                            canvas.drawText(label.text, centerX, dayTextCenterY + dayTextPaint.descent() + dateLabelTopMargin + (dateLabelPaint.textSize / 2), dateLabelPaint)
                        }
                    }
                }

                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
        dayTextPaint.color = originalDayTextColor
        dateLabelPaint.color = originalDateLabelColor

    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Handle scroll for date selection if selectedStartDate is already set and selectedEndDate is null
        if (event.action == MotionEvent.ACTION_MOVE && selectedStartDate != null && selectedEndDate == null) {
            handleTap(event.x, event.y, isDrag = true)
            return true // Consume the event for dragging
        }

        val result = gestureDetector.onTouchEvent(event)

        // For ACTION_UP, if we were dragging, consider the range selected
        if (event.action == MotionEvent.ACTION_UP && selectedStartDate != null && selectedEndDate != null && selectedStartDate != selectedEndDate) {
            onDateRangeSelectedListener?.onDateRangeSelected(selectedStartDate!!, selectedEndDate!!)
        }

        return result || super.onTouchEvent(event)
    }

    private fun handleTap(x: Float, y: Float, isDrag: Boolean = false) {
        val row = ((y - headerHeight - weekdaysHeaderHeight) / cellHeight).toInt()
        val col = (x / cellWidth).toInt()

        val numRowsDisplayed = getNumberOfWeeksInMonth(displayedMonth)

        if (row < 0 || row >= numRowsDisplayed || col < 0 || col >= 7) return

        val calendar = displayedMonth.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonthOffset = calendar.get(Calendar.DAY_OF_WEEK) - 1
        calendar.add(Calendar.DAY_OF_MONTH, (row * 7 + col) - firstDayOfMonthOffset)

        val tappedDate = calendar.time.normalizeDate() // Use the top-level normalizeDate()

        if (!isDateSelectable(tappedDate)) {
            onDateRangeSelectedListener?.onInvalidDateSelected(tappedDate)
            return
        }

        when {
            selectedStartDate == null -> {
                // First selection (always a tap)
                selectedStartDate = tappedDate
                selectedEndDate = null
                onDateRangeSelectedListener?.onDateSelectionChanged(selectedStartDate, selectedEndDate)
            }
            selectedEndDate == null -> {
                // Second selection or drag to select end date
                if (tappedDate.before(selectedStartDate)) {
                    // If tapped/dragged before start date, swap them
                    selectedEndDate = selectedStartDate
                    selectedStartDate = tappedDate
                } else {
                    selectedEndDate = tappedDate
                }
                onDateRangeSelectedListener?.onDateSelectionChanged(selectedStartDate, selectedEndDate)

                // If this is a tap (not a drag), and now a range is complete, notify
                if (!isDrag && selectedStartDate != null && selectedEndDate != null && selectedStartDate != selectedEndDate) {
                    onDateRangeSelectedListener?.onDateRangeSelected(selectedStartDate!!, selectedEndDate!!)
                }
            }
            // If already a range is selected and user taps again, reset selection (start a new one)
            else -> {
                selectedStartDate = tappedDate
                selectedEndDate = null
                onDateRangeSelectedListener?.onDateSelectionChanged(selectedStartDate, selectedEndDate)
            }
        }

        invalidate() // Force redraw
    }

    private fun isSameDay(date1: Date?, date2: Date?): Boolean {
        if (date1 == null || date2 == null) return false
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    private fun isDateSelectable(date: Date): Boolean {
        val normalizedDate = date.normalizeDate()

        val normalizedMin = minSelectableDate?.normalizeDate()
        val normalizedMax = maxSelectableDate?.let {
            Calendar.getInstance().apply {
                time = it
                set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
            }.time
        }?.normalizeDate()

        return (normalizedMin == null || !normalizedDate.before(normalizedMin)) &&
                (normalizedMax == null || !normalizedDate.after(normalizedMax))
    }


}

fun Date.normalizeDate(): Date {
    val cal = Calendar.getInstance().apply { time = this@normalizeDate }
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    return cal.time
}

fun Calendar.normalizeMonth(): Calendar {
    val normalized = this.clone() as Calendar
    normalized.set(Calendar.DAY_OF_MONTH, 1)
    normalized.set(Calendar.HOUR_OF_DAY, 0)
    normalized.set(Calendar.MINUTE, 0)
    normalized.set(Calendar.SECOND, 0)
    normalized.set(Calendar.MILLISECOND, 0)
    return normalized
}