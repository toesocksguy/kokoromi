package com.kokoromi.ui

import com.kokoromi.data.model.ThemePreference
import com.kokoromi.data.model.UserPreferences
import com.kokoromi.data.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.DayOfWeek

class FakePreferencesRepository : PreferencesRepository {
    private val prefs = MutableStateFlow(UserPreferences())

    override fun getUserPreferences(): Flow<UserPreferences> = prefs

    override suspend fun setReflectionDay(day: DayOfWeek) {
        prefs.value = prefs.value.copy(reflectionDay = day)
    }

    override suspend fun setTheme(theme: ThemePreference) {
        prefs.value = prefs.value.copy(theme = theme)
    }

    override suspend fun setReminderEnabled(enabled: Boolean) {
        prefs.value = prefs.value.copy(reminderEnabled = enabled)
    }

    override suspend fun setReminderTime(hour: Int, minute: Int) {
        prefs.value = prefs.value.copy(reminderHour = hour, reminderMinute = minute)
    }
}