package com.kokoromi.ui.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.model.ExperimentWithLogs
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun ExperimentCard(
    experimentWithLogs: ExperimentWithLogs,
    onCheckIn: () -> Unit,
    onSkip: () -> Unit,
    onTap: () -> Unit,
    onResume: () -> Unit,
    canResume: Boolean,
    modifier: Modifier = Modifier,
) {
    val experiment = experimentWithLogs.experiment
    val todayLog = experimentWithLogs.todayLog
    val isPaused = experiment.status == ExperimentStatus.PAUSED
    val today = remember { LocalDate.now() }
    val dayNumber = remember(experiment.startDate) {
        ChronoUnit.DAYS.between(experiment.startDate, today).toInt() + 1
    }
    val totalDays = remember(experiment.startDate, experiment.endDate) {
        ChronoUnit.DAYS.between(experiment.startDate, experiment.endDate).toInt() + 1
    }
    val action = experiment.action.trim().trimEnd('.', ',', '!', '?', ';', ':')
        .replaceFirstChar { it.uppercase() }
    val cardDescription = "$action, day $dayNumber of $totalDays${if (isPaused) ", paused" else ""}"

    Card(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Tappable header — navigates to detail
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onTap)
                    .semantics { contentDescription = cardDescription },
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = action,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = "Day $dayNumber of $totalDays",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (isPaused) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = MaterialTheme.shapes.small,
                        ) {
                            Text(
                                text = "PAUSED",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            )
                        }
                    }
                }

                StreakDisplay(
                    startDate = experiment.startDate,
                    endDate = experiment.endDate,
                    logs = experimentWithLogs.logs,
                )
            }

            // Action buttons — independent of card tap
            if (isPaused) {
                OutlinedButton(
                    onClick = onResume,
                    enabled = canResume,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .semantics { contentDescription = if (canResume) "Resume experiment" else "Resume unavailable: both slots are full" },
                ) { Text(if (canResume) "Resume" else "Resume (slots full)") }
            } else if (todayLog != null) {
                OutlinedButton(
                    onClick = onCheckIn,
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .semantics { contentDescription = "Edit today's log for $action" },
                ) { Text("Edit Log") }
            } else {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedButton(
                        onClick = onCheckIn,
                        modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
                    ) { Text("✓ YES") }
                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
                    ) { Text("✗ SKIP") }
                }
            }
        }
    }
}
