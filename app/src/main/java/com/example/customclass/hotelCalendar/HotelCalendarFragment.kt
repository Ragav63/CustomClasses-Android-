package com.example.customclass.hotelCalendar

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.customclass.R
import com.example.customclass.databinding.FragmentHotelCalendarBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class HotelCalendarFragment : Fragment(), OnCanvasDateRangeSelectedListener {

    private var _binding: FragmentHotelCalendarBinding? = null
    private val binding get() = _binding!!
    private lateinit var monthAdapter: CalendarMonthAdapter
    private val monthsToDisplay = mutableListOf<Calendar>()

    private var selectedStartDate: Date? = null
    private var selectedEndDate: Date? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHotelCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup min and max dates
        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val maxSelectableCalendar = Calendar.getInstance().apply {
            time = todayCalendar.time
            add(Calendar.MONTH, 14)
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }

        // Populate months to display
        val calendarIterator = Calendar.getInstance().apply { time = todayCalendar.time }
        while (calendarIterator.time.before(maxSelectableCalendar.time)) {
            calendarIterator.set(Calendar.DAY_OF_MONTH, 1)
            monthsToDisplay.add(calendarIterator.clone() as Calendar)
            calendarIterator.add(Calendar.MONTH, 1)
        }

        // Create comprehensive configuration
        val calendarConfig = createCalendarConfig(todayCalendar.time, maxSelectableCalendar.time)

        // Initialize adapter with configuration
        monthAdapter = CalendarMonthAdapter(
            months = monthsToDisplay,
            config = calendarConfig,
            onDateRangeSelectedListener = this,
            showWeekdaysInsideMonth = false
        ).apply {
            updateSelectedDates(selectedStartDate, selectedEndDate)
        }

        binding.monthsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.monthsRecyclerView.adapter = monthAdapter

        setupButtons()
    }

    private fun createCalendarConfig(minDate: Date, maxDate: Date): CalendarConfig {
        return CalendarConfig(
            // Weekday styling
            weekdayFormat = WeekdayFormat.THREE_LETTER,
            weekdayAppearance = TextAppearance(
                textSizeSP = 12f,
                textColor = Color.GRAY,
                isBold = false,
                isItalic = false
            ),

            // Month header styling
            monthLabelAlignment = MonthLabelAlignment.START,
            monthHeaderAppearance = TextAppearance(
                textSizeSP = 14f,
                textColor = Color.parseColor("#2C3E50"),
                isBold = true,
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            ),

            // Date text styling
            dateTextAppearance = TextAppearance(
                textSizeSP = 14f,
                textColor = Color.BLACK,
                isBold = true
            ),

            // Date label styling
            dateLabelAppearance = TextAppearance(
                textSizeSP = 12f,
                textColor = Color.parseColor("#7F8C8D"),
                isItalic = true
            ),

            // Selection colors
            selectedDayColor = Color.BLUE,
            selectedDayTextColor = Color.WHITE,
            rangeBackgroundColor = Color.parseColor("#E3F2FD"),
            rangeEdgeTextColor = Color.WHITE,
            rangeMiddleTextColor = Color.BLACK,
            todayColor = Color.parseColor("#E74C3C"),
            disabledDateColor = Color.parseColor("#BDC3C7"),

            // Layout
            showWeekdaysInsideMonth = true,
            cellHeightDP = 60f, // Larger cells
            dateLabelTopMarginDP = 4f,
            headerBottomMarginDP = 8f,

            // Shape
            selectedCircleScale = 1.0f,
            rangeBandHeightScale = 0.7f,

            // Restrictions
            minDate = minDate,
            maxDate = maxDate,
            disablePastDates = true
        )
    }

    private fun setupButtons() {
        // Example: Reset button with custom styling
        binding.tvClear.setOnClickListener {
            selectedStartDate = null
            selectedEndDate = null
            monthAdapter.updateSelectedDates(null, null)
            updateStartEndDate(null, null)
            updateApplyButtonState()
        }

        // Example: Apply button
        binding.btnApply.setOnClickListener {
            if (selectedStartDate != null && selectedEndDate != null) {
                // Handle date range application
                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val message = "Selected: ${dateFormat.format(selectedStartDate!!)} - " +
                        "${dateFormat.format(selectedEndDate!!)}"
                // Show confirmation or proceed
            }
        }
    }

    private fun updateStartEndDate(startDate: Date?, endDate: Date?) {
        val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
        binding.tvCheckInOut.text = when {
            startDate != null && endDate != null && startDate != endDate -> {
                "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
            }
            startDate != null && endDate == null -> {
                "${dateFormat.format(startDate)} - ${"Select check-out"}"
            }
            startDate != null && startDate == endDate -> {
                "${dateFormat.format(startDate)} (1 night)"
            }
            else -> {
                "Select start and end date"
            }
        }
    }

    private fun updateApplyButtonState() {
        val isEnabled = selectedStartDate != null && selectedEndDate != null
        binding.btnApply.isEnabled = isEnabled
        binding.btnApply.alpha = if (isEnabled) 1f else 0.2f
    }

    // Implement OnCanvasDateRangeSelectedListener methods
    override fun onDateRangeSelected(startDate: Date, endDate: Date) {
        selectedStartDate = startDate
        selectedEndDate = endDate
        updateStartEndDate(startDate, endDate)
        updateApplyButtonState()
        monthAdapter.updateSelectedDates(startDate, endDate)
    }

    override fun onDateSelectionChanged(startDate: Date?, endDate: Date?) {
        selectedStartDate = startDate
        selectedEndDate = endDate
        updateStartEndDate(startDate, endDate)
        updateApplyButtonState()
        monthAdapter.updateSelectedDates(startDate, endDate)
    }

    override fun onInvalidDateSelected(date: Date) {
        binding.tvCheckInOut.text = "Selected date is unavailable"
    }

    override fun onMonthDisplayed(month: Calendar) {
        // Optional: Scroll to month or update header
    }

    override fun onDateSelectionCancelled() {
        selectedStartDate = null
        selectedEndDate = null
        updateStartEndDate(null, null)
        updateApplyButtonState()
        monthAdapter.updateSelectedDates(null, null)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}