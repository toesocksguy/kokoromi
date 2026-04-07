package com.kokoromi.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.usecase.ReflectionPromptState
import com.kokoromi.ui.home.components.ExperimentCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCreateExperiment: () -> Unit,
    onCheckIn: (experimentId: String, initialCompleted: Boolean) -> Unit,
    onNavigateToCompletion: (experimentId: String) -> Unit,
    onNavigateToReflection: (experimentId: String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val dateLabel = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d"))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Kokoromi")
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        when (val state = uiState) {
            is HomeUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            is HomeUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Something went wrong. Try restarting the app.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp),
                    )
                }
            }

            is HomeUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    state.completedExperiments.forEach { experiment ->
                        CompletionBanner(
                            experiment = experiment,
                            onClick = { onNavigateToCompletion(experiment.id) },
                        )
                    }

                    state.reflectionPrompts.forEach { prompt ->
                        ReflectionPromptCard(
                            prompt = prompt,
                            onClick = { onNavigateToReflection(prompt.experimentId) },
                        )
                    }

                    if (state.experiments.isEmpty() && state.completedExperiments.isEmpty()) {
                        EmptyState(
                            onCreateExperiment = onCreateExperiment,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        state.experiments.forEach { experimentWithLogs ->
                            ExperimentCard(
                                experimentWithLogs = experimentWithLogs,
                                onCheckIn = { onCheckIn(experimentWithLogs.experiment.id, true) },
                                onSkip = { onCheckIn(experimentWithLogs.experiment.id, false) },
                            )
                        }

                        if (state.canCreateExperiment) {
                            OutlinedButton(
                                onClick = onCreateExperiment,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .semantics { contentDescription = "New experiment" },
                            ) {
                                Text("+ New Experiment")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletionBanner(
    experiment: Experiment,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val action = experiment.action.trim().trimEnd('.', ',', '!', '?', ';', ':')
        .replaceFirstChar { it.uppercase() }

    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .semantics { contentDescription = "$action just ended. Tap to review." },
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "🎉 $action just ended!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "Review →",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun EmptyState(
    onCreateExperiment: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "🔬",
            style = MaterialTheme.typography.displayLarge,
            modifier = Modifier.semantics { contentDescription = "" },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No active experiments",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start by running a small test on something you're curious about.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onCreateExperiment,
            modifier = Modifier.semantics { contentDescription = "Create experiment" },
        ) {
            Text("+ Create Experiment")
        }
    }
}
