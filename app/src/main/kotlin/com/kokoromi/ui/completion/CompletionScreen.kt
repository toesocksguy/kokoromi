package com.kokoromi.ui.completion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kokoromi.domain.model.CompletionStats
import com.kokoromi.domain.model.Experiment
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletionScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToCreate: () -> Unit,
    viewModel: CompletionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val displayState by viewModel.displayState.collectAsStateWithLifecycle()
    val form by viewModel.form.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showPauseConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is CompletionUiState.Persisted -> onNavigateToHome()
            is CompletionUiState.Pivoted -> onNavigateToCreate()
            is CompletionUiState.Paused -> onNavigateToHome()
            is CompletionUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as CompletionUiState.Error).message)
                viewModel.onErrorDismissed()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Experiment Complete") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when (uiState) {
            is CompletionUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            else -> {
                val display = displayState
                if (display != null) {
                    CompletionContent(
                        experiment = display.experiment,
                        stats = display.stats,
                        form = form,
                        isSubmitting = uiState is CompletionUiState.Loading,
                        onLearningsChange = viewModel::onLearningsChange,
                        onExternalSignalsChange = viewModel::onExternalSignalsChange,
                        onInternalSignalsChange = viewModel::onInternalSignalsChange,
                        onPersist = viewModel::onPersist,
                        onPivot = viewModel::onPivot,
                        onPause = { showPauseConfirm = true },
                        modifier = Modifier.padding(innerPadding),
                    )

                    if (showPauseConfirm) {
                        AlertDialog(
                            onDismissRequest = { showPauseConfirm = false },
                            properties = DialogProperties(dismissOnClickOutside = false),
                            title = { Text("Set this aside?") },
                            text = { Text("You can resume the experiment from the Archive when you're ready.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showPauseConfirm = false
                                    viewModel.onPause()
                                }) { Text("Pause") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showPauseConfirm = false }) { Text("Cancel") }
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletionContent(
    experiment: Experiment,
    stats: CompletionStats,
    form: CompletionFormState,
    isSubmitting: Boolean,
    onLearningsChange: (String) -> Unit,
    onExternalSignalsChange: (String) -> Unit,
    onInternalSignalsChange: (String) -> Unit,
    onPersist: () -> Unit,
    onPivot: () -> Unit,
    onPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Header
        Text(
            text = "Your experiment just ended!",
            style = MaterialTheme.typography.headlineSmall,
        )

        // Summary card
        SummaryCard(experiment = experiment, stats = stats)

        HorizontalDivider()

        // What did you learn?
        OutlinedTextField(
            value = form.learnings,
            onValueChange = onLearningsChange,
            label = { Text("What did you learn?") },
            placeholder = { Text("Reflect on how it went…") },
            minLines = 3,
            maxLines = 6,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.outline,
                focusedPlaceholderColor = MaterialTheme.colorScheme.outline,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        HorizontalDivider()

        // Steering sheet
        Text(
            text = "What's telling you what to do next?",
            style = MaterialTheme.typography.titleSmall,
        )

        OutlinedTextField(
            value = form.externalSignals,
            onValueChange = onExternalSignalsChange,
            label = { Text("External signals") },
            placeholder = { Text("Facts, context, practical constraints…") },
            minLines = 2,
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.outline,
                focusedPlaceholderColor = MaterialTheme.colorScheme.outline,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = form.internalSignals,
            onValueChange = onInternalSignalsChange,
            label = { Text("Internal signals") },
            placeholder = { Text("Feelings, motivations, energy…") },
            minLines = 2,
            maxLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.outline,
                focusedPlaceholderColor = MaterialTheme.colorScheme.outline,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        HorizontalDivider()

        // Decision buttons
        Text(
            text = "What's next?",
            style = MaterialTheme.typography.titleSmall,
        )

        DecisionButtons(
            enabled = !isSubmitting,
            onPersist = onPersist,
            onPivot = onPivot,
            onPause = onPause,
        )
    }
}

@Composable
private fun SummaryCard(
    experiment: Experiment,
    stats: CompletionStats,
    modifier: Modifier = Modifier,
) {
    val dateFmt = DateTimeFormatter.ofPattern("MMM d")
    val dateRange = "${experiment.startDate.format(dateFmt)} – ${experiment.endDate.format(dateFmt)}"
    val completionPct = (stats.completionRate * 100).toInt()
    val moodText = stats.avgMoodAfter?.let { "Avg mood: %.1f / 5".format(it) }

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = experiment.action,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = dateRange,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${stats.daysCompleted} of ${stats.totalDays} days completed ($completionPct%)",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (moodText != null) {
                Text(
                    text = moodText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (experiment.hypothesis.isNotBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Text(
                    text = "Hypothesis",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = experiment.hypothesis,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun DecisionButtons(
    enabled: Boolean,
    onPersist: () -> Unit,
    onPivot: () -> Unit,
    onPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Button(
            onClick = onPersist,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "Persist: start a new round of this experiment" },
        ) {
            Text("PERSIST")
        }
        OutlinedButton(
            onClick = onPivot,
            enabled = enabled,
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "Pivot: adjust or try a new approach" },
        ) {
            Text("PIVOT")
        }
        OutlinedButton(
            onClick = onPause,
            enabled = enabled,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier
                .weight(1f)
                .semantics { contentDescription = "Pause: set this aside for now" },
        ) {
            Text("PAUSE")
        }
    }
}
