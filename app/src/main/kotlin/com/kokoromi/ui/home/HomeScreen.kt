package com.kokoromi.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kokoromi.ui.home.components.ExperimentCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCreateExperiment: () -> Unit,
    onCheckIn: (experimentId: String, initialCompleted: Boolean) -> Unit,
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
                            style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
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

            is HomeUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (state.experiments.isEmpty()) {
                        EmptyState(
                            onCreateExperiment = onCreateExperiment,
                            modifier = Modifier.weight(1f),
                        )
                    } else {
                        state.experiments.forEach { experiment ->
                            ExperimentCard(
                                experiment = experiment,
                                onCheckIn = { onCheckIn(experiment.id, true) },
                                onSkip = { onCheckIn(experiment.id, false) },
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
            style = androidx.compose.material3.MaterialTheme.typography.displayLarge,
            modifier = Modifier.semantics { contentDescription = "" },
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No active experiments",
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Start by running a small test on something you're curious about.",
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant,
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
