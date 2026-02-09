package com.example.customclass.hotelCalendar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class CalendarCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Fixed: Properly initialize config
    private var _config = CalendarConfig()
    var config: CalendarConfig = _config
        set(value) {
            field = value
            applyConfig(value)
        }

    // Extension function to convert sp to px
    private fun Float.spToPx(): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP,
            this,
            resources.displayMetrics
        )
    }

    private fun Float.dpToPx(): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this,
            resources.displayMetrics
        )
    }

    // Paint objects
    private val dayTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val dateLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val selectedDayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val rangeBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val weekDayPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private var cellWidth = 0f
    private var cellHeight = 0f

    // Fixed: Use stored config instead of accessing it directly in getters
    private val headerHeight: Float
        get() = _config.monthHeaderAppearance.textSizeSP.spToPx() * 1.8f +
                _config.headerBottomMarginDP.dpToPx()

    private val weekdaysHeaderHeight: Float
        get() = _config.weekdayAppearance.textSizeSP.spToPx() * 1.6f

    private val dateLabelTopMargin: Float
        get() = _config.dateLabelTopMarginDP.dpToPx()

    // Layout variables
    var showWeekdayLabels: Boolean = _config.showWeekdaysInsideMonth
        set(value) {
            field = value
            requestLayout()
            invalidate()
        }

    var displayedMonth: Calendar = Calendar.getInstance()
        set(value) {
            field = value.clone() as Calendar
            field.set(Calendar.DAY_OF_MONTH, 1)
            onDateRangeSelectedListener?.onMonthDisplayed(field)
            invalidate()
            requestLayout()
        }

    var selectedStartDate: Date? = null
        set(value) {
            field = value?.normalizeDate()
            invalidate()
        }

    var selectedEndDate: Date? = null
        set(value) {
            field = value?.normalizeDate()
            invalidate()
        }

    var monthLabelAlignment: MonthLabelAlignment = _config.monthLabelAlignment
        set(value) {
            field = value
            invalidate()
        }

    private var minSelectableDate: Date? = null
    private var maxSelectableDate: Date? = null

    var onDateRangeSelectedListener: OnCanvasDateRangeSelectedListener? = null
    private val customDateLabels = mutableMapOf<Date, DateLabel>()

    // Formatting
    private val dateFormatMonthYear = SimpleDateFormat("MMMM y", Locale.getDefault())
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

        override fun onDown(e: MotionEvent): Boolean = true
    }).apply {
        setIsLongpressEnabled(false)
    }

    init {
        applyConfig(_config)
    }

    fun applyConfig(newConfig: CalendarConfig) {
        _config = newConfig

        // Apply month header appearance
        applyTextAppearance(headerPaint, newConfig.monthHeaderAppearance)

        // Apply weekday appearance
        applyTextAppearance(weekDayPaint, newConfig.weekdayAppearance)

        // Apply date text appearance
        applyTextAppearance(dayTextPaint, newConfig.dateTextAppearance)

        // Apply date label appearance
        applyTextAppearance(dateLabelPaint, newConfig.dateLabelAppearance)

        // Apply colors
        selectedDayPaint.color = newConfig.selectedDayColor
        rangeBackgroundPaint.color = newConfig.rangeBackgroundColor

        // Set min/max dates
        minSelectableDate = newConfig.minDate?.normalizeDate()
        maxSelectableDate = newConfig.maxDate?.let {
            Calendar.getInstance().apply {
                time = it
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.time
        }?.normalizeDate()

        // Update showWeekdayLabels
        showWeekdayLabels = newConfig.showWeekdaysInsideMonth

        // Update monthLabelAlignment
        monthLabelAlignment = newConfig.monthLabelAlignment

        invalidate()
        requestLayout()
    }

    private fun applyTextAppearance(paint: Paint, appearance: TextAppearance) {
        paint.textSize = appearance.textSizeSP.spToPx()
        paint.color = appearance.textColor

        val style = when {
            appearance.isBold && appearance.isItalic -> Typeface.BOLD_ITALIC
            appearance.isBold -> Typeface.BOLD
            appearance.isItalic -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }

        val typeface = appearance.typeface ?: Typeface.DEFAULT
        paint.typeface = Typeface.create(typeface, style)
    }

    fun getWeekdayLabel(dayIndex: Int): String {
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY + dayIndex)
        }

        return when (_config.weekdayFormat) {
            WeekdayFormat.ONE_LETTER ->
                SimpleDateFormat("EEEEE", Locale.getDefault()).format(cal.time)
            WeekdayFormat.TWO_LETTER ->
                SimpleDateFormat("EE", Locale.getDefault()).format(cal.time).take(2)
            WeekdayFormat.THREE_LETTER ->
                SimpleDateFormat("EEE", Locale.getDefault()).format(cal.time)
            WeekdayFormat.FULL ->
                SimpleDateFormat("EEEE", Locale.getDefault()).format(cal.time)
        }
    }

    private fun getNumberOfWeeksInMonth(month: Calendar): Int {
        val tempCal = month.clone() as Calendar
        tempCal.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
        val daysInMonth = tempCal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val offset = (firstDayOfWeek - Calendar.SUNDAY + 7) % 7
        val totalSlots = offset + daysInMonth
        return (totalSlots + 6) / 7
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val rows = getNumberOfWeeksInMonth(displayedMonth)

        // Base cell height from config (converted from dp to px)
        val baseCellHeight = _config.cellHeightDP.dpToPx()

        // Calculate desired height
        val desiredHeight = (
                headerHeight +
                        (if (showWeekdayLabels) weekdaysHeaderHeight else 16.dpToPx()) +
                        rows * baseCellHeight
                ).toInt()

        // Set measured dimensions
        setMeasuredDimension(
            resolveSize(widthSize, widthMeasureSpec),
            resolveSize(desiredHeight, heightMeasureSpec)
        )

        // Update cell dimensions
        cellWidth = widthSize / 7f
        cellHeight = baseCellHeight
    }

    override fun onDraw(canvas: Canvas) {
        if (width == 0 || height == 0) return

        val rangeBandHeight = cellHeight * _config.rangeBandHeightScale

        // Draw Month Header
        drawMonthHeader(canvas)

        // Draw Weekday Headers
        if (showWeekdayLabels) {
            drawWeekdayHeaders(canvas)
        }

        // Draw Calendar Days
        drawCalendarDays(canvas, rangeBandHeight)
    }

    private fun drawMonthHeader(canvas: Canvas) {
        val headerText = dateFormatMonthYear.format(displayedMonth.time)
        val headerY = headerHeight / 2f + headerPaint.textSize / 3

        val headerX = when (monthLabelAlignment) {
            MonthLabelAlignment.START -> {
                headerPaint.textAlign = Paint.Align.LEFT
                16f
            }
            MonthLabelAlignment.CENTER -> {
                headerPaint.textAlign = Paint.Align.CENTER
                width / 2f
            }
            MonthLabelAlignment.END -> {
                headerPaint.textAlign = Paint.Align.RIGHT
                width - 16f
            }
        }

        canvas.drawText(headerText, headerX, headerY, headerPaint)
    }

    private fun drawWeekdayHeaders(canvas: Canvas) {
        for (i in 0 until 7) {
            val x = cellWidth / 2f + i * cellWidth
            val y = headerHeight + weekdaysHeaderHeight / 2f + weekDayPaint.textSize / 3
            canvas.drawText(getWeekdayLabel(i), x, y, weekDayPaint)
        }
    }

    private fun drawCalendarDays(canvas: Canvas, rangeBandHeight: Float) {
        val startY = headerHeight +
                (if (showWeekdayLabels) weekdaysHeaderHeight else 16.dpToPx())

        val calendar = displayedMonth.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK)
        calendar.add(Calendar.DAY_OF_MONTH, -(firstDayOfMonth - 1))

        val originalDayTextColor = dayTextPaint.color
        val originalDateLabelColor = dateLabelPaint.color
        val numRowsToDraw = getNumberOfWeeksInMonth(displayedMonth)

        for (row in 0 until numRowsToDraw) {
            for (col in 0 until 7) {
                val x = col * cellWidth
                val y = startY + row * cellHeight

                val dayDate = calendar.time.normalizeDate()
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                val isCurrentMonth = calendar.get(Calendar.MONTH) == displayedMonth.get(Calendar.MONTH)
                val isToday = isSameDay(dayDate, todayNormalized)
                val isSelectable = isDateSelectable(dayDate)
                val isPastDate = dayDate.before(todayNormalized)

                val isStartDate = selectedStartDate != null && isSameDay(dayDate, selectedStartDate)
                val isEndDate = selectedEndDate != null && isSameDay(dayDate, selectedEndDate)
                val isDateBetweenRange = selectedStartDate != null && selectedEndDate != null &&
                        dayDate.after(selectedStartDate) && dayDate.before(selectedEndDate)

                val cellRectF = RectF(x, y, x + cellWidth, y + cellHeight)
                val rangeCenterY = y + cellHeight / 2f
                val rangeRectTop = rangeCenterY - rangeBandHeight / 2f
                val rangeRectBottom = rangeCenterY + rangeBandHeight / 2f

                if (isCurrentMonth) {
                    // FIXED: Draw range background BEFORE drawing date circles
                    drawRangeBackground(canvas, cellRectF, rangeRectTop, rangeRectBottom,
                        isStartDate, isEndDate, isDateBetweenRange)

                    // FIXED: Draw selected circles - this was missing!
                    if (isStartDate || isEndDate) {
                        drawSelectedCircle(canvas, cellRectF)
                    }

                    // Draw date text
                    drawDateText(canvas, cellRectF, dayOfMonth,
                        isSelectable, isStartDate || isEndDate, isDateBetweenRange, isToday)

                    // Draw date label
                    if (!isPastDate) {
                        drawDateLabel(
                            canvas,
                            cellRectF,
                            dayDate,
                            isStartDate,
                            isEndDate
                        )
                    }
                }

                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        dayTextPaint.color = originalDayTextColor
        dateLabelPaint.color = originalDateLabelColor
    }

    private fun drawRangeBackground(
        canvas: Canvas,
        cellRectF: RectF,
        rangeRectTop: Float,
        rangeRectBottom: Float,
        isStartDate: Boolean,
        isEndDate: Boolean,
        isDateBetweenRange: Boolean
    ) {
        // FIXED: Check if we have a valid range (not same day)
        val isRangeValid = selectedStartDate != null &&
                selectedEndDate != null &&
                !isSameDay(selectedStartDate, selectedEndDate)

        if (isRangeValid) {
            when {
                isStartDate && !isEndDate -> {
                    // Draw right half for start date
                    canvas.drawRect(
                        cellRectF.centerX(),
                        rangeRectTop,
                        cellRectF.right,
                        rangeRectBottom,
                        rangeBackgroundPaint
                    )
                }
                isEndDate && !isStartDate -> {
                    // Draw left half for end date
                    canvas.drawRect(
                        cellRectF.left,
                        rangeRectTop,
                        cellRectF.centerX(),
                        rangeRectBottom,
                        rangeBackgroundPaint
                    )
                }
                isDateBetweenRange -> {
                    // Draw full width for dates between range
                    canvas.drawRect(
                        cellRectF.left,
                        rangeRectTop,
                        cellRectF.right,
                        rangeRectBottom,
                        rangeBackgroundPaint
                    )
                }
            }
        }
    }

    private fun drawSelectedCircle(canvas: Canvas, cellRectF: RectF) {
        val padding = cellRectF.width() * (1 - _config.selectedCircleScale) / 2
        val circleRectF = RectF(
            cellRectF.left + padding,
            cellRectF.top + padding,
            cellRectF.right - padding,
            cellRectF.bottom - padding
        )
        canvas.drawCircle(
            circleRectF.centerX(),
            circleRectF.centerY(),
            min(circleRectF.width(), circleRectF.height()) / 2f,
            selectedDayPaint
        )
    }

    private fun drawDateText(
        canvas: Canvas,
        cellRectF: RectF,
        dayOfMonth: Int,
        isSelectable: Boolean,
        isEdgeDate: Boolean,
        isBetweenRange: Boolean,
        isToday: Boolean
    ) {
        val textColor = when {
            !isSelectable -> _config.disabledDateColor
            isEdgeDate -> _config.selectedDayTextColor
            isBetweenRange -> _config.rangeMiddleTextColor
            isToday -> _config.todayColor
            else -> _config.dateTextAppearance.textColor
        }

        dayTextPaint.color = textColor
        val centerX = cellRectF.centerX()
        val dayTextCenterY = cellRectF.centerY() -
                ((dayTextPaint.descent() + dayTextPaint.ascent()) / 2)

        canvas.drawText(dayOfMonth.toString(), centerX, dayTextCenterY, dayTextPaint)
    }

    private fun drawDateLabel(
        canvas: Canvas,
        cellRectF: RectF,
        dayDate: Date,
        isStartDate: Boolean,
        isEndDate: Boolean
    ) {
        customDateLabels[dayDate.normalizeDate()]?.let { label ->
            val centerX = cellRectF.centerX()
            val dayTextCenterY = cellRectF.centerY() -
                    ((dayTextPaint.descent() + dayTextPaint.ascent()) / 2)

            dateLabelPaint.color = if (isStartDate || isEndDate) {
                _config.selectedDayTextColor // Use config color instead of hardcoded white
            } else {
                _config.dateLabelAppearance.textColor
            }

            canvas.drawText(
                label.text,
                centerX,
                dayTextCenterY + dayTextPaint.descent() + dateLabelTopMargin +
                        (dateLabelPaint.textSize / 2),
                dateLabelPaint
            )
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_MOVE &&
            selectedStartDate != null &&
            selectedEndDate == null) {
            handleTap(event.x, event.y, isDrag = true)
            return true
        }

        val result = gestureDetector.onTouchEvent(event)

        if (event.action == MotionEvent.ACTION_UP &&
            selectedStartDate != null &&
            selectedEndDate != null) {
            onDateRangeSelectedListener?.onDateRangeSelected(
                selectedStartDate!!,
                selectedEndDate!!
            )
        }

        return result || super.onTouchEvent(event)
    }

    private fun handleTap(x: Float, y: Float, isDrag: Boolean = false) {
        val startY = headerHeight +
                (if (showWeekdayLabels) weekdaysHeaderHeight else 16.dpToPx())

        val row = ((y - startY) / cellHeight).toInt()
        val col = (x / cellWidth).toInt()

        val numRowsDisplayed = getNumberOfWeeksInMonth(displayedMonth)

        if (row < 0 || row >= numRowsDisplayed || col < 0 || col >= 7) return

        val calendar = displayedMonth.clone() as Calendar
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonthOffset = calendar.get(Calendar.DAY_OF_WEEK) - 1
        calendar.add(Calendar.DAY_OF_MONTH, (row * 7 + col) - firstDayOfMonthOffset)

        val tappedDate = calendar.time.normalizeDate()

        if (!isDateSelectable(tappedDate)) {
            onDateRangeSelectedListener?.onInvalidDateSelected(tappedDate)
            return
        }

        // Handle date selection logic...
        handleDateSelection(tappedDate)
        invalidate()
    }

    private fun Int.dpToPx(): Float {
        return this.toFloat().dpToPx()
    }


    private fun handleDateSelection(tappedDate: Date) {
        when {
            selectedStartDate == null -> {
                selectedStartDate = tappedDate
                selectedEndDate = tappedDate
                onDateRangeSelectedListener?.onDateSelectionChanged(
                    selectedStartDate,
                    selectedEndDate
                )
            }
            selectedStartDate != null &&
                    selectedEndDate != null &&
                    isSameDay(selectedStartDate, selectedEndDate) -> {
                if (tappedDate.before(selectedStartDate)) {
                    selectedStartDate = tappedDate
                    selectedEndDate = tappedDate
                } else {
                    selectedEndDate = tappedDate
                }
                onDateRangeSelectedListener?.onDateSelectionChanged(
                    selectedStartDate,
                    selectedEndDate
                )
            }
            else -> {
                selectedStartDate = tappedDate
                selectedEndDate = tappedDate
                onDateRangeSelectedListener?.onDateSelectionChanged(
                    selectedStartDate,
                    selectedEndDate
                )
            }
        }
    }

    private fun isSameDay(date1: Date?, date2: Date?): Boolean {
        if (date1 == null || date2 == null) return false
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH)
    }

    private val todayNormalized = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    private fun isDateSelectable(date: Date): Boolean {
        val normalizedDate = date.normalizeDate()
        val normalizedMin = minSelectableDate?.normalizeDate()
        val normalizedMax = maxSelectableDate?.normalizeDate()

        val isAfterMin = normalizedMin == null || !normalizedDate.before(normalizedMin)
        val isBeforeMax = normalizedMax == null || !normalizedDate.after(normalizedMax)
        val isNotPast = !_config.disablePastDates || !normalizedDate.before(todayNormalized)

        return isAfterMin && isBeforeMax && isNotPast
    }

    fun setDateLabels(labels: List<DateLabel>) {
        customDateLabels.clear()
        labels.forEach { label ->
            customDateLabels[label.date.normalizeDate()] =
                DateLabel(label.date.normalizeDate(), label.text)
        }
        invalidate()
    }

    fun clearDateLabels() {
        customDateLabels.clear()
        invalidate()
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

data class DateLabel(val date: Date, val text: String)
