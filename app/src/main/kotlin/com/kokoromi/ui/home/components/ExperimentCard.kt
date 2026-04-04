package com.kokoromi.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.kokoromi.domain.model.Experiment
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Composable
fun ExperimentCard(
    experiment: Experiment,
    onCheckIn: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = remember { LocalDate.now() }
    val dayNumber = remember(experiment.startDate) {
        ChronoUnit.DAYS.between(experiment.startDate, today).toInt() + 1
    }
    val totalDays = remember(experiment.startDate, experiment.endDate) {
        ChronoUnit.DAYS.between(experiment.startDate, experiment.endDate).toInt() + 1
    }
    val cardDescription = "${experiment.hypothesis}, day $dayNumber of $totalDays"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = cardDescription },
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = experiment.hypothesis,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Day $dayNumber of $totalDays",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Button(
                    onClick = onCheckIn,
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize(),
                ) {
                    Text("✓ YES")
                }
                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize(),
                ) {
                    Text("✗ SKIP")
                }
            }
        }
    }
}
