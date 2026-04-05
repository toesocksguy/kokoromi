package com.kokoromi.domain.usecase

import com.kokoromi.data.repository.DailyLogRepository
import com.kokoromi.data.repository.ExperimentRepository
import com.kokoromi.domain.model.DailyLog
import com.kokoromi.domain.model.ExperimentStatus
import com.kokoromi.util.Constants
import java.time.Instant
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

class LogDailyCheckInUseCase @Inject constructor(
    private val experimentRepository: ExperimentRepository,
    private val dailyLogRepository: DailyLogRepository,
) {
    suspend operator fun invoke(
        experimentId: String,
        date: LocalDate,
        completed: Boolean,
        moodBefore: Int? = null,
        moodAfter: Int? = null,
        notes: String? = null,
    ): Result<Unit> {
        val experiment = experimentRepository.getExperiment(experimentId)
            ?: return Result.failure(IllegalArgumentException("Experiment not found: $experimentId"))

        if (!canLogForDate(experiment.status, experiment.startDate, experiment.endDate, date)) {
            return Result.failure(
                IllegalStateException("Cannot log check-in for $date on this experiment")
            )
        }

        if (moodBefore != null && moodBefore !in Constants.MOOD_MIN..Constants.MOOD_MAX) {
            return Result.failure(
                IllegalArgumentException("moodBefore must be ${Constants.MOOD_MIN}–${Constants.MOOD_MAX}")
            )
        }
        if (moodAfter != null && moodAfter !in Constants.MOOD_MIN..Constants.MOOD_MAX) {
            return Result.failure(
                IllegalArgumentException("moodAfter must be ${Constants.MOOD_MIN}–${Constants.MOOD_MAX}")
            )
        }

        val existingLog = dailyLogRepository.getLogForDate(experimentId, date)
        val log = DailyLog(
            id = existingLog?.id ?: UUID.randomUUID().toString(),
            experimentId = experimentId,
            date = date,
            completed = completed,
            moodBefore = moodBefore,
            moodAfter = moodAfter,
            notes = notes?.trim()?.ifBlank { null },
            loggedAt = Instant.now(),
        )

        return runCatching { dailyLogRepository.upsertDailyLog(log) }
    }

    private fun canLogForDate(
        status: ExperimentStatus,
        startDate: LocalDate,
        endDate: LocalDate,
        date: LocalDate,
    ): Boolean {
        return status == ExperimentStatus.ACTIVE
            && date >= startDate
            && date <= minOf(endDate, LocalDate.now())
    }
}
