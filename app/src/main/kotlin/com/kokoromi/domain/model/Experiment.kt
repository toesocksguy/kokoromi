package com.kokoromi.domain.model

import java.time.Instant
import java.time.LocalDate

data class Experiment(
    val id: String,
    val hypothesis: String,
    val action: String,
    val why: String?,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val frequency: Frequency,
    val status: ExperimentStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)
