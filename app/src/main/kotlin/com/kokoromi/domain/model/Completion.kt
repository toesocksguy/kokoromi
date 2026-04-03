package com.kokoromi.domain.model

import java.time.Instant
import java.time.LocalDate

data class Completion(
    val id: String,
    val experimentId: String,
    val completionDate: LocalDate,
    val completionRate: Float,       // 0.0–1.0 (daysCompleted / totalDays)
    val decision: DecisionType,
    val learnings: String?,
    val nextExperimentId: String?,   // Set when decision = PIVOT
    val createdAt: Instant
)
