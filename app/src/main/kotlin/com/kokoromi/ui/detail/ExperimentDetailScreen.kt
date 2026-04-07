package com.kokoromi.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kokoromi.domain.model.Completion
import com.kokoromi.domain.model.DailyLog
import com.kokoromi.domain.model.DecisionType
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.model.Reflection
import com.kokoromi.ui.home.components.StreakDisplay
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExperimentDetailScreen(
    onBack: () -> Unit,
    onNavigateToCompletion: (experimentId: String) -> Unit,
    viewModel: ExperimentDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigateToCompletion.collect { experimentId ->
            onNavigateToCompletion(experimentId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    when (val state = uiState) {
                        is ExperimentDetailUiState.Success -> Text(state.experiment.action.displayFormat())
                        else -> Text("Experiment")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
                actions = {
                    if (uiState is ExperimentDetailUiState.Success) {
                        val status = (uiState as ExperimentDetailUiState.Success).experiment.status
                        StatusBadge(status = status, modifier = Modifier.padding(end = 16.dp))
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val state = uiState) {
            is ExperimentDetailUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }
            }

            is ExperimentDetailUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize().padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            is ExperimentDetailUiState.Success -> {
                DetailContent(
                    state = state,
                    onPauseRequested = viewModel::onPauseRequested,
                    onArchiveRequested = viewModel::onArchiveRequested,
                    onEndEarlyRequested = viewModel::onEndEarlyRequested,
                    modifier = Modifier.padding(innerPadding),
                )

                if (state.showArchiveDialog) {
                    AlertDialog(
                        onDismissRequest = viewModel::onArchiveDismissed,
                        title = { Text("Archive experiment?") },
                        text = { Text("This experiment will be moved to your archive. Your data will be preserved.") },
                        confirmButton = {
                            TextButton(onClick = viewModel::onArchiveConfirmed) { Text("Archive") }
                        },
                        dismissButton = {
                            TextButton(onClick = viewModel::onArchiveDismissed) { Text("Cancel") }
                        },
                    )
                }

                if (state.showPauseDialog) {
                    AlertDialog(
                        onDismissRequest = viewModel::onPauseDismissed,
                        title = { Text("Pause experiment?") },
                        text = { Text("Your data will be saved. You can resume when you're ready, as long as a slot is available.") },
                        confirmButton = {
                            TextButton(onClick = viewModel::onPauseConfirmed) { Text("Pause") }
                        },
                        dismissButton = {
                            TextButton(onClick = viewModel::onPauseDismissed) { Text("Cancel") }
                        },
                    )
                }

                if (state.showEndEarlyDialog) {
                    val daysLeft = java.time.LocalDate.now().until(state.experiment.endDate, java.time.temporal.ChronoUnit.DAYS)
                    AlertDialog(
                        onDismissRequest = viewModel::onEndEarlyDismissed,
                        title = { Text("End experiment early?") },
                        text = { Text("You still have $daysLeft day${if (daysLeft == 1L) "" else "s"} left. You'll be taken to the completion screen to record your decision.") },
                        confirmButton = {
                            TextButton(onClick = viewModel::onEndEarlyConfirmed) { Text("End Experiment") }
                        },
                        dismissButton = {
                            TextButton(onClick = viewModel::onEndEarlyDismissed) { Text("Cancel") }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailContent(
    state: ExperimentDetailUiState.Success,
    onPauseRequested: () -> Unit,
    onArchiveRequested: () -> Unit,
    onEndEarlyRequested: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val experiment = state.experiment
    val dateFmt = DateTimeFormatter.ofPattern("MMM d")

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp),
    ) {
        // Hypothesis section
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = "What I tested",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = experiment.hypothesis,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (experiment.why != null) {
                    Text(
                        text = "Why: ${experiment.why}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

        // Progress section
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = "Progress",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${state.stats.daysCompleted} of ${state.stats.totalDays} days (${(state.stats.completionRate * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodyMedium,
                )
                StreakDisplay(
                    startDate = experiment.startDate,
                    endDate = experiment.endDate,
                    logs = state.logs,
                )
            }
        }

        item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

        // Daily logs header
        item {
            Text(
                text = "Daily Logs",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }

        if (state.logs.isEmpty()) {
            item {
                Text(
                    text = "No logs yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        } else {
            items(state.logs) { log ->
                LogItem(log = log, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            }
        }

        // Reflections section
        if (state.reflections.isNotEmpty()) {
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

            item {
                Text(
                    text = "Reflections",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }

            items(state.reflections) { reflection ->
                ReflectionItem(
                    reflection = reflection,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
        }

        // Completion section
        if (state.completion != null) {
            item { HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp)) }

            item {
                CompletionSection(
                    completion = state.completion,
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        // Action buttons by status
        when (experiment.status) {
            ExperimentStatus.ACTIVE -> item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedButton(
                        onClick = onPauseRequested,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "Pause experiment" },
                    ) { Text("Pause Experiment") }
                    OutlinedButton(
                        onClick = onEndEarlyRequested,
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { contentDescription = "End experiment early" },
                    ) { Text("End Experiment") }
                }
            }
            ExperimentStatus.PAUSED -> item {
                OutlinedButton(
                    onClick = onArchiveRequested,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .semantics { contentDescription = "Archive experiment" },
                ) { Text("Archive Experiment") }
            }
            else -> Unit
        }
    }
}

@Composable
private fun LogItem(
    log: DailyLog,
    modifier: Modifier = Modifier,
) {
    val dateFmt = DateTimeFormatter.ofPattern("MMM d")
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = if (log.completed) "✓" else "✗",
            style = MaterialTheme.typography.bodyMedium,
            color = if (log.completed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = log.date.format(dateFmt),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (log.moodAfter != null) {
                Text(
                    text = "★".repeat(log.moodAfter) + "☆".repeat(5 - log.moodAfter),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            if (log.notes != null) {
                Text(
                    text = "\"${log.notes}\"",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun ReflectionItem(
    reflection: Reflection,
    modifier: Modifier = Modifier,
) {
    val dateFmt = DateTimeFormatter.ofPattern("MMM d")
    Column(
        modifier = modifier.semantics(mergeDescendants = true) {},
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Week of ${reflection.reflectionDate.format(dateFmt)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (reflection.plus != null) {
            Text(text = "+ ${reflection.plus}", style = MaterialTheme.typography.bodySmall)
        }
        if (reflection.minus != null) {
            Text(text = "− ${reflection.minus}", style = MaterialTheme.typography.bodySmall)
        }
        if (reflection.next != null) {
            Text(text = "→ ${reflection.next}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun CompletionSection(
    completion: Completion,
    modifier: Modifier = Modifier,
) {
    val decisionLabel = when (completion.decision) {
        DecisionType.PERSIST -> "Persist"
        DecisionType.PIVOT -> "Pivot"
        DecisionType.PAUSE -> "Pause"
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Completion",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "Decision: $decisionLabel",
            style = MaterialTheme.typography.bodyMedium,
        )
        if (completion.learnings != null) {
            Text(
                text = completion.learnings,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatusBadge(
    status: ExperimentStatus,
    modifier: Modifier = Modifier,
) {
    val (label, containerColor, contentColor) = when (status) {
        ExperimentStatus.ACTIVE -> Triple(
            "ACTIVE",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
        )
        ExperimentStatus.PAUSED -> Triple(
            "PAUSED",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ExperimentStatus.COMPLETED -> Triple(
            "COMPLETED",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
        )
        ExperimentStatus.ARCHIVED -> Triple(
            "ARCHIVED",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small,
        modifier = modifier.semantics { contentDescription = "Status: $label" },
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        )
    }
}

private fun String.displayFormat() =
    trim().trimEnd('.', ',', '!', '?', ';', ':').replaceFirstChar { it.uppercase() }
