package com.kokoromi.util

object Constants {
    // Experiment rules
    const val MAX_ACTIVE_EXPERIMENTS = 2
    const val MIN_EXPERIMENT_DURATION_DAYS = 1
    const val MAX_EXPERIMENT_DURATION_DAYS = 180
    const val DEFAULT_EXPERIMENT_DURATION_DAYS = 7

    // Input character limits
    const val HYPOTHESIS_MAX_CHARS = 500
    const val ACTION_MAX_CHARS = 500
    const val WHY_MAX_CHARS = 500
    const val NOTES_MAX_CHARS = 1000
    const val REFLECTION_MAX_CHARS = 2000
    const val FIELD_NOTE_TITLE_MAX_CHARS = 200
    const val FIELD_NOTE_BODY_MAX_CHARS = 5000

    // Mood rating range
    const val MOOD_MIN = 1
    const val MOOD_MAX = 5

    // Database
    const val DATABASE_NAME = "kokoromi.db"

    // Preferences keys
    const val PREF_REFLECTION_DAY = "reflection_day"
    const val PREF_USE_SYSTEM_THEME = "use_system_theme"
}
