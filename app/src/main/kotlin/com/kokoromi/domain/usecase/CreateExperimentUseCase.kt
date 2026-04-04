package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.Experiment
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.model.Frequency
import com.kokoromi.util.Constants
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class CreateExperimentUseCase @Inject constructor(
    private val repository: ExperimentRepository,
) {
    suspend operator fun invoke(
        hypothesis: String,
        action: String,
        why: String?,
        durationDays: Int,
        frequency: Frequency = Frequency.DAILY,
    ): Result<String> {
        ExperimentValidator.validate(hypothesis, action, why, durationDays)
            .onFailure { return Result.failure(it) }

        val activeCount = repository.getActiveExperimentCount()
        if (activeCount >= Constants.MAX_ACTIVE_EXPERIMENTS) {
            return Result.failure(
                IllegalStateException("Maximum of ${Constants.MAX_ACTIVE_EXPERIMENTS} active experiments reached")
            )
        }

        val now = Instant.now()
        val startDate = LocalDate.now()
        val experiment = Experiment(
            id = UUID.randomUUID().toString(),
            hypothesis = hypothesis.trim(),
            action = action.trim(),
            why = why?.trim()?.ifBlank { null },
            startDate = startDate,
            endDate = startDate.plusDays(durationDays.toLong() - 1),
            frequency = frequency,
            status = ExperimentStatus.ACTIVE,
            createdAt = now,
            updatedAt = now,
        )

        return runCatching { repository.createExperiment(experiment) }
    }
}
