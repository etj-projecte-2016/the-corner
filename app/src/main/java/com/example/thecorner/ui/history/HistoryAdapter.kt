package com.example.thecorner.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.thecorner.R
import com.example.thecorner.databinding.ItemHistoryMonthBinding
import com.example.thecorner.databinding.ItemHistoryDayBinding
import com.example.thecorner.databinding.ItemHistorySessionBinding
import com.example.thecorner.model.estimatedCalories
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToLong

internal sealed interface HistoryRow {
    data class Month(val month: YearMonth) : HistoryRow
    data class Day(val day: HistoryDay, val isNewest: Boolean) : HistoryRow
}

internal class HistoryAdapter(private val onSessionClick: (Long) -> Unit) :
    ListAdapter<HistoryRow, RecyclerView.ViewHolder>(Diff) {

    init { stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY }

    fun submitMonths(months: List<HistoryMonth>) {
        val newestDate = months.firstOrNull()?.days?.firstOrNull()?.date
        submitList(months.flatMap { group ->
            listOf(HistoryRow.Month(group.month)) + group.days.map { HistoryRow.Day(it, it.date == newestDate) }
        })
    }

    private val artworkTreatment = ColorMatrixColorFilter(ColorMatrix().apply {
        setSaturation(0.15f)
        postConcat(ColorMatrix().apply { setScale(0.85f, 0.85f, 0.85f, 1f) })
    })

    override fun getItemViewType(position: Int): Int = if (getItem(position) is HistoryRow.Month) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) MonthHolder(ItemHistoryMonthBinding.inflate(inflater, parent, false))
        else DayHolder(ItemHistoryDayBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = getItem(position)) {
            is HistoryRow.Month -> (holder as MonthHolder).binding.month.text = row.month.format(
                DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
            ).uppercase(Locale.getDefault())
            is HistoryRow.Day -> (holder as DayHolder).bind(row)
        }
    }

    private class MonthHolder(val binding: ItemHistoryMonthBinding) : RecyclerView.ViewHolder(binding.root) {
        init { ViewCompat.setAccessibilityHeading(binding.month, true) }
    }
    private inner class DayHolder(private val binding: ItemHistoryDayBinding) : RecyclerView.ViewHolder(binding.root) {
        private val cards = mutableListOf<ItemHistorySessionBinding>()

        init { ViewCompat.setAccessibilityHeading(binding.dayDate, true) }

        fun bind(row: HistoryRow.Day) {
            val context = binding.root.context
            val locale = Locale.getDefault()
            binding.weekday.text = row.day.date.format(DateTimeFormatter.ofPattern("EEE", locale)).uppercase(locale)
            binding.dayNumber.text = row.day.date.format(DateTimeFormatter.ofPattern("dd", locale))
            binding.monthYear.text = row.day.date.format(DateTimeFormatter.ofPattern("MMM yyyy", locale)).uppercase(locale)
            binding.daySummary.text = row.day.summary.dayCaption(context)
            binding.dot.setBackgroundResource(if (row.isNewest) R.drawable.bg_day_active else R.drawable.bg_day_inactive)

            // Recycle the day as a group so its date is shown once, alongside all of its cards.
            while (cards.size > row.day.sessions.size) {
                binding.cards.removeView(cards.removeAt(cards.lastIndex).root)
            }
            while (cards.size < row.day.sessions.size) {
                val card = ItemHistorySessionBinding.inflate(LayoutInflater.from(context), binding.cards, false)
                binding.cards.addView(card.root)
                cards.add(card)
            }
            row.day.sessions.forEachIndexed { index, workout ->
                cards[index].apply {
                    title.text = workout.displayTitle(context)
                    // date is completion time; duration excludes pauses, so start time cannot be inferred.
                    date.text = context.getString(R.string.history_ended_at, sessionTime(workout.date))
                    date.metricIcon(R.drawable.ic_timer, R.color.corner_text_secondary)
                    rounds.text = context.resources.getQuantityString(R.plurals.history_rounds, workout.totalRounds, workout.totalRounds)
                    rounds.metricIcon(R.drawable.ic_target, R.color.corner_red)
                    duration.text = historyCompactDuration(context, workout.duration.toLong())
                    duration.metricIcon(R.drawable.ic_timer, R.color.corner_text_secondary)
                    val calories = workout.estimatedCalories()
                    caloriesEstimate.isVisible = calories != null
                    caloriesEstimate.text = calories?.let { context.getString(R.string.history_kcal_compact, it.roundToLong()) }
                    caloriesEstimate.metricIcon(R.drawable.ic_fire, R.color.corner_red)
                    artwork.setImageResource(workout.workoutType.historyCardImage())
                    artwork.colorFilter = artworkTreatment
                    root.setOnClickListener { onSessionClick(workout.id) }
                }
            }
        }
    }

    private fun TextView.metricIcon(drawableRes: Int, tintRes: Int) {
        val size = (11 * resources.displayMetrics.density).toInt()
        val icon = ContextCompat.getDrawable(context, drawableRes)?.mutate()?.apply {
            setBounds(0, 0, size, size)
            setTint(ContextCompat.getColor(context, tintRes))
        }
        setCompoundDrawablesRelative(icon, null, null, null)
        compoundDrawablePadding = (3 * resources.displayMetrics.density).toInt()
    }

    private object Diff : DiffUtil.ItemCallback<HistoryRow>() {
        override fun areItemsTheSame(old: HistoryRow, new: HistoryRow): Boolean = when {
            old is HistoryRow.Month && new is HistoryRow.Month -> old.month == new.month
            old is HistoryRow.Day && new is HistoryRow.Day -> old.day.date == new.day.date
            else -> false
        }
        override fun areContentsTheSame(old: HistoryRow, new: HistoryRow) = old == new
    }
}
