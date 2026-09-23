package com.example.thecorner.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.thecorner.R
import com.example.thecorner.databinding.ItemHistoryMonthBinding
import com.example.thecorner.databinding.ItemHistorySessionBinding
import com.example.thecorner.model.Workout
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

internal sealed interface HistoryRow {
    data class Month(val month: YearMonth) : HistoryRow
    data class Session(val workout: Workout) : HistoryRow
}

internal class HistoryAdapter(private val onSessionClick: (Long) -> Unit) :
    ListAdapter<HistoryRow, RecyclerView.ViewHolder>(Diff) {

    init { stateRestorationPolicy = StateRestorationPolicy.PREVENT_WHEN_EMPTY }

    fun submitMonths(months: List<HistoryMonth>) = submitList(months.flatMap { group ->
        listOf(HistoryRow.Month(group.month)) + group.sessions.map { HistoryRow.Session(it) }
    })

    override fun getItemViewType(position: Int): Int = if (getItem(position) is HistoryRow.Month) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) MonthHolder(ItemHistoryMonthBinding.inflate(inflater, parent, false))
        else SessionHolder(ItemHistorySessionBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = getItem(position)) {
            is HistoryRow.Month -> (holder as MonthHolder).binding.month.text = row.month.format(
                DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
            ).uppercase(Locale.getDefault())
            is HistoryRow.Session -> (holder as SessionHolder).binding.apply {
                val workout = row.workout
                val context = root.context
                title.text = workout.displayTitle(context)
                date.text = context.getString(R.string.history_date_time, sessionDate(workout.date), sessionTime(workout.date))
                summary.text = context.getString(R.string.history_session_summary,
                    context.resources.getQuantityString(R.plurals.history_rounds, workout.totalRounds, workout.totalRounds),
                    sessionDuration(context, workout.duration))
                artwork.setImageResource(workout.workoutType.historyImage())
                root.setOnClickListener { onSessionClick(workout.id) }
            }
        }
    }

    private class MonthHolder(val binding: ItemHistoryMonthBinding) : RecyclerView.ViewHolder(binding.root) {
        init { ViewCompat.setAccessibilityHeading(binding.month, true) }
    }
    private class SessionHolder(val binding: ItemHistorySessionBinding) : RecyclerView.ViewHolder(binding.root)

    private object Diff : DiffUtil.ItemCallback<HistoryRow>() {
        override fun areItemsTheSame(old: HistoryRow, new: HistoryRow): Boolean = when {
            old is HistoryRow.Month && new is HistoryRow.Month -> old.month == new.month
            old is HistoryRow.Session && new is HistoryRow.Session -> old.workout.id == new.workout.id
            else -> false
        }
        override fun areContentsTheSame(old: HistoryRow, new: HistoryRow) = old == new
    }
}
