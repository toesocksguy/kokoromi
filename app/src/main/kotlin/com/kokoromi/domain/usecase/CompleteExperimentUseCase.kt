package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.CompletionRepository
import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.Completion
import com.kokoromi.domain.model.DecisionType
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.domain.model.Frequency
import com.kokoromi.util.Constants
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class CompleteExperimentUseCase @Inject constructor(
    private val experimentRepository: ExperimentRepository,
    private val completionRepository: CompletionRepository,
    private val computeCompletionStats: ComputeCompletionStatsUseCase,
    private val createExperiment: CreateExperimentUseCase,
) {
    /**
     * Persist: archive the completed experiment and start a new identical round.
     * Returns the new experiment's ID.
     */
    suspend fun persist(
        experimentId: String,
        learnings: String?,
    ): Result<String> {
        val experiment = experimentRepository.getExperiment(experimentId)
            ?: return Result.failure(IllegalArgumentException("Experiment not found: $experimentId"))

        val activeCount = experimentRepository.getActiveExperimentCount()
        if (activeCount >= Constants.MAX_ACTIVE_EXPERIMENTS) {
            return Result.failure(
                IllegalStateException("Cannot persist: ${Constants.MAX_ACTIVE_EXPERIMENTS} experiments already active")
            )
        }

        val stats = computeCompletionStats(experimentId).getOrElse {
            return Result.failure(it)
        }

        // Create the new experiment first so we have its ID for the Completion record
        val newExperimentId = createExperiment(
            hypothesis = experiment.hypothesis,
            action = experiment.action,
            why = experiment.why,
            durationDays = (experiment.endDate.toEpochDay() - experiment.startDate.toEpochDay() + 1).toInt(),
            frequency = experiment.frequency,
        ).getOrElse { return Result.failure(it) }

        return runCatching {
            completionRepository.saveCompletion(
                Completion(
                    id = UUID.randomUUID().toString(),
                    experimentId = experimentId,
                    completionDate = LocalDate.now(),
                    completionRate = stats.completionRate,
                    decision = DecisionType.PERSIST,
                    learnings = learnings?.trim()?.ifBlank { null },
                    nextExperimentId = newExperimentId,
                    createdAt = Instant.now(),
                )
            )
            experimentRepository.updateExperimentStatus(experimentId, ExperimentStatus.ARCHIVED)
            newExperimentId
        }
    }

    /**
     * Pivot: archive the completed experiment. Returns the experimentId so the
     * UI can pre-fill the create screen with this experiment's data.
     */
    suspend fun pivot(
        experimentId: String,
        learnings: String?,
    ): Result<Unit> {
        val experiment = experimentRepository.getExperiment(experimentId)
            ?: return Result.failure(IllegalArgumentException("Experiment not found: $experimentId"))

        val stats = computeCompletionStats(experimentId).getOrElse {
            return Result.failure(it)
        }

        return runCatching {
            completionRepository.saveCompletion(
                Completion(
                    id = UUID.randomUUID().toString(),
                    experimentId = experimentId,
                    completionDate = LocalDate.now(),
                    completionRate = stats.completionRate,
                    decision = DecisionType.PIVOT,
                    learnings = learnings?.trim()?.ifBlank { null },
                    nextExperimentId = null, // set later when user creates the new experiment
                    createdAt = Instant.now(),
                )
            )
            experimentRepository.updateExperimentStatus(experimentId, ExperimentStatus.ARCHIVED)
        }
    }

    /**
     * Pause: archive the completed experiment with no follow-up.
     */
    suspend fun pause(
        experimentId: String,
        learnings: String?,
    ): Result<Unit> {
        val experiment = experimentRepository.getExperiment(experimentId)
            ?: return Result.failure(IllegalArgumentException("Experiment not found: $experimentId"))

        val stats = computeCompletionStats(experimentId).getOrElse {
            return Result.failure(it)
        }

        return runCatching {
            completionRepository.saveCompletion(
                Completion(
                    id = UUID.randomUUID().toString(),
                    experimentId = experimentId,
                    completionDate = LocalDate.now(),
                    completionRate = stats.completionRate,
                    decision = DecisionType.PAUSE,
                    learnings = learnings?.trim()?.ifBlank { null },
                    nextExperimentId = null,
                    createdAt = Instant.now(),
                )
            )
            experimentRepository.updateExperimentStatus(experimentId, ExperimentStatus.ARCHIVED)
        }
    }
}
