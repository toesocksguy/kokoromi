package com.kokoromi.ui.checkin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kokoromi.util.Constants
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckInScreen(
    onBack: () -> Unit,
    viewModel: CheckInViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val displayState by viewModel.displayState.collectAsStateWithLifecycle()
    val form by viewModel.form.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is CheckInUiState.Saved -> onBack()
            is CheckInUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.onErrorDismissed()
            }
            else -> Unit
        }
    }

    val checkInDate = displayState?.checkInDate ?: LocalDate.now()
    val isToday = checkInDate == LocalDate.now()
    val dateLabel = if (isToday) "today" else checkInDate.format(DateTimeFormatter.ofPattern("MMM d"))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(displayState?.experimentAction?.ifBlank { "Check In" } ?: "Check In") },
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
        when (uiState) {
            is CheckInUiState.Loading -> {
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    Text(
                        text = if (displayState?.isEditing == true) "Edit log for $dateLabel" else "Did you do it $dateLabel?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    CompletionButtons(
                        completed = form.completed,
                        onCompletedChange = viewModel::onCompletedChange,
                    )

                    HorizontalDivider()

                    MoodRating(
                        label = "How did it feel?",
                        value = form.mood,
                        onValueChange = viewModel::onMoodChange,
                    )

                    OutlinedTextField(
                        value = form.notes,
                        onValueChange = viewModel::onNotesChange,
                        label = { Text("Notes (optional)") },
                        placeholder = { Text("Anything interesting?") },
                        minLines = 3,
                        maxLines = 6,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.outline,
                            focusedPlaceholderColor = MaterialTheme.colorScheme.outline,
                        ),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Button(
                        onClick = viewModel::onSubmit,
                        enabled = uiState is CheckInUiState.Ready,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text("Log Activity")
                    }
                }
            }
        }
    }
}

@Composable
private fun CompletionButtons(
    completed: Boolean,
    onCompletedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = if (completed) "Completed: Yes selected" else "Completed: Skip selected"
            },
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (completed) {
            Button(
                onClick = { onCompletedChange(true) },
                modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
            ) { Text("✓ YES") }
            OutlinedButton(
                onClick = { onCompletedChange(false) },
                modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
            ) { Text("✗ SKIP") }
        } else {
            OutlinedButton(
                onClick = { onCompletedChange(true) },
                modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
            ) { Text("✓ YES") }
            Button(
                onClick = { onCompletedChange(false) },
                modifier = Modifier.weight(1f).minimumInteractiveComponentSize(),
            ) { Text("✗ SKIP") }
        }
    }
}

@Composable
private fun MoodRating(
    label: String,
    value: Int?,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            (Constants.MOOD_MIN..Constants.MOOD_MAX).forEach { star ->
                IconButton(
                    onClick = {
                        // Tap the current rating again to clear it
                        onValueChange(if (value == star) null else star)
                    },
                    modifier = Modifier.semantics {
                        contentDescription = buildString {
                            append(if ((value ?: 0) >= star) "Star $star, filled" else "Star $star, empty")
                            if (value == star) append(", tap to clear")
                        }
                    },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if ((value ?: 0) >= star) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    )
                }
            }
        }
    }
}

