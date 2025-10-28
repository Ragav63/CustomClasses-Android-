package com.example.customclass.calendar

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.customclass.databinding.ItemCalendarMonthBinding
import java.util.Calendar
import java.util.Date
import kotlin.apply

class MonthAdapter(
    private val months: List<Calendar>,
    private val onDateRangeSelectedListener: OnCanvasDateRangeSelectedListener,
    private val selectedDayColorResId: Int,
    private val rangeBackgroundColorResId: Int,
    private val dayTextColorResId: Int,
    private val dateLabelColorResId: Int
) : RecyclerView.Adapter<MonthAdapter.MonthViewHolder>() {

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
            selectedDayColorResId,
            rangeBackgroundColorResId,
            dayTextColorResId,
            dateLabelColorResId
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
            selectedDayColorResId: Int,
            rangeBackgroundColorResId: Int,
            dayTextColorResId: Int,
            dateLabelColorResId: Int
        ) {
            binding.calendarCanvasView.displayedMonth = monthCalendar
            binding.calendarCanvasView.selectedStartDate = startDate
            binding.calendarCanvasView.selectedEndDate = endDate
            //binding.calendarCanvasView.invalidate()

            // It's crucial that each CalendarCanvasView instance reports back to the central listener
            // which in turn updates the adapter and other views.
            binding.calendarCanvasView.onDateRangeSelectedListener = listener

            binding.calendarCanvasView.apply {
                // Set fixed colors for selection and range background
                setSelectedDayColor(selectedDayColorResId)
                setRangeBackgroundColor(rangeBackgroundColorResId)
                setDayTextColor(dayTextColorResId)
                setDateLabelColor(dateLabelColorResId)
            }


            // Apply the pre-calculated labels
            binding.calendarCanvasView.setDateLabels(labels)
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