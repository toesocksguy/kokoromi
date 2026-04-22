package com.kokoromi.data.model

import java.time.DayOfWeek

data class UserPreferences(
    val reflectionDay: DayOfWeek = DayOfWeek.SUNDAY,
    val theme: ThemePreference = ThemePreference.SYSTEM,
    val reminderEnabled: Boolean = false,
    val reminderHour: Int = 20,
    val reminderMinute: Int = 0,
)
