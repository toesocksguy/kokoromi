package com.kokoromi.data.repository

import com.kokoromi.data.model.UserPreferences
import kotlinx.coroutines.flow.Flow
import java.time.DayOfWeek

interface PreferencesRepository {
    fun getUserPreferences(): Flow<UserPreferences>
    suspend fun setReflectionDay(day: DayOfWeek)
    suspend fun setUseSystemTheme(useSystem: Boolean)
}
