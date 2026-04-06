package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.DailyLogRepository
import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.CompletionStats
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class ComputeCompletionStatsUseCase @Inject constructor(
    private val experimentRepository: ExperimentRepository,
    private val dailyLogRepository: DailyLogRepository,
) {
    suspend operator fun invoke(experimentId: String): Result<CompletionStats> {
        val experiment = experimentRepository.getExperiment(experimentId)
            ?: return Result.failure(IllegalArgumentException("Experiment not found: $experimentId"))

        val logs = dailyLogRepository.getLogsInRange(
            experimentId = experimentId,
            startDate = experiment.startDate,
            endDate = experiment.endDate,
        )

        val totalDays = ChronoUnit.DAYS.between(experiment.startDate, experiment.endDate).toInt() + 1
        val daysLogged = logs.size
        val daysCompleted = logs.count { it.completed }
        val completionRate = daysCompleted.toFloat() / totalDays

        val moodReadings = logs.mapNotNull { it.moodAfter }
        val avgMoodAfter = if (moodReadings.isEmpty()) null else moodReadings.average().toFloat()

        return Result.success(
            CompletionStats(
                totalDays = totalDays,
                daysLogged = daysLogged,
                daysCompleted = daysCompleted,
                completionRate = completionRate,
                avgMoodAfter = avgMoodAfter,
                moodDelta = null,
            )
        )
    }
}
