package com.example.customclass.hotelCalendar

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.customclass.hotelCalendar.OnCanvasDateRangeSelectedListener
import com.example.customclass.databinding.ItemCalendarMonthBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.apply

class CalendarMonthAdapter(
    private val months: List<Calendar>,
    private val config: CalendarConfig,
    private val onDateRangeSelectedListener: OnCanvasDateRangeSelectedListener,
    private val showWeekdaysInsideMonth: Boolean
) : RecyclerView.Adapter<CalendarMonthAdapter.MonthViewHolder>()
 {

    private var selectedStartDate: Date? = null
    private var selectedEndDate: Date? = null

    private var monthlyDateLabels: Map<Calendar, List<DateLabel>> = emptyMap()

    // This method will be called by the BottomSheet to update the selection across all views
    fun updateSelectedDates(startDate: Date?, endDate: Date?) {
        this.selectedStartDate = startDate
        this.selectedEndDate = endDate
        notifyDataSetChanged() // Invalidate all items to redraw with new selection
    }

    // NEW: Method to update the pre-calculated labels from the Fragment
    fun updateMonthlyLabels(labels: Map<Calendar, List<DateLabel>>) {
        this.monthlyDateLabels = labels
        notifyDataSetChanged() // Redraw all calendar views with new labels
    }



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val binding = ItemCalendarMonthBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MonthViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val monthCalendar = months[position]
        val labelsForMonth = monthlyDateLabels[monthCalendar.normalizeMonth()] ?: emptyList()
        holder.bind(
            monthCalendar,
            selectedStartDate,
            selectedEndDate,
            onDateRangeSelectedListener,
            labelsForMonth,
            config,
            showWeekdaysInsideMonth
        )
    }

    override fun getItemCount(): Int = months.size

    class MonthViewHolder(private val binding: ItemCalendarMonthBinding) : RecyclerView.ViewHolder(binding.root) {

        // Helper to normalize a date to start of day, essential for consistent map keys
        private fun Date.normalizeDate(): Date {
            val cal = Calendar.getInstance().apply { time = this@normalizeDate }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.time
        }

        fun bind(
            monthCalendar: Calendar,
            startDate: Date?,
            endDate: Date?,
            listener: OnCanvasDateRangeSelectedListener,
            labels: List<DateLabel>,
            config: CalendarConfig,
            showWeekdaysInsideMonth: Boolean
        ) {
            binding.calendarCanvasView.apply {
                displayedMonth = monthCalendar
                selectedStartDate = startDate
                selectedEndDate = endDate
                onDateRangeSelectedListener = listener
                showWeekdayLabels = showWeekdaysInsideMonth

                applyConfig(config)
                setDateLabels(labels)
            }
        }
    }


    private fun Calendar.normalizeMonth(): Calendar {
        val normalized = this.clone() as Calendar
        normalized.set(Calendar.DAY_OF_MONTH, 1)
        normalized.set(Calendar.HOUR_OF_DAY, 0)
        normalized.set(Calendar.MINUTE, 0)
        normalized.set(Calendar.SECOND, 0)
        normalized.set(Calendar.MILLISECOND, 0)
        return normalized
    }
}