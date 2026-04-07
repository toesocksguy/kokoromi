package com.kokoromi.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kokoromi.data.model.ThemePreference
import com.kokoromi.data.model.UserPreferences
import com.kokoromi.data.repository.PreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.getUserPreferences()
        .catch { /* emit nothing; screen shows defaults */ }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UserPreferences(),
        )

    fun onThemeChange(theme: ThemePreference) {
        viewModelScope.launch { preferencesRepository.setTheme(theme) }
    }

    fun onReflectionDayChange(day: DayOfWeek) {
        viewModelScope.launch { preferencesRepository.setReflectionDay(day) }
    }
}
