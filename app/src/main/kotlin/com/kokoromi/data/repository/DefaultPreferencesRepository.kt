package com.kokoromi.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.kokoromi.data.model.UserPreferences
import com.kokoromi.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.DayOfWeek
import javax.inject.Inject

class DefaultPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    private val reflectionDayKey = intPreferencesKey(Constants.PREF_REFLECTION_DAY)
    private val useSystemThemeKey = booleanPreferencesKey(Constants.PREF_USE_SYSTEM_THEME)

    override fun getUserPreferences(): Flow<UserPreferences> {
        return dataStore.data.map { prefs ->
            UserPreferences(
                reflectionDay = DayOfWeek.of(prefs[reflectionDayKey] ?: DayOfWeek.SUNDAY.value),
                useSystemTheme = prefs[useSystemThemeKey] ?: true
            )
        }
    }

    override suspend fun setReflectionDay(day: DayOfWeek) {
        dataStore.edit { prefs -> prefs[reflectionDayKey] = day.value }
    }

    override suspend fun setUseSystemTheme(useSystem: Boolean) {
        dataStore.edit { prefs -> prefs[useSystemThemeKey] = useSystem }
    }
}
