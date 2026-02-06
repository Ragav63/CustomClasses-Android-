package com.example.customclass.hotelCalendar

import android.graphics.Color
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


class HotelCalendarFragment : Fragment(),
    OnCanvasDateRangeSelectedListener {

    private var _binding : FragmentHotelCalendarBinding?=null

    private val binding get() = _binding!!
    private lateinit var monthAdapter: CalendarMonthAdapter
    private val monthsToDisplay = mutableListOf<Calendar>()

    // Hold the selected dates at the BottomSheet level
    private var selectedStartDate: Date? = null
    private var selectedEndDate: Date? = null

    // Min and max selectable dates
    private lateinit var minSelectableDate: Date
    private lateinit var maxSelectableDate: Date

    private var cachedMonthlyDateLabels: Map<Calendar, List<DateLabel>> = emptyMap()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHotelCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val todayCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        minSelectableDate = todayCalendar.time

        val maxSelectableCalendar = Calendar.getInstance().apply {
            time = todayCalendar.time
            add(Calendar.MONTH, 14) // Display 15 months including current
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }
        maxSelectableDate = maxSelectableCalendar.time

        // Populate months to display
        val calendarIterator = Calendar.getInstance().apply { time = minSelectableDate }
        while (calendarIterator.time.before(maxSelectableDate)) {
            calendarIterator.set(Calendar.DAY_OF_MONTH, 1) // Ensure it's the first of the month
            monthsToDisplay.add(calendarIterator.clone() as Calendar)
            calendarIterator.add(Calendar.MONTH, 1)
        }


        val selectedDayColor = com.google.android.material.R.color.design_default_color_primary
        val rangeBackgroundColor = androidx.cardview.R.color.cardview_dark_background
        val dayTextColor = R.color.black
        val dateLabelColor = R.color.black
        val rangeMiddleColor = R.color.black
        val rangeEdgeColor = R.color.white
        var showWeekdays: Boolean = false



        // Initialize RecyclerView and Adapter
        monthAdapter = CalendarMonthAdapter(
            months = monthsToDisplay,
            config = CalendarConfig(
                weekdayFormat = WeekdayFormat.THREE_LETTER,
                monthLabelAlignment = MonthLabelAlignment.START,

                dayTextColor = Color.BLACK,
                dateLabelColor = Color.GRAY,
                headerTextColor = Color.DKGRAY,
                weekdayTextColor = Color.GRAY,

                selectedDayColor = Color.parseColor("#6200EE"),
                rangeBackgroundColor = Color.parseColor("#E3F2FD"),
                rangeEdgeTextColor = Color.WHITE,
                rangeMiddleTextColor = Color.WHITE
            ),
            onDateRangeSelectedListener = this,
            showWeekdaysInsideMonth = true
        )
            .apply {
            // Set min/max dates for all individual CalendarCanvasViews through the adapter's listener
            // (You might want to pass min/max dates directly to each CalendarCanvasView within MonthViewHolder's bind method)
            updateSelectedDates(selectedStartDate, selectedEndDate) // IMPORTANT: Initial update
        }


        binding.monthsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.monthsRecyclerView.adapter = monthAdapter


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



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    override fun onDateRangeSelected(startDate: Date, endDate: Date) {
        selectedStartDate = startDate
        selectedEndDate = endDate
        updateStartEndDate(startDate, endDate)
        updateApplyButtonState()
        // Update all calendar views to reflect the new selection
        monthAdapter.updateSelectedDates(startDate, endDate)
    }

    override fun onDateSelectionCancelled() {

    }

    override fun onInvalidDateSelected(date: Date) {
        binding.tvCheckInOut.text = "Selected date is unavailable"
    }


    override fun onDateSelectionChanged(startDate: Date?, endDate: Date?) {
        selectedStartDate = startDate
        selectedEndDate = endDate
        updateStartEndDate(startDate, endDate)
        updateApplyButtonState()
        // Update all calendar views to reflect the new selection
        monthAdapter.updateSelectedDates(startDate, endDate)
    }

    override fun onMonthDisplayed(month: Calendar) {
        // No-op for now
    }

}