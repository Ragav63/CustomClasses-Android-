package com.example.customclass.calendar

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.customclass.R
import com.example.customclass.databinding.FragmentCalenderViewBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class CalenderViewFragment : Fragment() {

    private var _binding : FragmentCalenderViewBinding?=null
    private val binding get() = _binding!!
    private lateinit var monthAdapter: MonthAdapter
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
        _binding = FragmentCalenderViewBinding.inflate(layoutInflater, container, false)
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

        // Initialize RecyclerView and Adapter
        // Initialize RecyclerView and Adapter
        monthAdapter = MonthAdapter(
            monthsToDisplay,
            object : OnCanvasDateRangeSelectedListener {
                override fun onDateRangeSelected(startDate: Date, endDate: Date) {
                    selectedStartDate = startDate
                    selectedEndDate = endDate
                    updateStartEndDate(startDate, endDate)
                    updateApplyButtonState()
                    // Update all calendar views to reflect the new selection
                    monthAdapter.updateSelectedDates(startDate, endDate)
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
                    // This callback is less critical now as months are pre-populated by the BottomSheet.
                    // You can leave it empty or add logging/debugging here if needed.
                }

                override fun onInvalidDateSelected(date: Date) {
                }

                override fun onDateSelectionCancelled() {
                    // Not directly triggered by Canvas view, but kept for interface consistency
                }
            },
            selectedDayColor,
            rangeBackgroundColor,
            dayTextColor,
            dateLabelColor
        ).apply {
            // Set min/max dates for all individual CalendarCanvasViews through the adapter's listener
            // (You might want to pass min/max dates directly to each CalendarCanvasView within MonthViewHolder's bind method)
            updateSelectedDates(selectedStartDate, selectedEndDate) // IMPORTANT: Initial update
        }

        binding.monthsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.monthsRecyclerView.adapter = monthAdapter

        // List of possible random labels
        val randomLabels = listOf(
            "Sale", "Deal", "Offer",
            "Event", "Meeting", "Closed",
            "Busy", "Open", "Special",
            "Limited", "Early Bird", "Last Chance"
        )

        // List of possible emoji labels
        val emojiLabels = listOf("🎉", "🔥", "⭐", "💯", "👍", "👑", "✨", "💎", "🏆", "🚀")


        cachedMonthlyDateLabels = generateAllMonthlyLabels(emojiLabels)
        monthAdapter.updateMonthlyLabels(cachedMonthlyDateLabels)
    }

    private fun generateAllMonthlyLabels(label: List<String>): Map<Calendar, List<DateLabel>> {
        val allLabels = mutableMapOf<Calendar, MutableList<DateLabel>>()

        // Normalize initial date for consistent price calculation base
        val firstSelectableDay = minSelectableDate.normalizeDate()

        val calendarIterator = Calendar.getInstance().apply { time = minSelectableDate }
        while (calendarIterator.time.before(maxSelectableDate)) {
            // This line now correctly calls the top-level extension function
            val monthStartNormalized = calendarIterator.normalizeMonth()
            val labelsForMonth = mutableListOf<DateLabel>()

            val tempCalendar = calendarIterator.clone() as Calendar
            tempCalendar.set(Calendar.DAY_OF_MONTH, 1)

            val daysInMonth = tempCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

            for (dayOfMonth in 1..daysInMonth) {
                val dayCal = tempCalendar.clone() as Calendar
                dayCal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                val currentDate = dayCal.time.normalizeDate()

                val daysSinceFirstSelectable = ((currentDate.time - firstSelectableDay.time) / (1000 * 60 * 60 * 24)).toInt()


                val labelColor = this?.let { ContextCompat.getColor(requireContext(), R.color.black) } ?: Color.GRAY

                val randomEmoji = label.random()

                labelsForMonth.add(DateLabel(currentDate, randomEmoji))
            }
            allLabels[monthStartNormalized] = labelsForMonth
            calendarIterator.add(Calendar.MONTH, 1)
        }
        return allLabels
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
                "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}" // Single day selection
            }
            else -> {
                "Select start and end date"
            }
        }
    }

    private fun updateApplyButtonState() {
        // Enable button only if both start and end dates are selected AND they are not the same date
        val isEnabled = selectedStartDate != null && selectedEndDate != null && selectedStartDate != selectedEndDate
        binding.btnApply.isEnabled = isEnabled
        binding.btnApply.alpha = if (isEnabled) 1f else 0.2f
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}