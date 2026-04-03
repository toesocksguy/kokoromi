package com.kokoromi.data.model

import java.time.DayOfWeek

data class UserPreferences(
    val reflectionDay: DayOfWeek = DayOfWeek.SUNDAY,
    val useSystemTheme: Boolean = true
)
