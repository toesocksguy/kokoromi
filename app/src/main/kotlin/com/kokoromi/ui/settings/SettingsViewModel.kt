package com.kokoromi.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kokoromi.data.model.ThemePreference
import com.kokoromi.data.model.UserPreferences
import com.kokoromi.data.repository.PreferencesRepository
import com.kokoromi.domain.usecase.ClearAllDataUseCase
import com.kokoromi.domain.usecase.ExportDataUseCase
import com.kokoromi.domain.usecase.ImportDataUseCase
import com.kokoromi.domain.usecase.ImportResult
import com.kokoromi.notification.ReminderScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

sealed interface SettingsEvent {
    data class ExportReady(val json: String) : SettingsEvent
    data class ImportComplete(val result: ImportResult) : SettingsEvent
    data class Error(val message: String) : SettingsEvent
    data object DataCleared : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
    private val exportData: ExportDataUseCase,
    private val importData: ImportDataUseCase,
    private val clearAllData: ClearAllDataUseCase,
    private val reminderScheduler: ReminderScheduler,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.getUserPreferences()
        .catch { /* emit nothing; screen shows defaults */ }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences(),
        )

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun onThemeChange(theme: ThemePreference) {
        viewModelScope.launch { preferencesRepository.setTheme(theme) }
    }

    fun onReflectionDayChange(day: DayOfWeek) {
        viewModelScope.launch { preferencesRepository.setReflectionDay(day) }
    }

    fun onExport() {
        viewModelScope.launch {
            exportData().onSuccess { json ->
                _events.send(SettingsEvent.ExportReady(json))
            }.onFailure { e ->
                _events.send(SettingsEvent.Error(e.message ?: "Export failed"))
            }
        }
    }

    fun onImport(json: String) {
        viewModelScope.launch {
            importData(json).onSuccess { result ->
                _events.send(SettingsEvent.ImportComplete(result))
            }.onFailure { e ->
                _events.send(SettingsEvent.Error(e.message ?: "Import failed"))
            }
        }
    }

    fun onClearAllData() {
        viewModelScope.launch {
            clearAllData().onSuccess {
                _events.send(SettingsEvent.DataCleared)
            }.onFailure { e ->
                _events.send(SettingsEvent.Error(e.message ?: "Failed to delete data"))
            }
        }
    }

    fun onReminderToggle(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setReminderEnabled(enabled)
            val prefs = preferences.value
            if (enabled) {
                reminderScheduler.schedule(prefs.reminderHour, prefs.reminderMinute)
            } else {
                reminderScheduler.cancel()
            }
        }
    }

    fun onReminderTimeChange(hour: Int, minute: Int) {
        viewModelScope.launch {
            preferencesRepository.setReminderTime(hour, minute)
            if (preferences.value.reminderEnabled) {
                reminderScheduler.schedule(hour, minute)
            }
        }
    }
}
