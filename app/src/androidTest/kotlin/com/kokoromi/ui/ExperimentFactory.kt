package com.kokoromi.ui

import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.model.Frequency
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

fun makeExperiment(
    id: String = UUID.randomUUID().toString(),
    action: String = "Do the thing",
    durationDays: Int = 7,
    status: ExperimentStatus = ExperimentStatus.ACTIVE,
): Experiment {
    val start = LocalDate.now()
    return Experiment(
        id = id,
        hypothesis = "If I do X, then Y will happen",
        action = action,
        why = null,
        startDate = start,
        endDate = start.plusDays(durationDays.toLong() - 1),
        frequency = Frequency.DAILY,
        status = status,
        createdAt = Instant.now(),
        updatedAt = Instant.now(),
    )
}
