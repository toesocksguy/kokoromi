package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.util.Constants
import javax.inject.Inject

class UpdateExperimentStatusUseCase @Inject constructor(
    private val experimentRepository: ExperimentRepository,
) {
    suspend fun pause(experimentId: String): Result<Unit> {
        val experiment = experimentRepository.getExperiment(experimentId)
            ?: return Result.failure(IllegalArgumentException("Experiment not found: $experimentId"))

        if (experiment.status != ExperimentStatus.ACTIVE) {
            return Result.failure(IllegalStateException("Only ACTIVE experiments can be paused"))
        }

        return runCatching {
            experimentRepository.updateExperimentStatus(experimentId, ExperimentStatus.PAUSED)
        }
    }

    suspend fun archive(experimentId: String): Result<Unit> {
        val experiment = experimentRepository.getExperiment(experimentId)
            ?: return Result.failure(IllegalArgumentException("Experiment not found: $experimentId"))

        if (experiment.status != ExperimentStatus.PAUSED) {
            return Result.failure(IllegalStateException("Only PAUSED experiments can be archived"))
        }

        return runCatching {
            experimentRepository.updateExperimentStatus(experimentId, ExperimentStatus.ARCHIVED)
        }
    }

    suspend fun endEarly(experimentId: String): Result<Unit> {
        val experiment = experimentRepository.getExperiment(experimentId)
            ?: return Result.failure(IllegalArgumentException("Experiment not found: $experimentId"))

        if (experiment.status != ExperimentStatus.ACTIVE) {
            return Result.failure(IllegalStateException("Only ACTIVE experiments can be ended early"))
        }

        return runCatching {
            experimentRepository.updateExperimentStatus(experimentId, ExperimentStatus.COMPLETED)
        }
    }

    suspend fun resume(experimentId: String): Result<Unit> {
        val experiment = experimentRepository.getExperiment(experimentId)
            ?: return Result.failure(IllegalArgumentException("Experiment not found: $experimentId"))

        if (experiment.status != ExperimentStatus.PAUSED) {
            return Result.failure(IllegalStateException("Only PAUSED experiments can be resumed"))
        }

        val activeCount = experimentRepository.getActiveExperimentCount()
        if (activeCount >= Constants.MAX_ACTIVE_EXPERIMENTS) {
            return Result.failure(
                IllegalStateException("Cannot resume: ${Constants.MAX_ACTIVE_EXPERIMENTS} experiments already active")
            )
        }

        return runCatching {
            experimentRepository.updateExperimentStatus(experimentId, ExperimentStatus.ACTIVE)
        }
    }
}
