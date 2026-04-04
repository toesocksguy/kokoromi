package com.kokoromi.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kokoromi.util.Constants
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val DURATION_PRESETS = listOf(7, 14, 28)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateExperimentScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: CreateExperimentViewModel = hiltViewModel(),
) {
    val form by viewModel.form.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CreateExperimentUiState.Success -> onSuccess()
            is CreateExperimentUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.onErrorDismissed()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("What are you curious about?") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Hypothesis
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = form.hypothesis,
                    onValueChange = viewModel::onHypothesisChange,
                    label = { Text("What do you want to test?") },
                    placeholder = { Text("If I do X, will Y happen?") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "${form.hypothesis.length} / ${Constants.HYPOTHESIS_MAX_CHARS}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Action
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = form.action,
                    onValueChange = viewModel::onActionChange,
                    label = { Text("What will you do?") },
                    placeholder = { Text("Describe the specific action you'll take") },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "${form.action.length} / ${Constants.ACTION_MAX_CHARS}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Duration
            DurationPicker(
                selectedDays = form.durationDays,
                onDurationChange = viewModel::onDurationChange,
            )

            // Pact preview
            if (form.action.isNotBlank()) {
                PactPreview(action = form.action, durationDays = form.durationDays)
            }

            // Why (optional)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = form.why,
                    onValueChange = viewModel::onWhyChange,
                    label = { Text("Why? (optional)") },
                    placeholder = { Text("What's your motivation?") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "${form.why.length} / ${Constants.WHY_MAX_CHARS}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Button(
                onClick = viewModel::onSubmit,
                enabled = uiState !is CreateExperimentUiState.Loading,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Start Experiment")
            }
        }
    }
}

@Composable
private fun DurationPicker(
    selectedDays: Int,
    onDurationChange: (Int) -> Unit,
) {
    var customInput by remember { mutableStateOf("") }
    val isCustomSelected = selectedDays !in DURATION_PRESETS

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "How long?",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DURATION_PRESETS.forEach { days ->
                FilterChip(
                    selected = selectedDays == days,
                    onClick = {
                        customInput = ""
                        onDurationChange(days)
                    },
                    label = { Text(durationLabel(days)) },
                )
            }
            FilterChip(
                selected = isCustomSelected,
                onClick = {
                    customInput = ""
                    onDurationChange(Constants.DEFAULT_EXPERIMENT_DURATION_DAYS)
                },
                label = { Text("Custom") },
            )
        }
        if (isCustomSelected) {
            OutlinedTextField(
                value = customInput,
                onValueChange = { input ->
                    customInput = input
                    input.toIntOrNull()
                        ?.coerceIn(
                            Constants.MIN_EXPERIMENT_DURATION_DAYS,
                            Constants.MAX_EXPERIMENT_DURATION_DAYS,
                        )
                        ?.let(onDurationChange)
                },
                label = { Text("Days (${Constants.MIN_EXPERIMENT_DURATION_DAYS}–${Constants.MAX_EXPERIMENT_DURATION_DAYS})") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        val endDate = LocalDate.now().plusDays(selectedDays.toLong() - 1)
        Text(
            text = "Ends: ${endDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"))}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PactPreview(action: String, durationDays: Int) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = MaterialTheme.shapes.medium,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "YOUR PACT",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = "\"I will ${action.trim()} for ${durationLabel(durationDays).lowercase()}.\"",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

private fun durationLabel(days: Int): String = when (days) {
    7 -> "1 week"
    14 -> "2 weeks"
    28 -> "4 weeks"
    else -> "$days days"
}
