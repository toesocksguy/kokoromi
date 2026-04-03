package com.kokoromi.domain.model

import java.time.Instant
import java.time.LocalDate

data class DailyLog(
    val id: String,
    val experimentId: String,
    val date: LocalDate,
    val completed: Boolean,
    val moodBefore: Int?,  // 1–5 or null
    val moodAfter: Int?,   // 1–5 or null
    val notes: String?,
    val loggedAt: Instant
)
