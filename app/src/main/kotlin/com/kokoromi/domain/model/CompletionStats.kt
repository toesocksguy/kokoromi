package com.kokoromi.domain.model

data class CompletionStats(
    val totalDays: Int,         // endDate - startDate + 1
    val daysLogged: Int,        // number of days with any log entry
    val daysCompleted: Int,     // number of days logged as completed
    val completionRate: Float,  // daysCompleted / totalDays (not / daysLogged)
    val avgMoodAfter: Float?,   // null if no mood data
    val moodDelta: Float?,      // avgMoodAfter - avgMoodBefore (null if either is missing)
)
