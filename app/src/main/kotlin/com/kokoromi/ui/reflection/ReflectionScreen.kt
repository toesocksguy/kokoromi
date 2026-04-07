package com.kokoromi.ui.reflection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReflectionScreen(
    onBack: () -> Unit,
    viewModel: ReflectionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val form by viewModel.form.collectAsStateWithLifecycle()
    val experimentName by viewModel.experimentName.collectAsStateWithLifecycle()
    val weekDaysCompleted by viewModel.weekDaysCompleted.collectAsStateWithLifecycle()
    val weekTotalDays by viewModel.weekTotalDays.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ReflectionUiState.Saved -> onBack()
            is ReflectionUiState.Error -> {
                snackbarHostState.showSnackbar((uiState as ReflectionUiState.Error).message)
                viewModel.onErrorDismissed()
            }
            else -> Unit
        }
    }

    val dateFmt = DateTimeFormatter.ofPattern("MMM d")
    val weekLabel = "Week of ${viewModel.weekStart.format(dateFmt)}"
    val name = experimentName.trim().trimEnd('.', ',', '!', '?', ';', ':')
        .replaceFirstChar { it.uppercase() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Time to Reflect")
                        if (name.isNotBlank()) {
                            Text(
                                text = "$name · $weekLabel",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
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
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when (uiState) {
            is ReflectionUiState.Loading -> {
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
                ReflectionContent(
                    form = form,
                    weekDaysCompleted = weekDaysCompleted,
                    weekTotalDays = weekTotalDays,
                    isSubmitting = uiState is ReflectionUiState.Loading,
                    onPlusChange = viewModel::onPlusChange,
                    onMinusChange = viewModel::onMinusChange,
                    onNextChange = viewModel::onNextChange,
                    onSave = viewModel::onSave,
                    onSkip = onBack,
                    modifier = Modifier.padding(innerPadding),
                )
            }
        }
    }
}

@Composable
private fun ReflectionContent(
    form: ReflectionFormState,
    weekDaysCompleted: Int,
    weekTotalDays: Int,
    isSubmitting: Boolean,
    onPlusChange: (String) -> Unit,
    onMinusChange: (String) -> Unit,
    onNextChange: (String) -> Unit,
    onSave: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Stat card
        if (weekTotalDays > 0) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics(mergeDescendants = true) {
                        contentDescription = "This week: $weekDaysCompleted of $weekTotalDays days completed"
                    },
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = "This week",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = "$weekDaysCompleted of $weekTotalDays days",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        text = "🌱",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.semantics { contentDescription = "" },
                    )
                }
            }
        }

        // Plus field
        PmnField(
            value = form.plus,
            onValueChange = onPlusChange,
            symbol = "+",
            symbolBackground = MaterialTheme.colorScheme.primaryContainer,
            symbolColor = MaterialTheme.colorScheme.onPrimaryContainer,
            label = "What went well?",
            placeholder = "What went well? What did you enjoy?",
        )

        // Minus field
        PmnField(
            value = form.minus,
            onValueChange = onMinusChange,
            symbol = "−",
            symbolBackground = MaterialTheme.colorScheme.errorContainer,
            symbolColor = MaterialTheme.colorScheme.error,
            label = "What didn't work?",
            placeholder = "What didn't work? What felt off?",
        )

        // Next field
        PmnField(
            value = form.next,
            onValueChange = onNextChange,
            symbol = "→",
            symbolBackground = MaterialTheme.colorScheme.tertiaryContainer,
            symbolColor = MaterialTheme.colorScheme.onTertiaryContainer,
            label = "What's your next small tweak?",
            placeholder = "What small tweak can you make based on what you've learned?",
        )

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedButton(
                onClick = onSkip,
                enabled = !isSubmitting,
                modifier = Modifier.weight(1f),
            ) {
                Text("SKIP")
            }
            Button(
                onClick = onSave,
                enabled = !isSubmitting && !form.isEmpty,
                modifier = Modifier
                    .weight(1f)
                    .semantics { contentDescription = "Save reflection" },
            ) {
                Text("SAVE")
            }
        }
    }
}

@Composable
private fun PmnField(
    value: String,
    onValueChange: (String) -> Unit,
    symbol: String,
    symbolBackground: Color,
    symbolColor: Color,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Surface(
                color = symbolBackground,
                shape = CircleShape,
                modifier = Modifier.size(20.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = symbol,
                        style = MaterialTheme.typography.labelSmall,
                        color = symbolColor,
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            minLines = 3,
            maxLines = 6,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedPlaceholderColor = MaterialTheme.colorScheme.outline,
                focusedPlaceholderColor = MaterialTheme.colorScheme.outline,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = label },
        )
    }
}
