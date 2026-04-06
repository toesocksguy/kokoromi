package com.kokoromi.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kokoromi.domain.model.DailyLog
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StreakDisplay(
    startDate: LocalDate,
    endDate: LocalDate,
    logs: List<DailyLog>,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    val logsByDate = logs.associateBy { it.date }
    val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
    val pastDays = ChronoUnit.DAYS.between(startDate, minOf(today, endDate)).toInt() + 1
    val completedCount = logs.count { it.completed }

    FlowRow(
        modifier = modifier.semantics {
            contentDescription = "$completedCount completed out of $pastDays days so far"
        },
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        repeat(totalDays) { dayIndex ->
            val date = startDate.plusDays(dayIndex.toLong())
            val isFuture = date.isAfter(today)
            val isToday = date == today
            val log = logsByDate[date]

            val color = when {
                isToday && log == null -> MaterialTheme.colorScheme.primary
                log?.completed == true -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outlineVariant
            }
            val alpha = when {
                isFuture -> 0.4f
                log?.completed == true -> 0.6f
                else -> 1f
            }

            Box(
                modifier = Modifier
                    .size(14.dp)
                    .alpha(alpha)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color),
            )
        }
    }
}
