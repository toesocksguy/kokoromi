package com.kokoromi.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kokoromi.data.model.ThemePreference
import com.kokoromi.ui.components.KokoromiBottomNav
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToTab: (Int) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    var showReflectionDayPicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var pendingExportJson by remember { mutableStateOf<String?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val json = pendingExportJson ?: return@rememberLauncherForActivityResult
        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { it.write(json.toByteArray()) }
        }
        pendingExportJson = null
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.ExportReady -> {
                    pendingExportJson = event.json
                    createDocumentLauncher.launch("kokoromi_export.json")
                }
                is SettingsEvent.DataCleared -> {
                    snackbarHostState.showSnackbar("All data deleted")
                }
                is SettingsEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    if (showReflectionDayPicker) {
        ReflectionDayDialog(
            current = prefs.reflectionDay,
            onConfirm = { day ->
                viewModel.onReflectionDayChange(day)
                showReflectionDayPicker = false
            },
            onDismiss = { showReflectionDayPicker = false },
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete all data?") },
            text = {
                Text(
                    "This will permanently delete all experiments, logs, reflections, and field notes. " +
                    "Export your data first if you want to keep a backup."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.onClearAllData()
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            },
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Settings") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = { KokoromiBottomNav(selectedIndex = 3, onNavigate = onNavigateToTab) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            SectionLabel("Preferences")

            SettingsRow(label = "Theme") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ThemePreference.entries.forEach { option ->
                        FilterChip(
                            selected = prefs.theme == option,
                            onClick = { viewModel.onThemeChange(option) },
                            label = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                            modifier = Modifier.padding(end = 6.dp),
                        )
                    }
                }
            }

            HorizontalDivider()

            SettingsRow(
                label = "Reflection Day",
                trailing = {
                    TextButton(onClick = { showReflectionDayPicker = true }) {
                        Text(
                            prefs.reflectionDay.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " ›",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                },
            )

            HorizontalDivider()

            SectionLabel("Data", topPadding = true)

            OutlinedButton(
                onClick = viewModel::onExport,
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            ) {
                Text("Export all data as JSON")
            }

            val errorColor = MaterialTheme.colorScheme.error
            OutlinedButton(
                onClick = { showDeleteConfirm = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = errorColor),
                border = BorderStroke(width = 1.dp, color = errorColor),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            ) {
                Text("Delete all data")
            }

            SectionLabel("About", topPadding = true)

            SettingsRow(label = "Privacy", trailing = { Text("No tracking", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) })
            HorizontalDivider()
            SettingsRow(label = "License", trailing = { Text("GPL-3.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) })
            HorizontalDivider()

            val uriHandler = LocalUriHandler.current
            OutlinedButton(
                onClick = { uriHandler.openUri("https://github.com/toesocksguy/kokoromi") },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            ) {
                Text("View source code")
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String, topPadding: Boolean = false) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = if (topPadding) 20.dp else 8.dp, bottom = 4.dp),
    )
}

@Composable
private fun SettingsRow(
    label: String,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        trailing?.invoke()
        content?.invoke()
    }
}

@Composable
private fun ReflectionDayDialog(
    current: DayOfWeek,
    onConfirm: (DayOfWeek) -> Unit,
    onDismiss: () -> Unit,
) {
    var selected by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reflection day") },
        text = {
            Column {
                DayOfWeek.entries.forEach { day ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        RadioButton(
                            selected = selected == day,
                            onClick = { selected = day },
                        )
                        Text(
                            text = day.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected) }) { Text("OK") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
